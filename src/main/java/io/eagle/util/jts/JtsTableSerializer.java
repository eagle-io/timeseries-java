package io.eagle.util.jts;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.ImmutableMap;
import io.eagle.util.jackson.JacksonUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;


/**
 * Jackson serializer which serializes a {@link JtsDocument} to JSON.
 * <p>
 * This serializer contains two features beyond regular serialization of Objects to JSON:
 * <ul>
 * <li>if a non-null column header map is provided, it will be used to format {@link Double} Objects according to the appropriate format
 * string
 * <li>if a {@link PrettyPrinter} has already been enabled, it will be temporarily replaced with a {@link MinimalPrettyPrinter}, causing
 * each {@link JtsRecord} to be printed on only one line (this improves readability when there are many JtsRecords in a document)
 * </ul>
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 * @see JacksonUtil
 */
public final class JtsTableSerializer extends StdSerializer<JtsTable<?>> {

    private static final long serialVersionUID = 1L;


    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(JtsTableSerializer.class);

    /**
     * A minimal PrettyPrinter used to ensure each JtsRecord is printed on only one line
     */
    private static final PrettyPrinter minimalPrettyPrinter = new MinimalPrettyPrinter();

    private static final PrettyPrinter regularPrettyPrinter = new DefaultPrettyPrinter();

    private final Map<Integer, String> numberFormats;
    private final DocumentFormat documentFormat;
    private final DateTimeZone dateTimeZone;

    public JtsTableSerializer(Map<Integer, String> numberFormats, DocumentFormat documentFormat, DateTimeZone dateTimeZone) {
        super(JtsTable.class, true);
        this.numberFormats = numberFormats;
        this.documentFormat = documentFormat;
        this.dateTimeZone = dateTimeZone;
    }


    /**
     * Serializes the given {@link JtsDocument} to JSON.
     *
     * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator,
     * com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(JtsTable<?> table, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (this.documentFormat.isPretty())
            jgen.setPrettyPrinter(regularPrettyPrinter);

        jgen.writeStartArray();

        if (table.isEmpty()) {
            jgen.writeEndArray();
            return;
        }

        Map<DateTime, Map<Integer, JtsField>> records = table.getRecords();
        Map<Integer, JtsField> fields = null;
        JtsField field = null;
        Object tsValue = null;

        for (DateTime ts : records.keySet()) {
            // If the pretty option is enabled, each JtsRecord will be printed on its own single line
            if (this.documentFormat.isPretty()) {
                jgen.writeRaw("\n    ");
                tsValue = String.valueOf(ts.withZone(this.dateTimeZone));
                jgen = jgen.setPrettyPrinter(minimalPrettyPrinter);
            } else {
                tsValue = ImmutableMap.of(Time.MILLIS_KEY, ts.getMillis());
            }

            if (this.documentFormat.getDocumentSubType() == DocumentSubType.WINDROSE)
                tsValue = ImmutableMap.of("$winddir", ts.getMillis());

            jgen.writeStartObject();
            jgen.writeObjectField("ts", tsValue);
            jgen.writeFieldName("f");
            jgen.writeStartObject();

            fields = records.get(ts);

            for (int index : table.getColumnIndexes()) {
                if (fields.containsKey(index)) {
                    field = fields.get(index);

                    jgen.writeFieldName(String.valueOf(index));
                    jgen.writeStartObject();

                    if (field.getValue() != null) {
                        String format = this.numberFormats != null && this.numberFormats.containsKey(index) ? this.numberFormats.get(index) : null;
                        String value = JtsDocument.renderValue(field.getValue(), format, this.documentFormat, this.dateTimeZone);

                        if (field.getValue() instanceof Time) {
                            if (this.documentFormat.isPretty()) {
                                String tsString = String.valueOf(JtsDocument.renderTime(field.getValueAsTime().getValue(), this.dateTimeZone, format));
                                jgen.writeObjectField("v", ImmutableMap.of(Time.TIME_KEY, tsString));
                            } else {
                                jgen.writeObjectField("v", ImmutableMap.of(Time.MILLIS_KEY, field.getValueAsTime().getValue().getMillis()));
                            }
                        } else if (field.getValue() instanceof Double) {
                            jgen.writeFieldName("v");
                            jgen.writeNumber(value);
                        } else if (field.getValue() instanceof String) {
                            // Text qualifiers should be ignored for JSON so we ignore the rendered value
                            jgen.writeObjectField("v", field.getValue());
                        } else if (field.getValue() instanceof ComplexValue) {
                            // ComplexValues should be serialized as an object with embedded type identifier
                            jgen.writeObjectField("v", field.getValue());
                        } else
                            jgen.writeObjectField("v", value);
                    }

                    if (field.getQuality() != null)
                        jgen.writeObjectField("q", field.getQuality());

                    if (field.getAnnotation() != null)
                        jgen.writeObjectField("a", field.getAnnotation());

                    jgen.writeEndObject();
                }
            }

            // end fields
            jgen.writeEndObject();

            // end record
            jgen.writeEndObject();

            // Replace our MinimalPrettyPrinter with the regular multiline pretty printer
            if (this.documentFormat.isPretty())
                jgen = jgen.setPrettyPrinter(regularPrettyPrinter);
        }

        if (this.documentFormat.isPretty())
            jgen.writeRaw("\n  ");

        jgen.writeEndArray();
    }
}
