package io.eagle.util.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import org.joda.time.DateTimeZone;


public final class DateTimeZoneDeserializer extends StdScalarDeserializer<DateTimeZone> {

    /**
     * Default serial version ID
     */
    private static final long serialVersionUID = 1L;


    public DateTimeZoneDeserializer() {
        super(DateTimeZone.class);
    }


    @Override
    public DateTimeZone deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws DatabindException {
        DateTimeZone dateTimeZone = null;
        JsonToken token = jsonParser.currentToken();

        try {
            // The JSON token must be a String
            if (token == JsonToken.VALUE_STRING) {
                dateTimeZone = DateTimeZone.forID(jsonParser.getValueAsString());
            } else {
                throw DatabindException.from(jsonParser, "Invalid type, must be String: " + token);
            }
        } catch (Exception e) {
            throw DatabindException.from(jsonParser, "Invalid ObjectId: " + token);
        }

        return dateTimeZone;
    }

}
