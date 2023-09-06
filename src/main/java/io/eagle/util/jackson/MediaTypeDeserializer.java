package io.eagle.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.google.common.net.MediaType;


public final class MediaTypeDeserializer extends StdScalarDeserializer<MediaType> {
    /**
     * Default serial version ID
     */
    private static final long serialVersionUID = 1L;

    public MediaTypeDeserializer() {
        super(MediaType.class);
    }

    @Override
    public MediaType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JsonMappingException {
        MediaType mimeType = null;
        JsonToken token = jsonParser.getCurrentToken();

        try {
            // The JSON token must be a String
            if (token == JsonToken.VALUE_STRING) {
                mimeType = MediaType.parse(jsonParser.getValueAsString());
            } else {
                throw deserializationContext.mappingException("Invalid type, must be String: " + token);
            }
        } catch (Exception e) {
            throw deserializationContext.mappingException("Invalid ObjectId: " + token);
        }

        return mimeType;
    }

}
