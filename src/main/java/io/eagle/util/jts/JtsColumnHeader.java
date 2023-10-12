package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.eagle.util.DataType;
import io.eagle.util.jackson.JacksonUtil;
import io.eagle.util.AggregateType;
import io.eagle.util.BaselineType;
import io.eagle.util.DisplayType;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


/**
 * An immutable column header which describes a column within a {@link JtsDocumentHeader}; part of the JSON Time Series document
 * specification.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JtsColumnHeader {


    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(JtsColumnHeader.class);

    /**
     * MongoDB _id of the column
     */
    private String id;

    /**
     * Name of the column
     */
    private String name;

    /**
     * DataType of the column
     */
    private DataType dataType;

    /**
     * Aggregate type of the column
     */
    private AggregateType aggregate;

    /**
     * Aggregate period
     */
    private Period interval;

    /**
     * Aggregate base time
     */
    private String baseTime;

    /**
     * Expression describing how the values for this column should be rendered
     */
    private String format;

    private DisplayType renderType;

    private String units;

    private BaselineType baselineType;

    private DateTime baselineTime;

    private Map<String, String> metrics;

    /**
     * Default constructor; looks useless because the fields are final, but in fact this is used by Jackson for object/JSON mapping.
     */
    @SuppressWarnings("unused")
    private JtsColumnHeader() {
    }


    /**
     * Copy constructor.
     *
     * @param other the JtsColumnHeader to copy from
     */
    public JtsColumnHeader(JtsColumnHeader other) {
        this.id = other.getId();
        this.name = other.getName();
        this.dataType = other.getDataType();
        this.aggregate = other.getAggregate();
        this.interval = other.getInterval();
        this.baseTime = other.getBaseTime();
        this.format = other.getFormat();
        this.renderType = other.getRenderType();
        this.units = other.getUnits();
        this.baselineType = other.getBaselineType();
        this.baselineTime = other.getBaselineTime();
        this.metrics = other.getMetrics();
    }


    /**
     * Parameter constructor.
     *
     * @param id          MongoDB _id of the column
     * @param name        name of the column
     * @param datatype    dataType of the column
     * @param agggregate  aggregate type of the column
     * @param format      format of the column
     */
    public JtsColumnHeader(String id, String name, DataType datatype, AggregateType agggregate, Period period, String baseTime,
                           String format, DisplayType renderType, String units) {
        this.id = id;
        this.name = name;
        this.dataType = datatype;
        this.aggregate = agggregate;
        this.interval = period;
        this.baseTime = baseTime;
        this.format = format;
        this.renderType = renderType;
        this.units = units;
    }


    private JtsColumnHeader(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.dataType = builder.dataType;
        this.aggregate = builder.aggregate;
        this.interval = builder.interval;
        this.baseTime = builder.baseTime;
        this.format = builder.format;
        this.renderType = builder.renderType;
        this.units = builder.units;
        this.baselineType = builder.baselineType;
        this.baselineTime = builder.baselineTime;
        this.metrics = builder.metrics;
    }

    /**
     * Returns a string representation of this JtsColumnHeader.
     *
     * @return a string representation of this JtsColumnHeader
     */
    @Override
    public String toString() {
        return this.toPrettyJson();
    }

    /**
     * Returns this JtsColumnHeader as a JSON string.
     *
     * @return this JtsColumnHeader as a JSON formatted string
     */
    public String toJson() {
        return JacksonUtil.toJson(this);
    }

    /**
     * Returns this JtsColumnHeader as a JSON string, formatted to be more easily human-readable.
     * <p>
     * An example of "pretty" output:
     *
     * <pre>
     * {
     * 		"id": "myId",
     * 		"name": "myColumn",
     * 		"description" : "a description of my column",
     * 		"dataType" : "NUMBER",
     * 		"aggType" : "RAW"
     * 		"format" : "#.##"
     * }
     * </pre>
     *
     * @return this JtsColumnHeader as a JSON formatted string, formatted to be more easily human-readable
     */
    public String toPrettyJson() {
        return JacksonUtil.toPrettyJson(this);
    }

    public JtsColumnHeader withId(String id) {
        JtsColumnHeader column = new JtsColumnHeader(this);
        column.id = id;

        return column;
    }

    public JtsColumnHeader withName(String name) {
        JtsColumnHeader column = new JtsColumnHeader(this);
        column.name = name;

        return column;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the dataType
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * @return the aggType
     */
    public AggregateType getAggregate() {
        return aggregate;
    }

    /**
     * @return the period
     */
    public Period getInterval() {
        return interval;
    }

    public String getBaseTime() {
        return baseTime;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    public DisplayType getRenderType() {
        return renderType;
    }

    public String getUnits() {
        return units;
    }

    public BaselineType getBaselineType() {
        return baselineType;
    }

    public DateTime getBaselineTime() {
        return baselineTime;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public static class Builder {

        private String id;
        private String name;
        private DataType dataType;
        private AggregateType aggregate;
        private Period interval;
        private String baseTime;
        private String format;
        private DisplayType renderType;
        private String units;
        private BaselineType baselineType;
        private DateTime baselineTime;
        private Map<String, String> metrics;

        public Builder id(String id) {
            this.id = id;
            return this;
        }


        public Builder name(String name) {
            this.name = name;
            return this;
        }


        public Builder dataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }


        public Builder aggregate(AggregateType aggregate) {
            this.aggregate = aggregate;
            return this;
        }


        public Builder interval(Period interval) {
            this.interval = interval;
            return this;
        }


        public Builder baseTime(String baseTime) {
            this.baseTime = baseTime;
            return this;
        }


        public Builder format(String format) {
            this.format = format;
            return this;
        }


        public Builder renderType(DisplayType renderType) {
            this.renderType = renderType;
            return this;
        }


        public Builder units(String units) {
            this.units = units;
            return this;
        }


        public Builder baselineType(BaselineType baselineType) {
            this.baselineType = baselineType;
            return this;
        }


        public Builder baselineTime(DateTime baselineTime) {
            this.baselineTime = baselineTime;
            return this;
        }

        public Builder metrics(Map<String, String> metrics) {
            this.metrics = metrics;
            return this;
        }


        public JtsColumnHeader build() {
            return new JtsColumnHeader(this);
        }
    }
}
