package io.eagle.util.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
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
    public MediaType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws DatabindException {
        MediaType mimeType = null;
        JsonToken token = jsonParser.currentToken();

        try {
            // The JSON token must be a String
            if (token == JsonToken.VALUE_STRING) {
                mimeType = MediaType.parse(jsonParser.getValueAsString());
            } else {
                throw DatabindException.from(jsonParser, "Invalid type, must be String: " + token);
            }
        } catch (Exception e) {
            throw DatabindException.from(jsonParser, "Invalid ObjectId: " + token);
        }

        return mimeType;
    }

}
