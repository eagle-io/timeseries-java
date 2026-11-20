package io.eagle.util.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.net.MediaType;
import io.eagle.util.jts.DocumentFormat;
import io.eagle.util.jts.JtsProfile;
import io.eagle.util.jts.JtsProfileSerializer;
import io.eagle.util.jts.JtsTableSerializer;
import org.bson.types.ObjectId;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.json.JsonWriteFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.datatype.joda.JodaModule;
import tools.jackson.module.kotlin.KotlinModule;

import java.util.HashMap;


/**
 * The eagle.io Jackson {@link JsonMapper}.
 * <p>
 * Jackson 3 mappers are immutable: use {@link #builder()} to customise a mapper with the eagle.io configuration (for example from a
 * Spring {@code JsonMapperBuilderCustomizer}), and {@link #withSerializer(ValueSerializer)} to derive a new mapper with an extra serializer.
 * <p>
 * The configuration starts from {@link JsonMapper.Builder#configureForJackson2()} so the JSON produced and accepted is the same as the
 * Jackson 2 based io.eagle:timeseries 1.x releases.
 */
public class EagleObjectMapper extends JsonMapper {

    private static final long serialVersionUID = 2L;


    public EagleObjectMapper() {
        this(builder());
    }


    protected EagleObjectMapper(JsonMapper.Builder builder) {
        super(builder);
    }


    /**
     * Returns a new {@link JsonMapper.Builder} with the eagle.io configuration, modules and custom (de)serializers applied.
     */
    public static JsonMapper.Builder builder() {
        return configure(JsonMapper.builder());
    }


    /**
     * Applies the eagle.io configuration, modules and custom (de)serializers to the given builder.
     */
    public static JsonMapper.Builder configure(JsonMapper.Builder builder) {
        return builder
                // Keep Jackson 2.x defaults (property order, null/trailing token handling, enum and date formats, ...)
                .configureForJackson2()
                // ...which does not cover these Jackson 2.x defaults
                .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .disable(JsonWriteFeature.COMBINE_UNICODE_SURROGATES_IN_UTF8)

                // Set visibility on all field scopes, meaning that private fields can be identified by Jackson
                .changeDefaultVisibility(vc -> vc
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.ANY))

                // TODO: we need this for CampbellPakbus Tabledefs to be de/serialized correctly; why?
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

                .enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS)

                // Serialize Joda DateTime as ISO format string (instead of a millisecond timestamp)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)

                // As Node models now may contain beans, we need to disable bean serialization
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

                .addModule(new JodaModule())
                .addModule(new KotlinModule.Builder().build())
                .addModule(new SimpleModule("EagleObjectMapper")
                        .addSerializer(ObjectId.class, new ObjectIdSerializer())
                        .addDeserializer(ObjectId.class, new ObjectIdDeserializer())
                        .addSerializer(DateTimeZone.class, new DateTimeZoneSerializer())
                        .addDeserializer(DateTimeZone.class, new DateTimeZoneDeserializer())
                        .addSerializer(Period.class, new PeriodSerializer())
                        .addDeserializer(Period.class, new PeriodDeserializer())
                        .addSerializer(MediaType.class, new MediaTypeSerializer())
                        .addDeserializer(MediaType.class, new MediaTypeDeserializer())
                        .addSerializer(new JtsTableSerializer(new HashMap<>(), DocumentFormat.JSON_STANDARD, DateTimeZone.UTC))
                        .addSerializer(JtsProfile.class, new JtsProfileSerializer()));
    }


    /**
     * Returns a new mapper with the same configuration as this one, plus the given serializer (which takes precedence over existing
     * serializers for the same type). This mapper is not modified.
     */
    public EagleObjectMapper withSerializer(final ValueSerializer<?> serializer) {
        return new EagleObjectMapper(rebuild().addModule(new SimpleModule().addSerializer(serializer)));
    }

}
