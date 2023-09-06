package io.eagle.util.geo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import io.eagle.util.Assert;
import io.eagle.util.ComplexValue;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;


/**
 * Coordinates as Latitude and Longitude.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
public class Coordinates implements Comparable<Coordinates>, ComplexValue {

    public static final String COORDINATES_DELIMITER = "/";

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger( Coordinates.class );
    /**
     * Latitude, always serialized first
     */
    private final Double latitude;

    /**
     * Longitude, always serialized second
     */
    private final Double longitude;


    /**
     * Default constructor, required for Spring MongoDB integration
     */
    @SuppressWarnings("unused")
    private Coordinates() {
        this.latitude = null;
        this.longitude = null;
    }


    /**
     * Parameter constructor.
     *
     * @param latitude  the latitude to set, never null, absolute value must be <= 90
     * @param longitude the longitude to set, never null, absolute value must be <= 180
     */
    public Coordinates( Number latitude, Number longitude ) {
        Assert.notNull( latitude );
        Assert.notNull( longitude );

        this.latitude = latitude.doubleValue();
        this.longitude = longitude.doubleValue();

        validate( this.latitude, this.longitude );
    }


    /**
     * Parameter constructor, used by Jackson deserialization.
     *
     * @param coordinatesList the latitude and longitude as a List<Number>, never null, must have exactly two items
     */
    @JsonCreator
    public Coordinates( List<? extends Number> coordinatesList ) {
        Assert.notNull( coordinatesList );
        Assert.isTrue( coordinatesList.size() == 2, "Coordinates list must contain exactly 2 items" );

        this.latitude = coordinatesList.get( 0 ).doubleValue();
        this.longitude = coordinatesList.get( 1 ).doubleValue();

        validate( this.latitude, this.longitude );
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

    /**
     * Returns a Map which can be serialized to JSON, where the key is {@link #COORDS_KEY} and the value is latitude and longitude as an
     * array, for example: "$coords" => [ 90, 180 ]
     *
     * @return a Map which can be serialized to JSON, where the key is {@link #COORDS_KEY} and the value is latitude and longitude as an
     * array
     */
    @JsonValue
    public Object toJson() {
        return ImmutableMap.of( ComplexValue.COORDS_KEY, this.asArray() );
    }

    /**
     * Returns a string representation of this Coordinates.
     *
     * @return a string representation of this Coordinates
     */
    @Override
    public String toString() {
        return this.getLatitude() + COORDINATES_DELIMITER + this.getLongitude();
    }


    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return this.latitude;
    }


    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return this.longitude;
    }


    /**
     * @return the latitude and longitude as an Array
     */
    public double[] asArray() {
        return new double[] { latitude, longitude };
    }


    /**
     * @return the latitude and longitude as a List
     */
    public List<Double> asList() {
        return Arrays.asList( latitude, longitude );
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits( latitude );
        result = prime * result + (int) ( temp ^ ( temp >>> 32 ) );
        temp = Double.doubleToLongBits( longitude );
        result = prime * result + (int) ( temp ^ ( temp >>> 32 ) );
        return result;
    }


    @Override
    public boolean equals( Object obj ) {
        if( this == obj ) {
            return true;
        }
        if( obj == null ) {
            return false;
        }
        if( getClass() != obj.getClass() ) {
            return false;
        }
        Coordinates other = (Coordinates) obj;
        if( Double.doubleToLongBits( latitude ) != Double.doubleToLongBits( other.latitude ) ) {
            return false;
        }
        return Double.doubleToLongBits( longitude ) == Double.doubleToLongBits( other.longitude );
    }


    /**
     * Compares two intervals.
     *
     * @param otherCoordinates the other Coordinates to compare with this one
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unused")
    public int compareTo( Coordinates otherCoordinates ) {
        final int BEFORE = - 1;
        final int EQUAL = 0;
        final int AFTER = 1;

        // this optimization is usually worthwhile, and can
        // always be added
        if( this == otherCoordinates )
            return EQUAL;

        int latitudeComparison = this.getLatitude().compareTo( otherCoordinates.getLatitude() );

        if( latitudeComparison != EQUAL )
            return latitudeComparison;

        int longitudeComparison = this.getLongitude().compareTo( otherCoordinates.getLongitude() );

        if( longitudeComparison != EQUAL )
            return longitudeComparison;

        // all comparisons have yielded equality
        // verify that compareTo is consistent with equals (optional)
        assert this.equals( otherCoordinates ) : "compareTo inconsistent with equals.";

        return EQUAL;
    }


    @Override
    public BSONObject toDBObject() {
        return new BasicBSONObject( ComplexValue.COORDS_KEY, this.asArray() );
    }


    @Override
    public String toDelimitedText( String delimiter, String textQualifier ) {
        Assert.doesNotContain( delimiter, COORDINATES_DELIMITER );
        return this.toString();
    }

}
