package io.eagle.util.jts;

import io.eagle.util.geo.Coordinates;
import io.eagle.util.jackson.JacksonUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JtsFieldTest {
    private static final Logger logger = LoggerFactory.getLogger( JtsFieldTest.class );

    @Test
    void testCoordinatesSerialization() {
        String json = JacksonUtil.toJson(new Coordinates(35.6, 155.1));
        logger.info("JSON: {}", json);
//        ComplexValue coords = JacksonUtil.parseObject(json, ComplexValue.class);
//        logger.info("OBJ: {}, class: {}", coords, coords.getClass());
    }

    @Test
    void testJtsFieldSerialization() {
        JtsField field = JtsField.of(new Coordinates(35.6, 115.2));
        //logger.info("FIELD: {}", field);
        String json = JacksonUtil.toJson(field);
        logger.info("JSON: {}", json);
//        field = JacksonUtil.parseObject(json, JtsField.class);
//        logger.info("OBJ: {}", field);
    }


}
