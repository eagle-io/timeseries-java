package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.eagle.util.Assert;
import io.eagle.util.BitBuddy;
import io.eagle.util.ComplexValue;
import io.eagle.util.DataType;
import io.eagle.util.geo.Coordinates;
import io.eagle.util.time.JodaTime;
import org.bson.BasicBSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * A single field of the JSON Time Series document specification; a JtsField is the base-level structure of the JTS specification,
 * consisting of a single value {@link Object} {@link #v} and an (optional) single quality {@link Integer} {@link #q}.
 * <p>
 * The field value is an {@link Object} to allow flexibility, but only four class types are allowed:
 * <ol>
 * <li>{@link Double}
 * <li>{@link String}
 * <li>{@link DateTime}
 * <li>{@link ComplexValue}
 * </ol>
 * <p>
 * Although only the above four classes are allowed as values, for deserialization and convenience purposes,the following type conversions
 * will be performed when setting the value:
 * <ul>
 * <li>a {@link Date} will be converted to a {@link DateTime}
 * <li>a {@link Number} will be converted to a {@link Double}
 * <li>a {@link Boolean} will be converted to a {@link Double}
 * <li>a {@link Date}, and a {@link Double} may be set as a {@link Number}
 * <li>a {@link Map} of String => Object will be converted to either {@link DateTime} or {@link ComplexValue} after being parsed by
 * {@link #parseMapAsComplexValue(Map)}
 * <li>a {@link BasicBSONObject} will be converted to either {@link DateTime} or {@link ComplexValue} after being parsed by
 * {@link #parseMapAsComplexValue(Map)}
 * </ul>
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
public final class JtsField {

    public final static JtsField EMPTY_FIELD = new JtsField();
    public final static JtsField DELETE_FIELD = new JtsField( SystemQuality.DELETE );

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger( JtsField.class );
    /**
     * Bit offset for system quality
     */
    private final static int SYSTEM_QUALITY = 16;
    /**
     * An Object which is the value of this field. The Object class is limited to one of these 4:
     * <ol>
     * <li>{@link Double}
     * <li>{@link String}
     * <li>{@link DateTime}
     * <li>{@link ComplexValue}
     * </ol>
     */
    private final JtsAttribute<Object> v = new JtsAttribute<>();

    /**
     * An optional code representing quality information for this field
     */
    private final JtsAttribute<Integer> q = new JtsAttribute<>();

    /**
     * An optional text description for this field
     */
    private final JtsAttribute<String> a = new JtsAttribute<>();

    /**
     * An optional timestamp when this field was last modified
     */
    private DateTime m = null;

    /**
     * Default constructor, used by Jackson for object/JSON mapping.
     */
    public JtsField() {
    }


    /**
     * Copy constructor.
     *
     * @param other the JtsField to copy from
     */
    public JtsField( JtsField other ) {
        if( other.v.isPresent() )
            this.v.setAttribute( parseValue( other.v.getAttribute() ) );

        if( other.q.isPresent() )
            this.q.setAttribute( other.q.getAttribute() );

        if( other.a.isPresent() )
            this.a.setAttribute( other.a.getAttribute() );

        this.m = other.m;
    }


    @JsonCreator
    public JtsField( Map<String, Object> attributes ) {
        if( attributes == null )
            return;

        attributes.forEach( ( key, value ) -> {
            switch( key ) {
                case "v":
                    this.v.setAttribute( parseValue( value ) );
                    break;
                case "q":
                    this.q.setAttribute( (Integer) value );
                    break;
                case "a":
                    this.a.setAttribute( (String) value );
                    break;
            }
        } );

    }


    public JtsField( Object value ) {
        this.v.setAttribute( parseValue( value ) );
    }


    public JtsField( Object value, Integer quality ) {
        this.v.setAttribute( parseValue( value ) );
        this.q.setAttribute( quality );
    }


    public JtsField( Object value, Integer quality, String annotation ) {
        this( value, quality, annotation, null );
    }


    public JtsField( Object value, Integer quality, String annotation, DateTime modifiedTime ) {
        this.v.setAttribute( parseValue( value ) );
        this.q.setAttribute( quality );
        this.a.setAttribute( annotation );
        this.m = modifiedTime;
    }


    /**
     * Parameter constructor which sets the quality only (the value is set to {@code null}).
     *
     * @param quality the quality
     */
    public JtsField( SystemQuality quality ) {
        this.q.setAttribute( quality.getCode() );
    }


    public JtsField( Object value, SystemQuality quality ) {
        this.v.setAttribute( value );
        this.q.setAttribute( quality.getCode() );
    }

    public static JtsField of( Object value ) {
        return new JtsField( value );
    }

    @SuppressWarnings("unchecked")
    private static Object parseValue( Object value ) {
        if( value instanceof String || value instanceof DateTime || value instanceof ComplexValue )
            return value;
        else if( value instanceof Double )
            return Double.isNaN( (Double) value ) || Double.isInfinite( (Double) value ) ? null : value;
        else if( value instanceof Number )        // Number converted to Double
            return ( (Number) value ).doubleValue();
        else if( value instanceof Boolean )    // Boolean converted to Double, e.g. true=1.0, false=0.0
            return (Boolean) value == true ? 1.0 : 0.0;
        else if( value instanceof Date )
            return new DateTime( value, DateTimeZone.UTC );
        else if( value instanceof Map )        // Map parsed as ComplexType, e.g. {"$coords": [-35.0,153.4]}
            return parseMapAsComplexValue( (Map<String, Object>) value );
        else if( value == null )
            return null;
        else
            throw new IllegalStateException( String.format( "Unable to convert value '%s' of class '%s' to JtsField value", value, value.getClass().getName() ) );
    }

    /**
     * Parses a Map containing a {@link ComplexValue} which is defined by the map key.
     *
     * @param complexValueMap the Map containing a {@link ComplexValue} defined by the map key
     * @return the Object which is the Map value; either {@link DateTime} or {@link ComplexValue}
     */
    @SuppressWarnings("unchecked")
    public static Object parseMapAsComplexValue( Map<String, Object> complexValueMap ) {
        Object value;

        if( complexValueMap.containsKey( ComplexValue.TIME_MILLIS_KEY ) ) {
            /*
             * The ComplexValue.DATE_KEY means the map value is a DateTime expressed as milliseconds
             * e.g. "$date" => 123456789
             */
            value = complexValueMap.get( ComplexValue.TIME_MILLIS_KEY );
            Assert.isInstanceOf( Number.class, value );
            value = new DateTime( ( (Number) value ).longValue(), DateTimeZone.UTC );
        } else if( complexValueMap.containsKey( ComplexValue.TIME_ISO_KEY ) ) {
            /*
             * The ComplexValue.DATE_ISO_KEY means the map value is a DateTime expressed as an ISO8601 String
             * e.g. "$dateiso" => "2014-06-09T10:20:30-08:00"
             */
            value = complexValueMap.get( ComplexValue.TIME_ISO_KEY );
            Assert.isInstanceOf( String.class, value );
            value = JodaTime.parseDateTimeISO( (String) value );
        } else if( complexValueMap.containsKey( ComplexValue.COORDS_KEY ) ) {
            // The Coordinates.COMPLEX_KEY means the map value is a Coordinates expressed as List of Numbers, e.g. $coords => [ 1234, 5678 ]
            value = complexValueMap.get( ComplexValue.COORDS_KEY );
            Assert.isInstanceOf( List.class, value );
            value = new Coordinates( (List<Number>) value );
        } else if( complexValueMap.containsKey( ComplexValue.WIND_DIR_KEY ) ) {
            value = complexValueMap.get( ComplexValue.WIND_DIR_KEY );
            Assert.isInstanceOf( Number.class, value );
            value = new DateTime( ( (Number) value ).longValue(), DateTimeZone.UTC );
        } else {
            // Any other type is an exception
            throw new IllegalStateException( "Unable to parse complex type: " + complexValueMap.keySet() );
        }

        return value;
    }

    public void merge( JtsField other ) {
        if( other.v.getAttribute() != null )
            this.v.setAttribute( other.v.getAttribute() );
        else if( other.v.isPresent() )
            this.v.setAttribute( null );

        if( other.q.getAttribute() != null )
            this.q.setAttribute( other.q.getAttribute() );
        else if( other.q.isPresent() )
            this.q.setAttribute( null );

        if( other.a.getAttribute() != null )
            this.a.setAttribute( other.a.getAttribute() );
        else if( other.a.isPresent() )
            this.a.setAttribute( null );

        if( this.m == null )
            this.m = other.m;
        else if( other.m != null && other.m.isAfter( this.m ) )
            this.m = other.m;
    }

    public void setAttributes( JtsField other ) {
        if( other.v.isPresent() )
            this.v.setAttribute( parseValue( other.v.getAttribute() ) );
        else
            this.v.clear();

        if( other.q.isPresent() )
            this.q.setAttribute( other.q.getAttribute() );
        else
            this.q.clear();

        if( other.a.isPresent() )
            this.a.setAttribute( other.a.getAttribute() );
        else
            this.a.clear();

        this.m = other.m;
    }

    public boolean hasValue() {
        return this.v.isPresent();
    }

    public boolean hasQuality() {
        return this.q.isPresent();
    }

    public boolean hasAnnotation() {
        return this.a.isPresent();
    }

    @Override
    public int hashCode() {
        return Objects.hash( v, q, a );
    }

    @Override
    public boolean equals( Object object ) {
        if( object instanceof JtsField ) {
            JtsField that = (JtsField) object;
            return Objects.equals( this.v, that.v )
                    && Objects.equals( this.q, that.q )
                    && Objects.equals( this.a, that.a );
        }
        return false;
    }

    public boolean isDeleted() {
        // Assert.isNull( this.v.getAttribute(), "Field marked for deletion must have null value" );
        // Assert.isNull( this.a.getAttribute(), "Field marked for deletion must have null annotation" );
        return SystemQuality.DELETE.getCode().equals( this.q.getAttribute() );
    }

    /**
     * Returns the quality; note that defensive copy is not needed because {@link Integer} is immutable.
     *
     * @return the quality
     */
    public Integer getQuality() {
        if( this.q.getAttribute() == null )
            return null;
        else if( this.q.getAttribute() < 0 )
            return this.q.getAttribute();
        else
            return BitBuddy.getRight( this.q.getAttribute() );
    }

    /**
     * Returns the value; note that defensive copy is not needed because all possible types are immutable: {@link String}, {@link Double},
     * {@link DateTime} and {@link ComplexValue}.
     *
     * @return the value
     */
    public Object getValue() {
        return this.v.getAttribute();
    }

    /**
     * Sets the given value {@link Object}, which must be {@link Double}, {@link String}, {@link DateTime}, {@link ComplexValue},
     * {@link Date}, {@link BasicBSONObject}, or null.
     *
     * @param value the value to set; must be {@link Double}, {@link String}, {@link DateTime}, {@link ComplexValue}, {@link Date},
     *              {@link BasicBSONObject}, or null
     */
    public void setValue( Object value ) {
        this.v.setAttribute( parseValue( value ) );
    }

    /**
     * @return the {@link ComplexValue} value, or null if the value is null
     */
    public ComplexValue getValueAsComplexValue() {
        if( this.v.getAttribute() == null )
            return null;
        else {
            Assert.isInstanceOf( ComplexValue.class, this.v.getAttribute() );
            return (ComplexValue) this.v.getAttribute();
        }
    }

    /**
     * @return the {@link DateTime} value, or null if the value is null
     */
    public DateTime getValueAsDateTime() {
        if( this.v.getAttribute() == null )
            return null;
        else {
            Assert.isInstanceOf( DateTime.class, this.v.getAttribute() );
            return (DateTime) this.v.getAttribute();
        }
    }

    /**
     * @return the {@link Double} value, or null if the value is null
     */
    public Double getValueAsDouble() {
        if( this.v.getAttribute() == null )
            return null;
        else {
            Assert.isInstanceOf( Double.class, this.v.getAttribute() );
            return (Double) this.v.getAttribute();
        }
    }

    /**
     * @return the {@link String} value, or null if the value is null
     */
    public String getValueAsString() {
        if( this.v.getAttribute() == null )
            return null;
        else {
            Assert.isInstanceOf( String.class, this.v.getAttribute() );
            return (String) this.v.getAttribute();
        }
    }

    public String getAnnotation() {
        return this.a.getAttribute();
    }

    public void setAnnotation( String annotation ) {
        this.a.setAttribute( annotation );
    }

    public DateTime getModifiedTime() {
        return this.m;
    }

    public void setModifiedTime( Object modifiedTime ) {
        this.m = new DateTime( modifiedTime, DateTimeZone.UTC );
    }

    public DataType getDataType() {
        return DataType.getDataType( this.v.getAttribute() );
    }

    /**
     * @return true if the quality has the system quality bit set; false if the system quality bit is not set, or if the quality is null
     */
    public boolean hasSystemQuality() {
        if( this.q.getAttribute() != null )
            return BitBuddy.getBit( this.q.getAttribute(), SYSTEM_QUALITY );
        else
            return false;
    }

    /**
     * Sets the combined quality value.
     *
     * @param quality the combined quality value
     */
    public void setCombinedQuality( Integer quality ) {
        this.q.setAttribute( quality );
    }

    /**
     * Sets the system quality.
     *
     * @param systemQuality the system quality
     */
    public void setSystemQuality( Integer systemQuality ) {
        // TODO: why do we not set the quality to null here?
        if( systemQuality == null )
            return;

        Integer quality = 0;
        quality = BitBuddy.setRight( quality, systemQuality );
        quality = BitBuddy.setBit( quality, SYSTEM_QUALITY );

        this.q.setAttribute( quality );
    }

    /**
     * Sets the user quality.
     *
     * @param userQuality sets the user quality
     */
    public void setUserQuality( Integer userQuality ) {
        if( userQuality == null ) {
            this.q.setAttribute( null );
            return;
        }

        if( userQuality > 65535 || userQuality < 0 )
            throw new IllegalArgumentException( "Invalid user quality. Must be greater than 0 and less than 65535" );

        Integer quality = 0;
        quality = BitBuddy.setRight( quality, userQuality );
        quality = BitBuddy.clearBit( quality, SYSTEM_QUALITY );

        this.q.setAttribute( quality );
    }

    public void clearValue() {
        this.v.clear();
    }

    public void clearQuality() {
        this.q.clear();
    }

    public void clearAnnotation() {
        this.a.clear();
    }

    public void clearModifiedTime() {
        this.m = null;
    }

    /**
     * Returns this JtsField as a raw String; uses the toString() method of the value object (with no JSON quoting) plus the quality, which
     * may be null.
     * <p>
     * Note this may represent DateTime values differently before and after JSON serialization. Before serialization of a JtsField to JSON,
     * a DateTime value will be UTC timezone. After reserialization of a new JtsField object from JSON, the same value may have a different
     * timezone, if the JSON was formatted with a non-UTC timezone during serialization.
     * <p>
     *
     * @return this JtsField as a raw String with no type conversion
     */
    @Override
    public String toString() {
        String str = String.valueOf( this.v );

        if( this.q.getAttribute() != null )
            str += ":" + this.q;

        if( this.a.getAttribute() != null )
            str += ";" + this.a;

        return str;
    }


    /**
     * @param quality
     * @return
     */
    public JtsField withSystemQuality( Integer quality ) {
        JtsField field = new JtsField( this );
        field.setSystemQuality( quality );
        return field;
    }


    public JtsField withUserQuality( Integer quality ) {
        JtsField field = new JtsField( this );
        field.setUserQuality( quality );
        return field;
    }


    public JtsField withValue( Object lastValue ) {
        JtsField field = new JtsField( this );
        field.setValue( lastValue );
        return field;
    }
}
