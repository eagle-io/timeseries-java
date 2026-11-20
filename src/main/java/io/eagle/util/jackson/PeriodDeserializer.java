package io.eagle.util.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
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
    public Period deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws DatabindException {
        Period period = null;
        JsonToken token = jsonParser.currentToken();

        try {
            // The JSON token must be a String
            if (token == JsonToken.VALUE_STRING) {
                period = OpcTime.parsePeriod(jsonParser.getValueAsString());
            } else {
                throw DatabindException.from(jsonParser, "Invalid type, must be String: " + token);
            }
        } catch (Exception e) {
            throw DatabindException.from(jsonParser, "Invalid ObjectId: " + token);
        }

        return period;
    }

}
