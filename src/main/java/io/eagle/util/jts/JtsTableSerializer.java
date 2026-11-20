package io.eagle.util.jts;

import com.google.common.collect.ImmutableMap;
import io.eagle.util.jackson.JacksonUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Map;


/**
 * Jackson serializer which serializes a {@link JtsDocument} to JSON.
 * <p>
 * This serializer contains two features beyond regular serialization of Objects to JSON:
 * <ul>
 * <li>if a non-null column header map is provided, it will be used to format {@link Double} Objects according to the appropriate format
 * string
 * <li>if the {@link DocumentFormat} is pretty and the generator uses a {@link JtsPrettyPrinter}, it is temporarily switched to single-line
 * output, causing each {@link JtsRecord} to be printed on only one line (this improves readability when there are many JtsRecords in a
 * document)
 * </ul>
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 * @see JacksonUtil
 */
public final class JtsTableSerializer extends StdSerializer<JtsTable<?>> {

    private static final long serialVersionUID = 1L;


    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(JtsTableSerializer.class);

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
     * @see tools.jackson.databind.ValueSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator,
     * tools.jackson.databind.SerializationContext)
     */
    @Override
    public void serialize(JtsTable<?> table, JsonGenerator jgen, SerializationContext provider) {
        JtsPrettyPrinter prettyPrinter = this.documentFormat.isPretty() && jgen.getPrettyPrinter() instanceof JtsPrettyPrinter
                ? (JtsPrettyPrinter) jgen.getPrettyPrinter() : null;

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
                if (prettyPrinter != null)
                    prettyPrinter.setSingleLine(true);
            } else {
                tsValue = ImmutableMap.of(Time.MILLIS_KEY, ts.getMillis());
            }

            if (this.documentFormat.getDocumentSubType() == DocumentSubType.WINDROSE)
                tsValue = ImmutableMap.of("$winddir", ts.getMillis());

            jgen.writeStartObject();
            jgen.writePOJOProperty("ts", tsValue);
            jgen.writeName("f");
            jgen.writeStartObject();

            fields = records.get(ts);

            for (int index : table.getColumnIndexes()) {
                if (fields.containsKey(index)) {
                    field = fields.get(index);

                    jgen.writeName(String.valueOf(index));
                    jgen.writeStartObject();

                    if (field.getValue() != null) {
                        String format = this.numberFormats != null && this.numberFormats.containsKey(index) ? this.numberFormats.get(index) : null;
                        String value = JtsDocument.renderValue(field.getValue(), format, this.documentFormat, this.dateTimeZone);

                        if (field.getValue() instanceof Time) {
                            if (this.documentFormat.isPretty()) {
                                String tsString = String.valueOf(JtsDocument.renderTime(field.getValueAsTime().getValue(), this.dateTimeZone, format));
                                jgen.writePOJOProperty("v", ImmutableMap.of(Time.TIME_KEY, tsString));
                            } else {
                                jgen.writePOJOProperty("v", ImmutableMap.of(Time.MILLIS_KEY, field.getValueAsTime().getValue().getMillis()));
                            }
                        } else if (field.getValue() instanceof Double) {
                            jgen.writeName("v");
                            jgen.writeNumber(value);
                        } else if (field.getValue() instanceof String) {
                            // Text qualifiers should be ignored for JSON so we ignore the rendered value
                            jgen.writePOJOProperty("v", field.getValue());
                        } else if (field.getValue() instanceof ComplexValue) {
                            // ComplexValues should be serialized as an object with embedded type identifier
                            jgen.writePOJOProperty("v", field.getValue());
                        } else
                            jgen.writePOJOProperty("v", value);
                    }

                    if (field.getQuality() != null)
                        jgen.writePOJOProperty("q", field.getQuality());

                    if (field.getAnnotation() != null)
                        jgen.writePOJOProperty("a", field.getAnnotation());

                    jgen.writeEndObject();
                }
            }

            // end fields
            jgen.writeEndObject();

            // end record
            jgen.writeEndObject();

            // Switch back to the regular multiline pretty printer
            if (prettyPrinter != null)
                prettyPrinter.setSingleLine(false);
        }

        if (this.documentFormat.isPretty())
            jgen.writeRaw("\n  ");

        jgen.writeEndArray();
    }
}
