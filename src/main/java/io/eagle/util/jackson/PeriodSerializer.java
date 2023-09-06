package io.eagle.util.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.joda.time.Period;

import java.io.IOException;


public final class PeriodSerializer extends StdScalarSerializer<Period> {

    private static final long serialVersionUID = -7690680265143252016L;


    public PeriodSerializer() {
        super(Period.class);
    }


    @Override
    public void serialize(Period value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        String opc = value.toString();

        if (opc.contains("T"))
            opc = opc.replaceFirst("M(.*T)", "MO$1");
        else
            opc = opc.replaceFirst("M", "MO");

        opc = opc.replaceAll("[PT]", "");

        jgen.writeString(opc);
    }

}
