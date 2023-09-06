package io.eagle.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import io.eagle.util.time.OpcTime;
import org.joda.time.Period;


public final class PeriodDeserializer extends StdScalarDeserializer<Period> {

    /**
     * Default serial version ID
     */
    private static final long serialVersionUID = 1L;


    public PeriodDeserializer() {
        super(Period.class);
    }


    @Override
    public Period deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JsonMappingException {
        Period period = null;
        JsonToken token = jsonParser.getCurrentToken();

        try {
            // The JSON token must be a String
            if (token == JsonToken.VALUE_STRING) {
                period = OpcTime.parsePeriod(jsonParser.getValueAsString());
            } else {
                throw deserializationContext.mappingException("Invalid type, must be String: " + token);
            }
        } catch (Exception e) {
            throw deserializationContext.mappingException("Invalid ObjectId: " + token);
        }

        return period;
    }

}
