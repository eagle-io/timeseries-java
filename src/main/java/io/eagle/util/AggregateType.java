package io.eagle.util;

import java.util.EnumSet;


/**
 * Defines aggregate types, including groupings of types.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 * @see Aggregate
 */
public enum AggregateType {
    INTERPOLATED,

    /**
     * Average type; the average value derived from the count and total of raw values in the aggregate; included in the {@link #ALL} type
     */
    AVERAGE,

    /**
     * Total type; the sum of raw values in the aggregate; included in the {@link #ALL} type
     */
    TOTAL,

    /**
     * Minimum type; the smallest of raw values in the aggregate; included in the {@link #ALL} type
     */
    MIN,
    MINIMUM,

    /**
     * Maximum type; the largest of raw values in the aggregate; included in the {@link #ALL} type
     */
    MAX,
    MAXIMUM,

    RANGE,

    /**
     * Count type; the total number of raw values in the aggregate; included in the {@link #ALL} type
     */
    COUNT,

    START,

    END,

    /**
     * Delta type; derived from subtracting the first raw value from the last raw value; <b>NOT included in the {@link #ALL} type</b>
     */
    DELTA,

    CHANGE,

    PERCENT,

    MEDIAN,

    /**
     * None type; represents no aggregate calculation (i.e. a raw value); <b>NOT included in the {@link #ALL} type</b>
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
