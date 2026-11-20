package io.eagle.util.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdScalarSerializer;
import org.joda.time.DateTimeZone;



public final class DateTimeZoneSerializer extends StdScalarSerializer<DateTimeZone> {
    private static final long serialVersionUID = 7758274249103592979L;


    public DateTimeZoneSerializer() {
        super(DateTimeZone.class);
    }


    @Override
    public void serialize(DateTimeZone value, JsonGenerator jgen, SerializationContext provider) {
        jgen.writeString(value.getID());
    }

}
