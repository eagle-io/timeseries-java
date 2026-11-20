package io.eagle.util.jackson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;


public class JacksonUtil {


    private static final Logger logger = LoggerFactory.getLogger(JacksonUtil.class);

    private static final ObjectMapper EAGLE_OBJECT_MAPPER = new EagleObjectMapper();

    /**
     * Builds and returns a Jackson {@link ObjectMapper}.
     */
    public static ObjectMapper getObjectMapper() {
        return EAGLE_OBJECT_MAPPER;
    }


    /**
     * Converts an Object to a JSON string.
     *
     * @param object the Object to convert to a JSON string
     * @return JSON string representing the serialized object
     */
    public static String toJson(Object object, Class<?> view) {
        String json = null;

        try {
            if (view == null)
                json = getObjectMapper().writeValueAsString(object);
            else
                json = getObjectMapper().writerWithView(view).writeValueAsString(object);
        } catch (JacksonException e) {
            logger.error("COULD NOT CONVERT OBJECT TO JSON [{}]", object.getClass().getName(), e);
        }

        return json;
    }


    public static String toJson(Object object) {
        return toJson(object, (Class<?>) null);
    }


    public static String toJson(Object object, String... excludeFields) {
        Map<String, Object> objMap = JacksonUtil.getObjectMapper().convertValue(object, new TypeReference<Map<String, Object>>() {
        });

        for (String field : excludeFields) {
            objMap.remove(field);
        }

        return toJson(objMap);
    }


    /**
     * Converts an Object to a JSON string.
     *
     * @param object the Object to convert to a JSON string
     * @return JSON string representing the serialized object
     */
    public static String toPrettyJson(Object object) {
        String json = null;

        try {
            json = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JacksonException e) {
            logger.error("COULD NOT CONVERT OBJECT TO JSON [{}]", object.getClass().getName(), e);
        }

        return json;
    }


    /**
     * Converts a JSON string to an Object.
     *
     * @param json the JSON string to convert into an Object
     * @return the deserialized object
     */
    public static <T> T parseObject(String json, Class<T> valueType) {
        T object = null;

        try {
            object = getObjectMapper().readValue(json, valueType);
        } catch (JacksonException e) {
            logger.error("COULD NOT CONVERT JSON TO {}: {}", valueType.getSimpleName(), e);
        }

        return object;
    }


    /**
     * Converts a JSON string to an Object.
     *
     * @param json the JSON string to convert into an Object
     * @return the deserialized object
     */
    public static <T> T parseObject(String json, TypeReference<T> valueType) {
        T object = null;

        try {
            object = getObjectMapper().readValue(json, valueType);
        } catch (JacksonException e) {
            logger.error("COULD NOT CONVERT JSON TO {}: {}", valueType.getClass().getName(), e);
        }

        return object;
    }


    /**
     * Converts a JSON string to a {@code Map<String,Object>}.
     *
     * @param json the JSON string to convert into a map
     * @return the deserialized object
     */
    public static Map<String, Object> parseMap(String json) {
        Map<String, Object> object = null;

        try {
            object = getObjectMapper().readValue(json, new TypeReference<HashMap<String, Object>>() {
            });
        } catch (JacksonException e) {
            logger.error("COULD NOT CONVERT JSON TO MAP", e);
        }

        return object;
    }

}
