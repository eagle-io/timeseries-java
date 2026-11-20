package io.eagle.util.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdScalarSerializer;
import org.joda.time.Period;



public final class PeriodSerializer extends StdScalarSerializer<Period> {

    private static final long serialVersionUID = -7690680265143252016L;


    public PeriodSerializer() {
        super(Period.class);
    }


    @Override
    public void serialize(Period value, JsonGenerator jgen, SerializationContext provider) {
        String opc = value.toString();

        if (opc.contains("T"))
            opc = opc.replaceFirst("M(.*T)", "MO$1");
        else
            opc = opc.replaceFirst("M", "MO");

        opc = opc.replaceAll("[PT]", "");

        jgen.writeString(opc);
    }

}
