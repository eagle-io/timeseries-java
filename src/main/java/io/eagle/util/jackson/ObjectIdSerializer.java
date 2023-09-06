package io.eagle.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Jackson serializer which serializes a BSON {@link ObjectId} to JSON.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 * @see JacksonUtil
 * @see EagleModule
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
     * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator,
     * com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(ObjectId value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }

}
