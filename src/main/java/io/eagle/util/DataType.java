package io.eagle.util;

import io.eagle.util.geo.Coordinates;
import io.eagle.util.jts.complex.Metrics;
import io.eagle.util.jts.complex.Time;
import org.joda.time.DateTime;


/**
 * Represents the different data types which can be stored.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
public enum DataType {
    /**
     * All number types, both floating-point and integer
     */
    NUMBER(Number.class),

    /**
     * Boolean values
     */
    BOOLEAN(Boolean.class),

    /**
     * String values
     */
    TEXT(String.class),

    /**
     * Date and time
     */
    TIME(Time.class),

    /**
     * Geospatial coordinates (latitude and longitude)
     */
    COORDINATES(Coordinates.class),

    METRICS(Metrics.class);

    private final Class<?> classType;


    DataType(Class<?> classType) {
        this.classType = classType;
    }

    /**
     * If the given object matches a data type class, return the associated data type.
     */
    public static DataType getDataType(Object object) {
        if (object == null)
            return null;

        for (DataType dataType : DataType.values()) {
            if (dataType.getClassType().isAssignableFrom(object.getClass()))
                return dataType;
        }

        throw new IllegalArgumentException("Object could not be evaluated to a DataType: " + object.getClass().getSimpleName());
    }

    public Class<?> getClassType() {
        return this.classType;
    }

}
