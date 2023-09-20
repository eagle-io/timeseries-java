package io.eagle.util.jts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.eagle.util.geo.Coordinates;
import io.eagle.util.jackson.JacksonUtil;
import io.eagle.util.jts.complex.ComplexValue;
import io.eagle.util.jts.complex.Metrics;
import io.eagle.util.jts.complex.Time;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ComplexValueTest {
    private static final Logger logger = LoggerFactory.getLogger( ComplexValueTest.class );

    private static ObjectMapper mapper = JacksonUtil.getObjectMapper();

    @Test
    void testCoordinates() {
        ComplexValue value = new Coordinates(36.1, 151.2);
        String json = JacksonUtil.toJson(value);
        logger.info("JSON value: {}", json);
        assertEquals(value, JacksonUtil.parseObject(json, ComplexValue.class));

        JtsField field = JtsField.of(value);
        json = JacksonUtil.toJson(field);
        logger.info("JSON field: {}", json);
        assertEquals(field, JacksonUtil.parseObject(json, JtsField.class));
    }

    @Test
    void testTime() {
        ComplexValue value;
        String json;
        JtsField field;

        value = new Time(DateTime.now(DateTimeZone.UTC));
        json = JacksonUtil.toJson(value);
        logger.info("JSON time: {}", json);
        assertEquals(value, JacksonUtil.parseObject(json, ComplexValue.class));

        field = JtsField.of(value);
        json = JacksonUtil.toJson(field);
        logger.info("JSON field: {}", json);
        assertEquals(field, JacksonUtil.parseObject(json, JtsField.class));

        value = new Time(947529474L);
        json = JacksonUtil.toJson(value);
        logger.info("JSON time: {}", json);
        assertEquals(value, JacksonUtil.parseObject(json, ComplexValue.class));

        field = JtsField.of(value);
        json = JacksonUtil.toJson(field);
        logger.info("JSON field: {}", json);
        assertEquals(field, JacksonUtil.parseObject(json, JtsField.class));
    }

    @Test
    void testMetrics() {
        ComplexValue value = new Metrics(Arrays.asList(34, 55.2, 99.7));
        String json = JacksonUtil.toJson(value);
        logger.info("JSON: {}", json);
        assertEquals(value, JacksonUtil.parseObject(json, ComplexValue.class));

        JtsField field = JtsField.of(value);
        json = JacksonUtil.toJson(field);
        logger.info("JSON field: {}", json);
        assertEquals(field, JacksonUtil.parseObject(json, JtsField.class));
    }

    @Test
    void testDocumentSerialization() throws JsonProcessingException {
        JtsTable<?> table = new JtsTable<>();
        DateTime ts = DateTime.now(DateTimeZone.UTC);
        table.put(ts, 0, JtsField.of(new Coordinates(35.1, 151.2)));
        table.put(ts, 1, JtsField.of(new Time(DateTime.now(DateTimeZone.UTC))));
        table.put(ts, 2, JtsField.of(new Metrics(Arrays.asList(34,23.6, 999))));
        table.put(ts, 3, JtsField.of(56.8));
        table.put(ts, 4, JtsField.of("cheese"));
        table.put(ts, 5, JtsField.of(true));

        String json = table.toJson();
        logger.info("JSON table: {}", json);
        assertEquals(table, JacksonUtil.parseObject(json, JtsDocument.class).getTable());

    }

}
