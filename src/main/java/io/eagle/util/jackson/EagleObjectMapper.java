package io.eagle.util.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.google.common.net.MediaType;
import io.eagle.util.jts.DocumentFormat;
import io.eagle.util.jts.JtsProfile;
import io.eagle.util.jts.JtsProfileSerializer;
import io.eagle.util.jts.JtsTableSerializer;
import org.bson.types.ObjectId;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;


public class EagleObjectMapper extends ObjectMapper {
    private static final Logger logger = LoggerFactory.getLogger(EagleObjectMapper.class);

    private static final long serialVersionUID = 1L;


    public EagleObjectMapper() {
        // Set visibility on all field scopes, meaning that private fields can be identified by Jackson
        this.setVisibility(this.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.ANY));

        // Disable the FAIL_ON_UNKNOWN_PROPERTIES feature
        // TODO: we need this for CampbellPakbus Tabledefs to be de/serialized correctly; why?
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Allow leading zeros
        this.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);

        // Use this setting to serialize Joda DateTime as ISO format string (instead of a millisecond timestamp)
        this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // As Node models now may contain beans, we need to disable bean serialization
        this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Register the JodaModule, which is required to correctly de/serialize Joda DateTime objects
        this.registerModule(new JodaModule());
        this.registerModule(new Jdk8Module());
        this.registerModule( new KotlinModule() );

//        try {
//            for (Class<? extends ComplexValue> subType : new Reflections().getSubTypesOf(ComplexValue.class)) {
//                this.registerSubtypes(new NamedType(subType, subType.newInstance().getTypeId()));
//            }
//        } catch (InstantiationException | IllegalAccessException e) {
//            throw new RuntimeException(e.getMessage(), e);
//        }

        // Register custom serializers/deserializers
        this.registerModule(new SimpleModule() {

            private static final long serialVersionUID = 1L;


            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);

                SimpleSerializers serializers = new SimpleSerializers();
                SimpleDeserializers deserializers = new SimpleDeserializers();

                serializers.addSerializer(ObjectId.class, new ObjectIdSerializer());
                deserializers.addDeserializer(ObjectId.class, new ObjectIdDeserializer());

                serializers.addSerializer(DateTimeZone.class, new DateTimeZoneSerializer());
                deserializers.addDeserializer(DateTimeZone.class, new DateTimeZoneDeserializer());

                serializers.addSerializer(Period.class, new PeriodSerializer());
                deserializers.addDeserializer(Period.class, new PeriodDeserializer());

                serializers.addSerializer(MediaType.class, new MediaTypeSerializer());
                deserializers.addDeserializer(MediaType.class, new MediaTypeDeserializer());

                serializers.addSerializer(new JtsTableSerializer(new HashMap<Integer, String>(), DocumentFormat.JSON_STANDARD, DateTimeZone.UTC));
                serializers.addSerializer(JtsProfile.class, new JtsProfileSerializer());

                context.addSerializers(serializers);
                context.addDeserializers(deserializers);
            }
        });
    }


    public EagleObjectMapper withSerializer(final JsonSerializer<?> serializer) {
        this.registerModule(new SimpleModule() {

            private static final long serialVersionUID = 1L;


            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);

                SimpleSerializers serializers = new SimpleSerializers();

                serializers.addSerializer(serializer);

                context.addSerializers(serializers);
            }
        });

        return this;
    }

}
