package io.eagle.util.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
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
     * @see tools.jackson.databind.ValueDeserializer#deserialize(tools.jackson.core.JsonParser,
     * tools.jackson.databind.DeserializationContext)
     */
    @Override
    public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws DatabindException {
        ObjectId objectId = null;
        JsonToken token = jsonParser.currentToken();

        try {
            // The JSON token must be a String
            if (token == JsonToken.VALUE_STRING) {
                objectId = new ObjectId(jsonParser.getValueAsString());
            } else {
                throw DatabindException.from(jsonParser, "Invalid type, must be String: " + token);
            }
        } catch (Exception e) {
            throw DatabindException.from(jsonParser, "Invalid ObjectId: " + token);
        }

        return objectId;
    }

}
