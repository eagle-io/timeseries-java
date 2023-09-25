package io.eagle.util.geo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ComparisonChain;
import io.eagle.util.Assert;
import io.eagle.util.jts.ComplexValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;

public class Coordinates extends ComplexValue<List<Double>> {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger( Coordinates.class );

    public static final String COORDS_KEY = "$coords";

    /**
     * Parameter constructor.
     *
     * @param latitude  the latitude to set, never null, absolute value must be less than 90
     * @param longitude the longitude to set, never null, absolute value must be less than 180
     */
    public Coordinates( Number latitude, Number longitude ) {
        super(Arrays.asList(latitude.doubleValue(), longitude.doubleValue()));

        validate( getLatitude(), getLongitude() );
    }


    /**
     * Parameter constructor, used by Jackson deserialization.
     *
     * @param coords the latitude and longitude as a List, never null, must have exactly two items
     */
    @JsonCreator
    public Coordinates( List<? extends Number> coords ) {
        super(Arrays.asList(coords.get(0).doubleValue(), coords.get(1).doubleValue()));

        validate( getLatitude(), getLongitude() );
    }

    /**
     * Ensures the given latitude and longitude are within correct limits.
     *
     * @param latitude  the latitude to check
     * @param longitude the longitude to check
     * @return true if the given latitude and longitude are within correct limits, otherwise false
     */
    private static void validate( Double latitude, Double longitude ) {
        Assert.state( Math.abs( latitude ) <= 90, "Latitude must be less than or equal to 90 degrees" );
        Assert.state( Math.abs( longitude ) <= 180, "Longitude must be less than or equal to 180 degrees" );
    }

    @Override
    public int compareTo(ComplexValue<List<Double>> other) {
        return ComparisonChain.start()
                .compare(this.getLatitude(), other.getValue().get(0))
                .compare(this.getLongitude(), other.getValue().get(1))
                .result();
    }

    /**
     * Returns a string representation of this Coordinates.
     *
     * @return a string representation of this Coordinates
     */
    @Override
    public String toString() {
        return this.getLatitude() + "/" + this.getLongitude();
    }


    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return this.value.get(0);
    }


    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return this.value.get(1);
    }

    @Override
    public String getKey() {
        return COORDS_KEY;
    }
}
