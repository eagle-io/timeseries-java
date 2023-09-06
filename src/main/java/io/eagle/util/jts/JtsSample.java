package io.eagle.util.jts;

import org.joda.time.DateTime;

import java.util.Objects;


/**
 * A combination of timestamp and {@link JtsField}.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
public class JtsSample {

    /**
     * Field of the sample
     */
    private JtsField field;

    /**
     * Timestamp of the sample
     */
    private DateTime timestamp;


    /**
     * Default constructor.
     */
    public JtsSample() {
    }


    /**
     * Parameter constructor, sets timestamp and JtsField
     *
     * @param timestamp the timestamp
     * @param field     the JtsField
     */
    public JtsSample( DateTime timestamp, JtsField field ) {
        this.timestamp = timestamp;
        this.field = field;
    }


    /**
     * Parameter constructor, sets timestamp and JtsField value
     *
     * @param timestamp the timestamp
     * @param value     the value of the JtsField
     */
    public JtsSample( DateTime timestamp, Object value ) {
        this.timestamp = timestamp;
        this.field = new JtsField( value );
    }


    /**
     * Parameter constructor, sets timestamp, JtsField value, and JtsField quality
     *
     * @param timestamp the timestamp
     * @param value     the value of the JtsField
     * @param quality   the quality of the JtsField
     */
    public JtsSample( DateTime timestamp, Object value, Integer quality ) {
        this.timestamp = timestamp;
        this.field = new JtsField( value ).withUserQuality( quality );
    }


    @Override
    public int hashCode() {
        return Objects.hash( field, timestamp );
    }


    @Override
    public boolean equals( Object object ) {
        if( object instanceof JtsSample ) {
            JtsSample that = (JtsSample) object;
            return Objects.equals( this.field, that.field )
                    && Objects.equals( this.timestamp, that.timestamp );
        }
        return false;
    }


    /**
     * @return the JtsField
     */
    public JtsField getField() {
        return this.field;
    }

    /**
     * Sets JtsField by copying value and quality from the given JtsField
     *
     * @param field the JtsField to set
     */
    public void setField( JtsField field ) {
        if( field == null )
            this.field = null;
        else if( this.field == null )
            this.field = new JtsField( field );
        else
            this.field = field;
    }

    /**
     * @return the quality of the JtsField, or null if the JtsField is null
     */
    public Integer getQuality() {
        if( this.field == null )
            return null;
        else
            return this.field.getQuality();
    }

    public String getAnnotation() {
        if( this.field == null )
            return null;
        else
            return this.field.getAnnotation();
    }

    /**
     * @return the timestamp
     */
    public DateTime getTimestamp() {
        return this.timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp( DateTime timestamp ) {
        this.timestamp = timestamp;
    }

    public DateTime getModifiedTime() {
        if( this.field == null )
            return null;
        else
            return this.field.getModifiedTime();
    }

    /**
     * @return the value of the JtsField, or null if the JtsField is null
     */
    public Object getValue() {
        if( this.field == null )
            return null;
        else
            return this.field.getValue();
    }

    public Double getValueAsDouble() {
        if( this.field == null )
            return null;
        else
            return this.field.getValueAsDouble();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("javadoc")
    @Override
    public String toString() {
        return this.timestamp.toString() + "->" + this.field;
    }
}
