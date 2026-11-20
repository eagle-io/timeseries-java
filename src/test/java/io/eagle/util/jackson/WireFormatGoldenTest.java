package io.eagle.util.jackson;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;
import io.eagle.util.AggregateType;
import io.eagle.util.BaselineType;
import io.eagle.util.DataType;
import io.eagle.util.DisplayType;
import io.eagle.util.geo.Coordinates;
import io.eagle.util.jts.*;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden tests pinning the exact JSON produced and accepted by {@link EagleObjectMapper}, {@link JacksonUtil} and the JTS types.
 * <p>
 * The golden files under {@code src/test/resources/golden} were generated with io.eagle:timeseries 1.0.33 (Jackson 2.15) and must stay
 * byte-identical, because this JSON is exchanged with the frontend, RabbitMQ consumers and stored data.
 * <p>
 * Set the environment variable {@code GOLDEN_UPDATE=true} to (re)write the golden files instead of comparing against them.
 */
class WireFormatGoldenTest {

    private static final Path GOLDEN_DIR = Paths.get("src/test/resources/golden");
    private static final boolean UPDATE = Boolean.parseBoolean(System.getenv("GOLDEN_UPDATE"));

    static final DateTime T0 = new DateTime(2023, 3, 14, 1, 59, 26, 535, DateTimeZone.UTC);
    static final String TRICKY_TEXT = "a/b \\ \"quoted\" <tag> & é ✓ 😀 \t\n end";

    static JtsTable<?> table() {
        JtsTable<?> table = new JtsTable<>();
        DateTime t1 = T0, t2 = T0.plusMinutes(5), t3 = T0.plusHours(1);

        table.put(t1, 0, new JtsField(12.345, 0, "first"));
        table.put(t1, 1, JtsField.of(TRICKY_TEXT));
        table.put(t1, 2, JtsField.of(new Coordinates(-33.8688, 151.2093)));
        table.put(t1, 3, JtsField.of(new Time(T0.minusDays(1))));
        table.put(t1, 4, JtsField.of(new Metrics(Arrays.asList(1, 2.5, -3))));

        table.put(t2, 5, JtsField.of(true));
        table.put(t2, 1, new JtsField(null, 192));
        table.put(t2, 3, JtsField.of(new Time(1234567890123L)));

        table.put(t3, 6, new JtsField(-0.000123456789, 65536 + 3, "annotated", T0.plusDays(2)));
        table.put(t3, 1, JtsField.of(""));
        table.put(t3, 2, JtsField.DELETE_FIELD);
        table.put(t3, 0, new JtsField(1e21));
        return table;
    }

    static JtsColumnHeader columnHeader(int i) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "logger/" + i);
        metadata.put("nullValue", null);
        metadata.put("number", 42);
        return new JtsColumnHeader.Builder()
                .id("col" + i)
                .name("Column " + i)
                .dataType(DataType.NUMBER)
                .aggregate(AggregateType.AVERAGE)
                .interval(Period.parse("P1Y2M3DT4H5M6S"))
                .baseTime("D+1H")
                .format("0.##")
                .renderType(DisplayType.VALUE)
                .units("m/s")
                .metrics("min,max")
                .baselineType(BaselineType.values()[0])
                .baselineTime(T0.withZone(DateTimeZone.forID("Australia/Sydney")))
                .metadata(metadata)
                .build();
    }

    static JtsDocumentHeader header() {
        SortedMap<Integer, JtsColumnHeader> columns = new TreeMap<>();
        columns.put(0, columnHeader(0));
        columns.put(1, new JtsColumnHeader.Builder().id("col1").name("Text").dataType(DataType.TEXT).build());
        columns.put(4, new JtsColumnHeader.Builder().name("Metrics").format("0.0").build());
        return new JtsDocumentHeader.Builder()
                .id("5a1b2c3d4e5f60718293a4b5")
                .name("Table 1")
                .startTime(T0)
                .endTime(T0.plusHours(1))
                .recordCount(3)
                .columns(columns)
                .metadata(ImmutableMap.of("site", "Sydney", "n", 1))
                .build();
    }

    static JtsDocument document() {
        return new JtsDocument(table(), header());
    }

    static JtsProfile profile() {
        DateTime t1 = T0, t2 = T0.plusMinutes(10);
        JtsProfile profile = new JtsProfile(Arrays.asList(0, 1, 2), Arrays.asList(t1, t2));
        profile.put(0, t1, 499.345, 45.0);
        profile.put(0, t2, 499.132, 46.5);
        profile.put(1, t1, 999.567, null);
        profile.put(1, t2, 999.945, 1.0 / 3);
        profile.put(2, t1, 1499.345, 47.0);
        profile.put(2, t2, null, 48.0);
        return profile;
    }

    enum Colour {RED, GREEN}

    @SuppressWarnings("unused")
    static class Pojo {
        static String STATIC_FIELD = "ignored";
        private transient String transientField = "ignored";
        private String text = TRICKY_TEXT;
        private String nullText = null;
        private int primitiveInt = 7;
        private long primitiveLong = 9007199254740993L;
        private double primitiveDouble = 0.1;
        private boolean flag = true;
        private char letter = 'x';
        private Double boxedDouble = 1.0E-7;
        private Integer boxedNull = null;
        private java.math.BigDecimal decimal = new java.math.BigDecimal("12345678901234567890.000100");
        private byte[] bytes = "bytes".getBytes(StandardCharsets.UTF_8);
        private Colour colour = Colour.GREEN;
        private DataType dataType = DataType.COORDINATES;
        private ObjectId objectId = new ObjectId("5a1b2c3d4e5f60718293a4b5");
        private DateTimeZone zone = DateTimeZone.forID("Australia/Brisbane");
        private Period period = Period.parse("P1Y2M3W4DT5H6M7.008S");
        private Period shortPeriod = Period.minutes(15);
        private MediaType mediaType = MediaType.parse("text/csv; charset=utf-8");
        private DateTime utcTime = T0;
        private DateTime zonedTime = T0.withZone(DateTimeZone.forOffsetHours(10));
        private LocalDate localDate = new LocalDate(2023, 3, 14);
        private org.joda.time.LocalDateTime localDateTime = new org.joda.time.LocalDateTime(2023, 3, 14, 12, 30, 0, 0);
        private org.joda.time.Duration duration = org.joda.time.Duration.standardMinutes(90);
        private Date date = T0.toDate();
        private Optional<String> optionalPresent = Optional.of("present");
        private Optional<String> optionalEmpty = Optional.empty();
        private Optional<String> optionalNull = null;
        private List<Object> list = Arrays.asList(1, "two", 3.0, null, Arrays.asList());
        private Map<String, Object> map = new LinkedHashMap<>();
        private Map<Integer, String> intKeyMap = new TreeMap<>(ImmutableMap.of(2, "two", 10, "ten"));
        private Map<DateTime, String> dateKeyMap = ImmutableMap.of(T0, "t0");
        private Object emptyBean = new Object();
        private JtsField field = new JtsField(3.5, 1, "a");
        private Coordinates coordinates = new Coordinates(1, 2);

        Pojo() {
            map.put("z", 1);
            map.put("a", null);
            map.put("m", Collections.emptyMap());
        }

        public String getComputed() {
            return "getters are not serialized";
        }

        public boolean isComputed() {
            return true;
        }
    }

    @SuppressWarnings("unused")
    static class Primitives {
        private int intValue = -1;
        private boolean boolValue = true;
        private double doubleValue = -1;
        private Colour colour;
        private String text;
    }

    private static String bytes(Object value) throws Exception {
        return new String(JacksonUtil.getObjectMapper().writeValueAsBytes(value), StandardCharsets.UTF_8);
    }

    private static String describe(Object value) {
        if (value instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            ((Map<?, ?>) value).forEach((k, v) -> sb.append(k).append('=').append(describe(v)).append(';'));
            return sb.append('}').toString();
        } else if (value instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            ((List<?>) value).forEach(v -> sb.append(describe(v)).append(';'));
            return sb.append(']').toString();
        } else {
            return value == null ? "null" : value.getClass().getSimpleName() + ":" + value;
        }
    }

    private static String describePrimitives(Primitives p) {
        return p == null ? "null" : p.intValue + "," + p.boolValue + "," + p.doubleValue + "," + p.colour + "," + p.text;
    }

    private static Map<String, Callable<String>> cases() {
        Map<String, Callable<String>> cases = new LinkedHashMap<>();

        // --- JacksonUtil / EagleObjectMapper serialization ---
        cases.put("field-double", () -> JacksonUtil.toJson(JtsField.of(12.5)));
        cases.put("field-full", () -> JacksonUtil.toJson(new JtsField(12.5, 3, "note", T0)));
        cases.put("field-empty", () -> JacksonUtil.toJson(new JtsField()));
        cases.put("field-delete", () -> JacksonUtil.toJson(JtsField.DELETE_FIELD));
        cases.put("field-cleared-value", () -> {
            JtsField field = new JtsField(1.0, 2, "x");
            field.setValue(null);
            field.setAnnotation(null);
            return JacksonUtil.toJson(field);
        });
        cases.put("field-string", () -> JacksonUtil.toJson(JtsField.of(TRICKY_TEXT)));
        cases.put("field-boolean", () -> JacksonUtil.toJson(JtsField.of(false)));
        cases.put("field-coordinates", () -> JacksonUtil.toJson(JtsField.of(new Coordinates(-33.8688, 151.2093))));
        cases.put("field-time", () -> JacksonUtil.toJson(JtsField.of(new Time(T0))));
        cases.put("field-datetime", () -> JacksonUtil.toJson(JtsField.of(T0.withZone(DateTimeZone.forOffsetHours(10)))));
        cases.put("field-metrics", () -> JacksonUtil.toJson(JtsField.of(new Metrics(Arrays.asList(1, 2.5)))));
        cases.put("complex-coordinates", () -> JacksonUtil.toJson(new Coordinates(1.5, -2)));
        cases.put("complex-time", () -> JacksonUtil.toJson(new Time(T0.withZone(DateTimeZone.forOffsetHours(-5)))));
        cases.put("complex-metrics", () -> JacksonUtil.toJson(new Metrics(Arrays.asList(1, 2.5))));
        cases.put("record", () -> JacksonUtil.toJson(table().getFirstRecord()));
        cases.put("table", () -> JacksonUtil.toJson(table()));
        cases.put("table-empty", () -> JacksonUtil.toJson(new JtsTable<>()));
        cases.put("sample", () -> JacksonUtil.toJson(new JtsSample(T0, 3.0, 5)));
        cases.put("series-change", () -> JacksonUtil.toJson(new SeriesChange()));
        cases.put("quality", () -> JacksonUtil.toJson(new Quality(17)));
        cases.put("column-header", () -> JacksonUtil.toJson(columnHeader(0)));
        cases.put("column-header-tojson", () -> columnHeader(0).toJson());
        cases.put("column-header-pretty", () -> columnHeader(0).toPrettyJson());
        cases.put("document-header", () -> JacksonUtil.toJson(header()));
        cases.put("document-header-tojson", () -> header().toJson());
        cases.put("document-header-tostring", () -> header().toString());
        cases.put("document-format", () -> JacksonUtil.toJson(DocumentFormat.CSV));
        cases.put("document-format-tostring", () -> new DocumentFormat(DocumentFormat.CSV).toString());
        cases.put("document-mapper", () -> JacksonUtil.toJson(document()));
        cases.put("document-mapper-view", () -> JacksonUtil.toJson(document(), JtsViews.DocumentAndHeaderAndData.class));
        cases.put("document-mapper-view-data", () -> JacksonUtil.toJson(document(), JtsViews.Data.class));
        cases.put("document-mapper-exclude", () -> JacksonUtil.toJson(header(), "columns", "metadata"));
        cases.put("profile", () -> JacksonUtil.toJson(profile()));
        cases.put("profile-document", () -> new JtsDocument(profile(), null, DocumentSubType.PROFILE).toJson());
        cases.put("pojo", () -> JacksonUtil.toJson(new Pojo()));
        cases.put("pojo-bytes", () -> bytes(new Pojo()));
        cases.put("pojo-pretty", () -> JacksonUtil.toPrettyJson(new Pojo()));
        cases.put("pojo-view", () -> JacksonUtil.toJson(new Pojo(), JtsViews.Data.class));
        cases.put("map-pretty", () -> JacksonUtil.toPrettyJson(ImmutableMap.of("a", Collections.emptyMap(), "b", Collections.emptyList(),
                "c", Arrays.asList(1, 2), "d", ImmutableMap.of("e", "f"))));

        // --- JtsDocument API ---
        cases.put("document-tojson", () -> document().toJson());
        cases.put("document-tojson-pretty", () -> document().toJsonPretty());
        cases.put("document-tojson-pretty-repeated", () -> document().toJsonPretty() + "\n----\n" + document().toJsonPretty());
        cases.put("document-tostring", () -> document().toString());
        cases.put("document-tojson-pretty-zone", () -> document().toJson(new DocumentFormat(DocumentFormat.JSON), DateTimeZone.forOffsetHours(10)));
        cases.put("document-tojson-noheader", () -> document().toJson(DocumentFormat.JSON_STANDARD.withHeaderEnabled(false), DateTimeZone.UTC));
        cases.put("document-tojson-noheader-pretty", () -> document().toJson(DocumentFormat.JSON.withHeaderEnabled(false), DateTimeZone.UTC));
        cases.put("document-tojson-windrose", () -> new JtsDocument(table(), header(), DocumentSubType.WINDROSE).toJson());
        cases.put("document-tojson-windrose-pretty", () -> new JtsDocument(table(), header(), DocumentSubType.WINDROSE).toJsonPretty());
        cases.put("document-tojson-empty", () -> new JtsDocument(new JtsTable<>()).toJson());
        cases.put("document-tojson-empty-pretty", () -> new JtsDocument(new JtsTable<>(), header()).toJsonPretty());
        cases.put("document-tojson-nodata", () -> new JtsDocument().toJson());
        cases.put("table-tojson", () -> table().toJson());
        cases.put("document-tocsv", () -> document().toCsv());
        for (DocumentFormat format : Arrays.asList(DocumentFormat.JSON, DocumentFormat.JSON_STANDARD)) {
            String name = format.getFormatType().name().toLowerCase();
            cases.put("render-" + name, () -> document().renderHeader(new DocumentFormat(format), DateTimeZone.UTC)
                    + document().renderRecords(new DocumentFormat(format), DateTimeZone.UTC, true)
                    + document().renderRecords(new DocumentFormat(format), DateTimeZone.forOffsetHours(10), false)
                    + document().renderFooter(new DocumentFormat(format)));
        }

        // --- Deserialization: parse, then re-serialize ---
        cases.put("parse-document-standard", () -> JtsDocument.readJsonFromString(document().toJson()).toJson());
        cases.put("parse-document-pretty", () -> new JtsDocument(document().toJsonPretty()).toJson());
        cases.put("parse-document-zoned", () -> new JtsDocument(
                document().toJson(new DocumentFormat(DocumentFormat.JSON), DateTimeZone.forOffsetHours(10))).toJsonPretty());
        cases.put("parse-document-windrose", () -> JtsDocument.readJsonFromString(
                new JtsDocument(table(), header(), DocumentSubType.WINDROSE).toJson()).toJson());
        cases.put("parse-document-client", () -> JtsDocument.readJsonFromString("{\"docType\":\"jts\",\"version\":\"1.0\",\"unknown\":{\"x\":1},"
                + "\"header\":{\"columns\":{\"0\":{\"id\":\"a\",\"dataType\":\"NUMBER\",\"interval\":\"1MO2D\",\"extra\":true}},\"recordCount\":007},"
                + "\"data\":[{\"ts\":\"2023-03-14T11:59:26.535+10:00\",\"f\":{\"0\":{\"v\":1,\"q\":2,\"a\":\"x\"},\"1\":{\"v\":\"s\"}}},"
                + "{\"ts\":1678759166535,\"f\":{\"0\":null,\"2\":{\"v\":{\"$coords\":[1,2]}}}},"
                + "{\"ts\":{\"$millis\":1678759266535},\"f\":{\"3\":{\"v\":{\"$time\":\"2023-03-14T01:59:26.535Z\"}},\"4\":{\"v\":{\"$metrics\":[1,2]}}}},"
                + "{\"ts\":{\"$time\":\"2023-03-14T02:59:26.535Z\"},\"f\":{\"3\":{\"v\":{\"$millis\":1}},\"5\":{\"v\":true}}}]}").toJsonPretty());
        cases.put("parse-field", () -> {
            JtsField field = JacksonUtil.parseObject("{\"v\":{\"$coords\":[1,2]},\"q\":3,\"a\":\"x\",\"m\":\"2023-03-14T01:59:26.535Z\",\"z\":1}", JtsField.class);
            return field + " " + describe(field.getValue()) + " " + JacksonUtil.toJson(field);
        });
        cases.put("parse-field-number", () -> {
            JtsField field = JacksonUtil.parseObject("{\"v\":12}", JtsField.class);
            return field + " " + describe(field.getValue()) + " " + JacksonUtil.toJson(field);
        });
        cases.put("parse-complex", () -> describe(JacksonUtil.parseObject("{\"$time\":\"2023-03-14T11:59:26.535+10:00\"}", ComplexValue.class))
                + " " + describe(JacksonUtil.parseObject("{\"$millis\":1678759166535}", ComplexValue.class))
                + " " + describe(JacksonUtil.parseObject("{\"$metrics\":[1,2.5]}", ComplexValue.class)));
        cases.put("parse-column-header", () -> JacksonUtil.toJson(JacksonUtil.parseObject(columnHeader(0).toJson(), JtsColumnHeader.class)));
        cases.put("parse-document-header", () -> JacksonUtil.toJson(JacksonUtil.parseObject(header().toJson(), JtsDocumentHeader.class)));
        cases.put("parse-document-format", () -> JacksonUtil.toJson(JacksonUtil.parseObject(JacksonUtil.toJson(DocumentFormat.CSV), DocumentFormat.class)));
        cases.put("parse-sample", () -> JacksonUtil.toJson(JacksonUtil.parseObject(JacksonUtil.toJson(new JtsSample(T0, 3.0, 5)), JtsSample.class)));
        cases.put("parse-series-change", () -> JacksonUtil.toJson(JacksonUtil.parseObject(
                "{\"insertedRecords\":3,\"modifiedValues\":null,\"other\":1}", SeriesChange.class)));
        cases.put("parse-pojo", () -> JacksonUtil.toJson(JacksonUtil.parseObject(JacksonUtil.toJson(new Pojo()), Pojo.class)));
        cases.put("parse-primitives-null", () -> describePrimitives(JacksonUtil.parseObject(
                "{\"intValue\":null,\"boolValue\":null,\"doubleValue\":null,\"colour\":null,\"text\":null}", Primitives.class)));
        cases.put("parse-primitives-coercion", () -> describePrimitives(JacksonUtil.parseObject(
                "{\"intValue\":\"12\",\"boolValue\":\"false\",\"doubleValue\":\"1.5\",\"colour\":\"RED\",\"text\":5}", Primitives.class)));
        cases.put("parse-primitives-float-to-int", () -> describePrimitives(JacksonUtil.parseObject("{\"intValue\":1.0}", Primitives.class)));
        cases.put("parse-primitives-empty-string", () -> describePrimitives(JacksonUtil.parseObject(
                "{\"intValue\":\"\",\"doubleValue\":\"\",\"colour\":\"\"}", Primitives.class)));
        cases.put("parse-enum-ordinal", () -> describePrimitives(JacksonUtil.parseObject("{\"colour\":1}", Primitives.class)));
        cases.put("parse-enum-unknown", () -> describePrimitives(JacksonUtil.parseObject("{\"colour\":\"BLUE\"}", Primitives.class)));
        cases.put("parse-trailing-tokens", () -> describePrimitives(JacksonUtil.parseObject("{\"intValue\":3} {\"intValue\":4}", Primitives.class)));
        cases.put("parse-leading-zeros", () -> describePrimitives(JacksonUtil.parseObject("{\"intValue\":0012}", Primitives.class)));
        cases.put("parse-map", () -> describe(JacksonUtil.parseMap(
                "{\"int\":1,\"long\":12345678901234,\"big\":123456789012345678901234567890,\"double\":1.5,\"exp\":1e3,"
                        + "\"bool\":true,\"null\":null,\"list\":[1,\"a\",{\"b\":2.0}],\"text\":\"a/b\"}")));
        cases.put("parse-map-invalid", () -> describe(JacksonUtil.parseMap("[1,2]")));

        return cases;
    }

    @TestFactory
    Stream<DynamicTest> goldenFiles() {
        return cases().entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> {
            String actual;
            try {
                actual = entry.getValue().call();
            } catch (Exception e) {
                actual = "<exception>";
            }
            check(entry.getKey(), actual);
        }));
    }

    @TestFactory
    Stream<DynamicTest> roundTrips() {
        return Stream.of(
                DynamicTest.dynamicTest("document", () -> assertEquals(document().toJson(), JtsDocument.readJsonFromString(document().toJson()).toJson())),
                DynamicTest.dynamicTest("document-pretty", () -> assertEquals(document().toJsonPretty(),
                        new JtsDocument(document().toJsonPretty()).toJsonPretty())),
                DynamicTest.dynamicTest("document-header", () -> assertEquals(header().toJson(),
                        JacksonUtil.parseObject(header().toJson(), JtsDocumentHeader.class).toJson())),
                DynamicTest.dynamicTest("table-fields", () -> {
                    for (Map<Integer, JtsField> fields : table().getRecords().values())
                        for (JtsField field : fields.values())
                            assertEquals(field, JacksonUtil.parseObject(JacksonUtil.toJson(field), JtsField.class));
                }),
                DynamicTest.dynamicTest("field-modified-time", () -> {
                    JtsField field = JacksonUtil.parseObject(JacksonUtil.toJson(new JtsField(1.0, 2, "a", T0)), JtsField.class);
                    assertTrue(field.getModifiedTime() == null || T0.isEqual(field.getModifiedTime()));
                })
        );
    }

    private static void check(String name, String actual) throws IOException {
        Path file = GOLDEN_DIR.resolve(name + ".golden");
        String content = actual == null ? "<null>" : actual;

        if (UPDATE) {
            Files.createDirectories(GOLDEN_DIR);
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        } else {
            assertTrue(Files.exists(file), "Missing golden file " + file + "; run with GOLDEN_UPDATE=true against the reference version");
            assertEquals(new String(Files.readAllBytes(file), StandardCharsets.UTF_8), content, name);
        }
    }
}
