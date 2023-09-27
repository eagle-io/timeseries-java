package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.*;
import io.eagle.util.*;
import io.eagle.util.jackson.JacksonUtil;
import org.bson.BasicBSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


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
 * <li>a {@link Map} of String to Object will be converted to either {@link DateTime} or {@link ComplexValue} after being parsed by
 * <li>a {@link BasicBSONObject} will be converted to either {@link DateTime} or {@link ComplexValue} after being parsed by
 * </ul>
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
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
    private Optional<Object> v;

    /**
     * An optional code representing quality information for this field
     */
    private Optional<Integer> q;

    /**
     * An optional text description for this field
     */
    private Optional<String> a;

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
        this.v = other.v;
        this.q = other.q;
        this.a = other.a;
        this.m = other.m;
    }

    public JtsField( Object value ) {
        this(value, null, null, null);
    }


    public JtsField( Object value, Integer quality ) {
        this(value, quality, null, null);
    }


    public JtsField( Object value, Integer quality, String annotation ) {
        this( value, quality, annotation, null );
    }


    public JtsField( Object value, Integer quality, String annotation, DateTime modifiedTime ) {
        if( value != null ) this.v = Optional.of(parseValue(value));
        if( quality != null ) this.q = Optional.of(quality);
        if( annotation != null ) this.a = Optional.of(annotation);
        this.m = modifiedTime;
    }

    public JtsField( SystemQuality quality ) {
        this.q = Optional.of( quality.getCode() );
    }


    public JtsField( Object value, SystemQuality quality ) {
        this.v = Optional.of( value );
        this.q = Optional.of( quality.getCode() );
    }

    public static JtsField of( Object value ) {
        return new JtsField( value );
    }

    @JsonCreator
    public JtsField( Map<String, Object> attributes ) {
        if( attributes.containsKey("v") ) this.v = Optional.ofNullable(parseValue(attributes.get("v")));
        if( attributes.containsKey("q") ) this.q = Optional.ofNullable((Integer) attributes.get("q"));
        if( attributes.containsKey("a") ) this.a = Optional.ofNullable((String) attributes.get("a"));
    }

    /**
     * Strangely (perhaps due to type erasure?), fields of type Object (e.g. 'v') ignore type framing rules (e.g. {$coords: [35.1, 151.2]}.
     * Here we manually apply the ObjectMapper conversion to enforce type framing.
     */
    @JsonProperty("v")
    public Object toJsonValue() {
        if( this.v == null ) {
            return null;
        } else if( this.v.isPresent() ) {
            return JacksonUtil.getObjectMapper().convertValue(this.v.get(), Object.class);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object parseValue( Object value ) {
        if( value instanceof String || value instanceof ComplexValue )
            return value;
        else if( value instanceof Double )
            return Double.isNaN( (Double) value ) || Double.isInfinite( (Double) value ) ? null : value;
        else if( value instanceof Number )        // Number converted to Double
            return ( (Number) value ).doubleValue();
        else if( value instanceof Boolean )    // Boolean converted to Double, e.g. true=1.0, false=0.0
            return (Boolean) value == true ? 1.0 : 0.0;
        else if( value instanceof DateTime)
            return new Time((DateTime) value);
        else if( value instanceof Date )
            return new Time(new DateTime( value, DateTimeZone.UTC ));
        else if( value instanceof Map )        // Map parsed as ComplexType, e.g. {"$coords": [-35.0,153.4]}
            return JacksonUtil.getObjectMapper().convertValue(value, ComplexValue.class);
        else if( value == null )
            return null;
        else
            throw new IllegalStateException( String.format( "Unable to convert value '%s' of class '%s' to JtsField value", value, value.getClass().getName() ) );
    }

    public void merge( JtsField other ) {
        if( other.v != null ) this.v = other.v;
        if( other.q != null ) this.q = other.q;
        if( other.a != null ) this.a = other.a;

        if( this.m == null )
            this.m = other.m;
        else if( other.m != null && other.m.isAfter( this.m ) )
            this.m = other.m;
    }

    public void setAttributes( JtsField other ) {
        this.v = other.v;
        this.q = other.q;
        this.a = other.a;
        this.m = other.m;
    }

    public boolean hasValue() {
        return this.v   != null && this.v.isPresent();
    }

    public boolean hasQuality() {
        return this.q != null && this.q.isPresent();
    }

    public boolean hasAnnotation() {
        return this.a != null && this.a.isPresent();
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
        return this.q != null && SystemQuality.DELETE.getCode().equals( this.q.orElse(null) );
    }

    /**
     * Returns the quality; note that defensive copy is not needed because {@link Integer} is immutable.
     *
     * @return the quality
     */
    public Integer getQuality() {
        if( this.q == null || ! this.q.isPresent() )
            return null;
        else if( this.q.get() < 0 )
            return this.q.get();
        else
            return BitBuddy.getRight( this.q.get() );
    }

    /**
     * Returns the value; note that defensive copy is not needed because all possible types are immutable: {@link String}, {@link Double},
     * {@link DateTime} and {@link ComplexValue}.
     *
     * @return the value
     */
    public Object getValue() {
        return this.v == null ? null : this.v.get();
    }

    /**
     * Sets the given value {@link Object}, which must be {@link Double}, {@link String}, {@link DateTime}, {@link ComplexValue},
     * {@link Date}, {@link BasicBSONObject}, or null.
     *
     * @param value the value to set; must be {@link Double}, {@link String}, {@link DateTime}, {@link ComplexValue}, {@link Date},
     *              {@link BasicBSONObject}, or null
     */
    public void setValue( Object value ) {
        this.v = Optional.ofNullable( parseValue( value ) );
    }

    /**
     * @return the {@link ComplexValue} value, or null if the value is null
     */
    public ComplexValue getValueAsComplexValue() {
        if( this.v == null || ! this.v.isPresent() )
            return null;
        else {
            Assert.isInstanceOf( ComplexValue.class, this.v.get() );
            return (ComplexValue) this.v.get();
        }
    }

    /**
     * @return the {@link DateTime} value, or null if the value is null
     */
    public Time getValueAsTime() {
        if( this.v == null || ! this.v.isPresent() )
            return null;
        else {
            Assert.isInstanceOf( Time.class, this.v.get() );
            return (Time) this.v.get();
        }
    }

    /**
     * @return the {@link Double} value, or null if the value is null
     */
    public Double getValueAsDouble() {
        if( this.v == null || ! this.v.isPresent() )
            return null;
        else {
            Assert.isInstanceOf( Double.class, this.v.get() );
            return (Double) this.v.get();
        }
    }

    /**
     * @return the {@link String} value, or null if the value is null
     */
    public String getValueAsString() {
        if( this.v == null || ! this.v.isPresent() )
            return null;
        else {
            Assert.isInstanceOf( String.class, this.v.get() );
            return (String) this.v.get();
        }
    }

    public String getAnnotation() {
        if( this.a == null || ! this.a.isPresent() )
            return null;
        else
            return this.a.get();
    }

    public void setAnnotation( String annotation ) {
        this.a = Optional.ofNullable( annotation );
    }

    public DateTime getModifiedTime() {
        return this.m;
    }

    public void setModifiedTime( Object modifiedTime ) {
        this.m = new DateTime( modifiedTime, DateTimeZone.UTC );
    }

    public DataType getDataType() {
        if( this.v != null && this.v.isPresent() )
            return DataType.getDataType( this.v.get() );
        else
            return null;
    }

    /**
     * @return true if the quality has the system quality bit set; false if the system quality bit is not set, or if the quality is null
     */
    public boolean hasSystemQuality() {
        if( this.q != null && this.q.isPresent() )
            return BitBuddy.getBit( this.q.get(), SYSTEM_QUALITY );
        else
            return false;
    }

    /**
     * Sets the combined quality value.
     *
     * @param quality the combined quality value
     */
    public void setCombinedQuality( Integer quality ) {
        this.q = Optional.ofNullable( quality );
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

        this.q = Optional.ofNullable( quality );
    }

    /**
     * Sets the user quality.
     *
     * @param userQuality sets the user quality
     */
    public void setUserQuality( Integer userQuality ) {
        if( userQuality == null ) {
            this.q = Optional.empty();
            return;
        }

        if( userQuality > 65535 || userQuality < 0 )
            throw new IllegalArgumentException( "Invalid user quality. Must be greater than 0 and less than 65535" );

        Integer quality = 0;
        quality = BitBuddy.setRight( quality, userQuality );
        quality = BitBuddy.clearBit( quality, SYSTEM_QUALITY );

        this.q = Optional.ofNullable( quality );
    }

    public void clearValue() {
        this.v = null;
    }

    public void clearQuality() {
        this.q = null;
    }

    public void clearAnnotation() {
        this.a = null;
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
        String str = this.v == null ? "null" : String.valueOf( this.v.get() );

        if( this.q != null && this.q.isPresent() )
            str += ":" + this.q;

        if( this.a != null && this.a.isPresent() )
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
