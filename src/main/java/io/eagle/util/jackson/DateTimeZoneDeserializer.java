package io.eagle.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
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
    public DateTimeZone deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JsonMappingException {
        DateTimeZone dateTimeZone = null;
        JsonToken token = jsonParser.getCurrentToken();

        try {
            // The JSON token must be a String
            if (token == JsonToken.VALUE_STRING) {
                dateTimeZone = DateTimeZone.forID(jsonParser.getValueAsString());
            } else {
                throw deserializationContext.mappingException("Invalid type, must be String: " + token);
            }
        } catch (Exception e) {
            throw deserializationContext.mappingException("Invalid ObjectId: " + token);
        }

        return dateTimeZone;
    }

}
