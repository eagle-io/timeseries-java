package io.eagle.util.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdScalarSerializer;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Jackson serializer which serializes a BSON {@link MediaType} to JSON.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 * @see JacksonUtil
 * @see ObjectIdDeserializer
 */
public final class MediaTypeSerializer extends StdScalarSerializer<MediaType> {

    private static final long serialVersionUID = 4435689674092222020L;


    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(MediaTypeSerializer.class);


    /**
     * Default constructor; declares to the super constructor that the {@link MediaType} class will be serialized by this serializer.
     */
    public MediaTypeSerializer() {
        super(MediaType.class);
    }


    @Override
    public void serialize(MediaType value, JsonGenerator jgen, SerializationContext provider) {
        jgen.writeString(value.toString());
    }

}
