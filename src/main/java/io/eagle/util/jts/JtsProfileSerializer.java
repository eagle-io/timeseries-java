package io.eagle.util.jts;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.eagle.util.NumberFormat;
import org.joda.time.DateTime;

import java.io.IOException;


/**
 * [
 * { 0:{ pos: 499.345, f:{ v: 45 } }, 1:{ pos: 499.132, f:{ v: 45 } }, 2:{ pos: 499.555, f:{ v: 45 } }, 3:{ pos: 499.577, f:{ v: 45 } } },
 * { 0:{ pos: 999.567, f:{ v: 45 } }, 1:{ pos: 999.945, f:{ v: 45 } }, 2:{ pos: 999.972, f:{ v: 45 } }, 3:{ pos: 999.013, f:{ v: 45 } } },
 * { 0:{ pos: 1499.345, f:{ v: 45 } }, 1:{ pos: 1499.132, f:{ v: 45 } }, 2:{ pos: 1499.555, f:{ v: 45 } }, 3:{ pos: 1499.577, f:{ v: 45 } }
 * }
 * ]
 */
public class JtsProfileSerializer extends StdSerializer<JtsProfile> {

    private static final long serialVersionUID = 1L;

    public JtsProfileSerializer() {
        super(JtsProfile.class);
    }

    private static String format(Double number) {
        if (number == null)
            return "null";
        else
            return NumberFormat.getValueFormatted(number, "0.###");
    }

    @Override
    public void serialize(JtsProfile profile, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeRaw("\n");
        jgen.writeStartArray();
        jgen.writeRaw("\n");

        for (int row = 0; row < profile.recordCount(); row++) {
            jgen.writeRaw("  ");
            jgen.writeStartObject();

            int column = 0;

            for (DateTime ts : profile.getProfileTimes()) {
                Double pos = profile.getPos(row, ts);
                Double val = profile.getVal(row, ts);

                jgen.writeObjectFieldStart(String.valueOf(column++));
                jgen.writeFieldName("pos");
                jgen.writeNumber(format(pos));
                jgen.writeObjectFieldStart("f");
                jgen.writeFieldName("v");
                jgen.writeNumber(format(val));
                jgen.writeEndObject();
                jgen.writeEndObject();
            }

            jgen.writeEndObject();
            jgen.writeRaw("\n");
        }

        jgen.writeEndArray();
    }

}
