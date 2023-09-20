package io.eagle.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Jackson deserializer to deserialize a JSON token to a BSON {@link ObjectId}.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 * @see JacksonUtil
 * @see ObjectIdSerializer
 */
public final class ObjectIdDeserializer extends StdScalarDeserializer<ObjectId> {


    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ObjectIdDeserializer.class);

    /**
     * Default serial version ID
     */
    private static final long serialVersionUID = 1L;


    /**
     * Default constructor; declares to the super constructor that the {@link ObjectId} class will be deserialized by this deserializer.
     */
    public ObjectIdDeserializer() {
        super(ObjectId.class);
    }


    /**
     * Deserializes the current JSON token to a BSON {@link ObjectId}.
     *
     * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser,
     * com.fasterxml.jackson.databind.DeserializationContext)
     */
    @Override
    public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JsonMappingException {
        ObjectId objectId = null;
        JsonToken token = jsonParser.getCurrentToken();

        try {
            // The JSON token must be a String
            if (token == JsonToken.VALUE_STRING) {
                objectId = new ObjectId(jsonParser.getValueAsString());
            } else {
                throw deserializationContext.mappingException("Invalid type, must be String: " + token);
            }
        } catch (Exception e) {
            throw deserializationContext.mappingException("Invalid ObjectId: " + token);
        }

        return objectId;
    }

}
