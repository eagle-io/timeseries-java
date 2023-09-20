package io.eagle.util;

import java.util.EnumSet;


/**
 * Defines aggregate types, including groupings of types.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
public enum AggregateType {
    INTERPOLATED,

    /**
     * Average type; the average value derived from the count and total of raw values in the aggregate
     */
    AVERAGE,

    /**
     * Total type; the sum of raw values in the aggregate
     */
    TOTAL,

    /**
     * Minimum type; the smallest of raw values in the aggregate
     */
    MIN,
    MINIMUM,

    /**
     * Maximum type; the largest of raw values in the aggregate
     */
    MAX,
    MAXIMUM,

    RANGE,

    /**
     * Count type; the total number of raw values in the aggregate
     */
    COUNT,

    START,

    END,

    /**
     * Delta type; derived from subtracting the first raw value from the last raw value
     */
    DELTA,

    CHANGE,

    PERCENT,

    MEDIAN,

    /**
     * None type; represents no aggregate calculation (i.e. a raw value)
     */
    NONE;

    public static final EnumSet<AggregateType> NUMERIC_AGGREGATES = EnumSet.allOf(AggregateType.class);
    public static final EnumSet<AggregateType> NON_NUMERIC_AGGREGATES = EnumSet.of(CHANGE, COUNT, START, END, NONE);
    public static final EnumSet<AggregateType> NON_PERIODIC_AGGREGATES = EnumSet.of(CHANGE, NONE);
    public static final EnumSet<AggregateType> PERIODIC_AGGREGATES = EnumSet.complementOf(NON_PERIODIC_AGGREGATES);


    public boolean isPeriodicAggregate() {
        return PERIODIC_AGGREGATES.contains(this);
    }
}
