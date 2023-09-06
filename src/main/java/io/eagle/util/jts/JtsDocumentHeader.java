package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import io.eagle.util.Assert;
import io.eagle.util.jackson.JacksonUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * An immutable document header for a {@link JtsDocument}; part of the JSON Time Series document specification.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
// Use this annotation to avoid serializing a null
public class JtsDocumentHeader {


    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(JtsDocumentHeader.class);

    private String id;

    /**
     * Arbitrary name to identify the group of columns described, e.g. table name
     */
    private String name;

    /**
     * Timestamp of the earliest record (may not be the first record by record number)
     */
    private DateTime startTime;

    /**
     * Timestamp of the latest record (may not be the last record by record number)
     */
    private DateTime endTime;

    /**
     * Number of records in this document
     */
    private Integer recordCount;

    /**
     * Header information about each column
     */
    private SortedMap<Integer, JtsColumnHeader> columns = Maps.newTreeMap();


    /**
     * Default constructor; looks useless because the fields are final, but in fact this is used by Jackson for object/JSON mapping.
     */
    private JtsDocumentHeader() {
        this.columns = Maps.newTreeMap();
    }


    public JtsDocumentHeader(JtsDocumentHeader other) {
        this();
        this.id = other.id;
        this.name = other.name;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.recordCount = other.recordCount;
        this.columns = new TreeMap<Integer, JtsColumnHeader>(other.columns);
    }


    public JtsDocumentHeader(String id, String name, Integer recordCount) {
        this();
        this.id = id;
        this.name = name;
        this.recordCount = recordCount;
    }


    public JtsDocumentHeader(String name, Integer recordCount) {
        this();
        this.name = name;
        this.recordCount = recordCount;
    }


    private JtsDocumentHeader(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.recordCount = builder.recordCount;
        this.columns = builder.columns;
    }

    public JtsDocumentHeader withRecordCount(Integer recordCount) {
        JtsDocumentHeader header = new JtsDocumentHeader(this);
        header.recordCount = recordCount;
        return header;
    }

    public void putColumns(Map<Integer, JtsColumnHeader> columns) {
        this.columns.putAll(columns);
    }

    public void putColumn(int columnIndex, JtsColumnHeader column) {
        this.columns.put(columnIndex, column);
    }

    public Map<Integer, String> getNumberFormat() {
        Map<Integer, String> numberFormat = Maps.newHashMap();

        if (this.columns == null || this.columns.isEmpty())
            return numberFormat;

        for (Integer columnIndex : this.columns.keySet())
            numberFormat.put(columnIndex, this.columns.get(columnIndex).getFormat());

        return numberFormat;
    }

    public String getColumnFormat(Integer columnIndex) {
        Assert.notNull(columnIndex);

        if (this.columns.containsKey(columnIndex))
            return this.columns.get(columnIndex).getFormat();
        else
            return null;
    }

    /**
     * Returns a string representation of this JtsDocumentHeader (with no JSON quoting), using the default UTC timezone to format Dates.
     * <p>
     * Calls {@link #toString(DateTimeZone)} with UTC as the timezone.
     *
     * @return a string representation of this JtsDocumentHeader (with no JSON quoting), using the default UTC timezone to format Dates
     */
    @Override
    public String toString() {
        return this.toString(DateTimeZone.UTC);
    }

    /**
     * Returns a string representation of this JtsDocumentHeader (with no JSON quoting), using the given UTC timezone to format Dates.
     *
     * @param timezone timezone to use when printing a DateTime as a String
     * @return a string representation of this JtsDocumentHeader (with no JSON quoting), using the given UTC timezone to format Dates
     */
    public String toString(DateTimeZone timezone) {
        return JacksonUtil.toPrettyJson(this);
    }

    /**
     * Returns this JtsDocumentHeader as a JSON string.
     *
     * @return this JtsDocumentHeader as a JSON formatted string
     * @throws JsonProcessingException if there is an error writing this JtsDocumentHeader as a JSON string
     */
    public String toJson() throws JsonProcessingException {
        return JacksonUtil.getObjectMapper().writeValueAsString(this);
    }

    /**
     * Returns this JtsDocumentHeader as a JSON string, formatted to be more easily human-readable, using the default UTC timezone when
     * serializing a Joda {@link DateTime}.
     * <p>
     * Calls {@link #toJsonPretty(DateTimeZone)} with UTC as the timezone.
     *
     * @return this JtsDocumentHeader as a JSON string, formatted to be more easily human-readable, using the default UTC timezone when
     * serializing a Joda {@link DateTime}
     * @throws JsonProcessingException if there is an error writing the JSON string
     * @see #toJsonPretty(DateTimeZone)
     */
    public String toPrettyJson() throws JsonProcessingException {
        return this.toJsonPretty(DateTimeZone.UTC);
    }

    /**
     * Returns this JtsDocumentHeader as a JSON string, formatted to be more easily human-readable, using the given timezone when
     * serializing a Joda {@link DateTime}.
     * <p>
     * An example of "pretty" output (with two columns):
     *
     * <pre>
     * {
     * 	"name" : "Table 1",
     * 	"startTs" : "2013-01-01T12:00:00.000-07:00",
     * 	"endTs" : "2013-01-01T13:00:00.000-07:00",
     * 	"recordCount" : 60,
     * 	"metadata" :  { "foo" : "bar" },
     * 	"columns" :
     *    {
     * 		"0" :
     *        {
     * 			"id": "myId",
     * 			"name": "myColumn",
     * 			"description" : "a description of my column",
     * 			"dataType" : "NUMBER",
     * 			"aggType" : "RAW"
     * 			"format" : "#.##"
     *        },
     * 		"1" :
     *        {
     * 			"id": "myId",
     * 			"name": "second column",
     * 			"description" : "a description of the second column",
     * 			"dataType" : "NUMBER",
     * 			"aggType" : "RAW"
     * 			"format" : "#.##"
     *        }
     *    }
     * }
     * </pre>
     *
     * @param dateTimeZone the timezone to use when serializing a Joda {@link DateTime} Object; if null, {@link DateTime} Objects will be
     *                     formatted using their current timezone
     * @return this JtsDocumentHeader as a JSON string, formatted to be more easily human-readable, using the given timezone when
     * serializing a Joda {@link DateTime}
     * @throws JsonProcessingException if there is an error writing the JSON string
     */
    public String toJsonPretty(DateTimeZone dateTimeZone) throws JsonProcessingException {
        return JacksonUtil.toJson(this);
    }

    /**
     * Returns the column names as a fixed-width String, with the width being 30 characters per column.
     *
     * @return the column names as a fixed-width String, with the width being 30 characters per column
     */
    @JsonIgnore
    public Object getFixedWidthColumnNames() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < this.columns.size(); i++) {
            if (this.columns.get(i) != null && this.columns.get(i).getName() != null) {
                sb.append(String.format("%-30s", this.columns.get(i).getName()));
            } else {
                sb.append(String.format("%-30s", ""));
            }
        }

        return sb.toString();
    }

    @JsonIgnore
    public String toDelimitedText(DocumentFormat format) {
        StringBuffer idHeader = new StringBuffer();
        StringBuffer nameHeader = new StringBuffer();
        StringBuffer unitsHeader = new StringBuffer();
        String q = format.getTextQualifier();
        String d = format.getDelimiter();

        idHeader.append(q + "Id" + q);
        nameHeader.append(q + "Timestamp" + q);
        unitsHeader.append(q + "Units" + q);

        for (int i = 0; i <= this.columns.lastKey(); i++) {
            String columnId = "";
            String columnName = "";
            String columnQuality = "";
            String columnAnnotation = "";
            String columnUnits = "";

            if (this.columns.containsKey(i)) {
                JtsColumnHeader column = this.columns.get(i);

                if (column.getId() != null)
                    columnId = column.getId();

                if (column.getName() != null)
                    columnName = column.getName();

                if (column.getUnits() != null)
                    columnUnits = column.getUnits();
            }

            if (format.isQualityEnabled()) {
                switch (format.getQualityFormat()) {
                    // "Name:Quality"
                    case DELIMITED_WITH_VALUE:
                        columnId += format.getQualityDelimiter() + "Quality";
                        columnName += format.getQualityDelimiter() + "Quality";
                        break;
                    // "Name","Name [Quality]"
                    case SEPARATE_VALUE:
                        columnQuality = d + q + columnName + " [Quality]" + q;
                        break;
                    default:
                }
            }

            if (format.isAnnotationsEnabled()) {
                switch( format.getAnnotationFormat() ) {
                    // "Name:Annotation"
                    case DELIMITED_WITH_VALUE:
                        columnId += format.getAnnotationDelimiter() + "Annotation";
                        columnName += format.getAnnotationDelimiter() + "Annotation";
                        break;
                    // "Name","Name [Annotation]"
                    case SEPARATE_VALUE:
                        columnAnnotation = d + q + columnName + " [Annotation]" + q;
                        break;
                    default:
                }
            }

            idHeader.append(d + q + columnId + q + columnQuality + columnAnnotation);
            nameHeader.append(d + q + columnName + q + columnQuality + columnAnnotation);
            unitsHeader.append(d + q + columnUnits + q);
        }

        // append newline to both header lines
        idHeader.append("\n");
        nameHeader.append("\n");
        unitsHeader.append("\n");

        return idHeader.toString() + nameHeader.toString() + unitsHeader.toString();
    }

    @JsonIgnore
    public String toFixedWidth(DocumentFormat format) {
        return String.format("%-30s%s\n", "Timestamp", this.getFixedWidthColumnNames());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the startTs of this header as a Joda {@link DateTime}, with default UTC timezone.
     * <p>
     * Calls {@link #getStartTs(DateTimeZone)} with UTC as the timezone.
     *
     * @return the startTs of this header as a Joda {@link DateTime}, with default UTC timezone
     */
    public DateTime getStartTime() {
        if (this.startTime == null) {
            return null;
        } else {
            return this.getStartTs(DateTimeZone.UTC);
        }
    }

    /**
     * Returns the startTs of this header as a Joda {@link DateTime}, with specified timezone.
     *
     * @param timezone the timezone of the returned DateTime
     * @return the startTs of this header as a Joda {@link DateTime}, with specified timezone
     */
    public DateTime getStartTs(DateTimeZone timezone) {
        if (this.startTime == null) {
            return null;
        } else {
            return this.startTime.withZone(timezone);
        }
    }

    /**
     * Returns the endTs of this header as a Joda {@link DateTime}, with default UTC timezone.
     * <p>
     * Calls {@link #getEndTs(DateTimeZone)} with UTC as the timezone.
     *
     * @return the endTs of this header as a Joda {@link DateTime}, with default UTC timezone
     */
    public DateTime getEndTime() {
        if (this.endTime == null) {
            return null;
        } else {
            return this.getEndTs(DateTimeZone.UTC);
        }
    }

    /**
     * Returns the endTs of this header as a Joda {@link DateTime}, with specified timezone.
     *
     * @param timezone the timezone of the returned DateTime
     * @return the endTs of this header as a Joda {@link DateTime}, with specified timezone
     */
    public DateTime getEndTs(DateTimeZone timezone) {
        if (this.endTime == null) {
            return null;
        } else {
            return this.endTime.withZone(timezone);
        }
    }

    /**
     * @return the recordCount
     */
    public Integer getRecordCount() {
        return recordCount;
    }

    public boolean hasColumn(int columnIndex) {
        return this.columns != null && this.columns.containsKey(columnIndex);
    }

    public JtsColumnHeader getColumn(int columnIndex) {
        if (this.hasColumn(columnIndex))
            return this.columns.get(columnIndex);
        else
            return null;
    }

    /**
     * Returns an unmodifiable view of the columns map; this avoid creating a defensive copy, but callers must copy the map if they need to
     * modify it.
     *
     * @return an unmodifiable view of the columns map
     */
    public SortedMap<Integer, JtsColumnHeader> getColumns() {
        if (this.columns != null) {
            return Collections.unmodifiableSortedMap(this.columns);
        } else {
            return null;
        }
    }

    public static class Builder {

        private String id;
        private String name;
        private DateTime startTime;
        private DateTime endTime;
        private Integer recordCount;
        private SortedMap<Integer, JtsColumnHeader> columns;


        public Builder id(String id) {
            this.id = id;
            return this;
        }


        public Builder name(String name) {
            this.name = name;
            return this;
        }


        public Builder startTime(DateTime startTime) {
            this.startTime = startTime;
            return this;
        }


        public Builder endTime(DateTime endTime) {
            this.endTime = endTime;
            return this;
        }


        public Builder recordCount(Integer recordCount) {
            this.recordCount = recordCount;
            return this;
        }


        public Builder columns(SortedMap<Integer, JtsColumnHeader> columns) {
            this.columns = columns;
            return this;
        }


        public JtsDocumentHeader build() {
            return new JtsDocumentHeader(this);
        }
    }
}
