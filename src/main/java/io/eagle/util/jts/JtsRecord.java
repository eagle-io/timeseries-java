package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.eagle.util.Assert;
import io.eagle.util.DataType;
import io.eagle.util.time.JodaTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;


/**
 * A single record of the JSON Time Series document specification, defined as a timestamp and a fields mapping of index => {@link JtsField}
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
public class JtsRecord<T> {

    /**
     * Timestamp of the record
     */
    private final DateTime ts;
    @JsonIgnore
    private final BiMap<Integer, T> index;
    /**
     * Fields map of the record
     */
    private Map<Integer, JtsField> f = new TreeMap<>();


    /**
     * Parameter constructor for Jackson deserialization only.
     *
     * @param ts     the timestamp that was deserialized from JSON, as an ISO8601 string or self-describing map; the format is either "$date" =>
     *               123456789, or
     *               "$dateiso" => "2014-06-09T10:20:30-08:00"
     * @param fields the fields map that was deserialized from JSON
     */
    @JsonCreator
    @SuppressWarnings("unchecked")
    private JtsRecord( @JsonProperty("ts") final Object ts, @JsonProperty("f") final Map<Integer, JtsField> fields ) {
        Assert.notNull( ts, "timestamp must be defined" );
        Assert.notEmpty( fields, "fields must not be empty" );

        if( ts instanceof String )
            this.ts = JodaTime.parseDateTimeISO( (String) ts );
        else if( ts instanceof Number )
            this.ts = new DateTime( ts, DateTimeZone.UTC );
        else if( ts instanceof Map )
            this.ts = (DateTime) JtsField.parseMapAsComplexValue( (Map<String, Object>) ts );
        else
            throw new IllegalArgumentException( "Invalid timestamp format" );

        for( Integer i : fields.keySet() ) {
            if( fields.get( i ) == null )
                this.f.put( i, JtsField.DELETE_FIELD );
            else
                this.f.put( i, fields.get( i ) );
        }

        this.index = HashBiMap.create();
    }


    /**
     * Parameter constructor.
     *
     * @param ts     the timestamp of the record
     * @param fields the fields map of the record
     */
    public JtsRecord( final DateTime ts, final Map<Integer, JtsField> fields, Map<Integer, T> index ) {
        this.ts = ts;
        this.f = fields;
        this.index = HashBiMap.create( index );
    }


    public JtsRecord( final DateTime ts, final Map<Integer, JtsField> fields ) {
        this( ts, fields, new TreeMap<Integer, T>() );
    }


    /**
     * Copy constructor.
     *
     * @param other the record to copy
     */
    public JtsRecord( final JtsRecord<T> other ) {
        this( other, other.index );
    }


    public JtsRecord( final JtsRecord<?> other, Map<Integer, T> index ) {
        this.ts = other.ts;

        // Defensive copy
        this.f = new TreeMap<>();

        for( Integer i : other.getFields().keySet() )
            this.f.put( i, new JtsField( other.getFields().get( i ) ) );

        this.index = HashBiMap.create( index );
    }


    /**
     * Parameter constructor for one or more {@link JtsField} map values.
     *
     * @param ts     the timestamp of the record
     * @param fields one or more {@link JtsField} map values
     */
    public JtsRecord( final DateTime ts, final JtsField... fields ) {
        this.ts = ts;
        this.f = new TreeMap<>();

        for( int i = 0; i < fields.length; i++ )
            this.f.put( i, fields[i] );

        this.index = HashBiMap.create();
    }


    public JtsRecord( Entry<DateTime, Map<Integer, JtsField>> entry, Map<Integer, T> index ) {
        this( entry.getKey(), entry.getValue(), index );
    }


    private final Integer getColumnIndex( T id ) {
        return this.index.inverse().get( id );
    }


    /**
     * Returns the String value of the record, with the fields map formatted as JSON
     */
    @Override
    public String toString() {
        return this.ts.toString() + "->" +
                this.f.values().stream()
                .map( field -> field.toString() )
                .collect( Collectors.joining(",") );
    }


    /**
     * @return the timestamp
     */
    public DateTime getTimestamp() {
        return ts;
    }


    /**
     * @return the fields
     */
    public Map<Integer, JtsField> getFields() {
        return f;
    }


    public boolean hasField( final Integer index ) {
        return this.f.containsKey( index );
    }


    public JtsField getField( final Integer index ) {
        return this.f.get( index );
    }


    public JtsField getField( final T id ) {
        Integer i = getColumnIndex( id );

        if( i == null )
            return null;
        else
            return this.f.get( i );
    }


    public DataType getType(final Integer index ) {
        if( this.f.containsKey( index ) )
            return this.f.get( index ).getDataType();
        else
            return null;
    }


    public JtsSample getSample( final Integer index ) {
        if( this.hasField( index ) )
            return new JtsSample( this.getTimestamp(), this.getField( index ) );
        else
            return null;
    }


    /**
     * Returns the value from the {@link JtsField} matching the given field index, or null if the fields map does not contain the given
     * field index
     *
     * @param fieldIndex the field index
     * @return the value from the {@link JtsField} matching the given field index, or null if the fields map does not contain the given
     * field
     * index
     */
    public Object getValue( final Integer fieldIndex ) {
        // Defensive copy not required because JtsField is immutable
        JtsField field = this.f.get( fieldIndex );

        if( field == null )
            return null;
        else
            return field.getValue();
    }


    /**
     * Returns the quality from the {@link JtsField} matching the given field index, or null if the fields map does not contain the given
     * field index
     *
     * @param fieldIndex the field index
     * @return the quality from the {@link JtsField} matching the given field index, or null if the fields map does not contain the given
     * field index
     */
    public Integer getQuality( final int fieldIndex ) {
        // Defensive copy not required because JtsField is immutable
        JtsField field = this.f.get( fieldIndex );

        if( field == null )
            return null;
        else
            return field.getQuality();
    }


    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode( this.f, this.ts );
    }


    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( final Object other ) {
        JtsRecord<?> otherRecord = (JtsRecord<?>) other;
        return Objects.equal( this.f, otherRecord.f ) &&
                Objects.equal( this.ts, otherRecord.ts );
    }
}
