package io.eagle.util.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdScalarSerializer;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Jackson serializer which serializes a BSON {@link ObjectId} to JSON.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 * @see JacksonUtil
 * @see ObjectIdDeserializer
 */
public final class ObjectIdSerializer extends StdScalarSerializer<ObjectId> {

    private static final long serialVersionUID = 7125119464552703465L;


    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ObjectIdSerializer.class);


    /**
     * Default constructor; declares to the super constructor that the {@link ObjectId} class will be serialized by this serializer.
     */
    public ObjectIdSerializer() {
        super(ObjectId.class);
    }


    /**
     * Serializes the given {@link ObjectId} to JSON.
     *
     * @see tools.jackson.databind.ValueSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator,
     * tools.jackson.databind.SerializationContext)
     */
    @Override
    public void serialize(ObjectId value, JsonGenerator jgen, SerializationContext provider) {
        jgen.writeString(value.toString());
    }

}
