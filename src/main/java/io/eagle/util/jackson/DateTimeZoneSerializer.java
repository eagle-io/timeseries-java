package io.eagle.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.joda.time.DateTimeZone;

import java.io.IOException;


public final class DateTimeZoneSerializer extends StdScalarSerializer<DateTimeZone> {
    private static final long serialVersionUID = 7758274249103592979L;


    public DateTimeZoneSerializer() {
        super(DateTimeZone.class);
    }


    @Override
    public void serialize(DateTimeZone value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(value.getID());
    }

}
