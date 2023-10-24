package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Objects;
import com.google.common.collect.*;
import io.eagle.util.Assert;
import io.eagle.util.DataType;
import io.eagle.util.WriteMode;
import io.eagle.util.geo.Coordinates;
import org.bson.BasicBSONObject;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Table containing {@link JtsField} values, referenced by {@link DateTime} rows and {@link Integer} column numbers; part of the JSON Time
 * Series document specification.
 * <p>
 * Internally, the table is implemented as a {@code TreeMap<R, TreeMap<C, V>>} where the rows are {@link Integer} and the columns are
 * {@link DateTime}; however, once created, all references to the internal table are via {@link Tables#transpose(Table)} that flips its row
 * and column keys, thus providing {@link DateTime} rows and {@link Integer} column numbers.
 * <p>
 * <b>Note, all references to rows and columns in the subsequent documentation implicitly refer to the transposed table, with
 * {@link DateTime} rows and {@link Integer} column numbers.</b>
 * <p>
 * <h3>Example: Internal Structure (never referenced directly)</h3>
 *
 * <pre>
 *    |  2014-05-26T19:15:00Z  |  2014-05-26T19:30:00Z  |  2014-05-26T19:45:00Z  |
 * ---|------------------------|------------------------|------------------------|
 *  0 |   {"v":2.72, "q":0}    |   {"v":3.14, "q":0}    |   {"v":6.28, "q":0}    |
 * ---|------------------------|------------------------|------------------------|
 *  1 |   {"v":"foo", "q":0}   |   {"v":"bar", "q":0}   |   {"v":"dog", "q":0}   |
 * ---|------------------------|------------------------|------------------------|
 *  2 |   {"v":true, "q":0}    |   {"v":false, "q":0}   |   {"v":true, "q":0}    |
 * ---|------------------------|------------------------|------------------------|
 * </pre>
 * <p>
 * <h3>Example: Transposed Structure (the only way the table is referenced in JtsTable methods, and implied in all method javadoc)</h3>
 *
 * <pre>
 *                        |           0            |           1            |           2            |
 * -----------------------|------------------------|------------------------|------------------------|
 *  2014-05-26T19:15:00Z  |   {"v":2.72, "q":0}    |   {"v":"foo", "q":0}   |   {"v":true, "q":0}    |
 * -----------------------|------------------------|------------------------|------------------------|
 *  2014-05-26T19:30:00Z  |   {"v":3.14, "q":0}    |   {"v":"bar", "q":0}   |   {"v":false, "q":0}   |
 * -----------------------|------------------------|------------------------|------------------------|
 *  2014-05-26T19:45:00Z  |   {"v":6.28, "q":0}    |   {"v":"dog", "q":0}   |   {"v":true, "q":0}    |
 * -----------------------|------------------------|------------------------|------------------------|
 * </pre>
 *
 * @author <a href="mailto:jarrah@argos.io">Jarrah Watson</a>
 */
public final class JtsTable<T> extends ForwardingTable<DateTime, Integer, JtsField> implements JtsData {


    private static final Logger logger = LoggerFactory.getLogger( JtsTable.class );

    /**
     * Inner table implemented as {@code TreeMap<R, TreeMap<C, V>>} where the rows are {@link Integer} and the columns are {@link DateTime}
     */
    @JsonIgnore
    private final TreeBasedTable<Integer, DateTime, JtsField> innerTable;

    @JsonIgnore
    private final BiMap<Integer, T> index;

    @JsonIgnore
    private final Map<Integer, DataType> types;

    /**
     * Default constructor.
     */
    public JtsTable() {
        this.innerTable = TreeBasedTable.create();
        this.index = HashBiMap.create();
        this.types = Maps.newHashMap();
    }


    /**
     * Parameter constructor which creates rows (given as a collection of {@link DateTime}s) and columns (given as a collection of
     * {@link Integer}s), and populates the new table with {@link JtsField}s constructed with the given initializer value.
     *
     * @param rowKeys     a collection of {@link DateTime}s defining the table rows, never null
     * @param columnKeys  a collection of {@link Integer}s defining the table columns, never null
     * @param initializer an T used as the value of every new {@link JtsField} which will populate the new table; must be
     *                    {@link Double}, {@link String}, {@link DateTime}, {@link Coordinates}, {@link Date}, {@link BasicBSONObject}, or null
     */
    public JtsTable( Collection<DateTime> rowKeys, Collection<Integer> columnKeys, Object initializer ) {
        this();

        for( DateTime ts : rowKeys ) {
            for( Integer column : columnKeys )
                this.put( ts, column, new JtsField( initializer ) );
        }
    }


    public JtsTable( Integer rows, Integer columns, Object initValue, Integer initQuality, String initAnnotation ) {
        this();

        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < columns; j++ )
                this.put( new DateTime( i ), j, new JtsField( initValue, initQuality, initAnnotation ) );
        }
    }


    /**
     * Parameter constructor which puts the given {@link JtsSample}s into the specified column.
     *
     * @param column  the column number to put the JtsSamples into
     * @param samples one or more JtsSamples to put into the table column, never null, must not contain any null elements
     * @see #putSamples(Integer, JtsSample...)
     */
    public JtsTable( final Integer column, final JtsSample... samples ) {
        this();
        Assert.notNull( samples );
        Assert.noNullElements( samples );
        this.putSamples( column, samples );
    }


    /**
     * Parameter constructor which puts the given mapping of {@link DateTime} to {@link JtsField} into the specified column.
     *
     * @param records the mapping of {@link DateTime} to {@link JtsField} to put into the table column, never null
     */
    public JtsTable( final SortedMap<DateTime, JtsField> records ) {
        this();
        Assert.notNull( records );
        this.putColumn( 0, records );
    }


    public JtsTable( final T id, final SortedMap<DateTime, JtsField> records ) {
        this();
        Assert.notNull( records );
        this.putColumn( id, records );
    }


    /**
     * Parameter constructor which puts all non-null {@link JtsField} values in the given {@link JtsRecord}s into the table.
     *
     * @param records one or more JtsRecords containing JtsFields to put into the table, never null, must not contain any null elements
     */
    public JtsTable( final JtsRecord<T>... records ) {
        this();
        Assert.notNull( records );
        Assert.noNullElements( records );
        this.putRecordsByColumn( Lists.newArrayList( records ) );
    }


    /**
     * Copy constructor.
     *
     * @param otherTable the other table, never null
     */
    public JtsTable( final JtsTable<T> otherTable ) {
        Assert.notNull( otherTable );
        this.innerTable = TreeBasedTable.create( otherTable.innerTable );
        this.index = HashBiMap.create( otherTable.index );
        this.types = Maps.newHashMap( otherTable.types );
    }


    public JtsTable( final JtsTable<?> otherTable, final Map<Integer, ? extends T> otherIndex ) {
        this.innerTable = TreeBasedTable.create( otherTable.innerTable );
        this.index = HashBiMap.create( otherIndex );
        this.types = Maps.newHashMap( otherTable.types );
    }

    public JtsTable( final NavigableMap<DateTime, Map<Integer, JtsField>> records, final Map<Integer, ? extends T> index ) {
        this.innerTable = TreeBasedTable.create();
        this.index = HashBiMap.create( index );
        this.types = Maps.newHashMap();

        for( DateTime ts : records.keySet() )
            this.putFields( ts, records.get( ts ) );
    }


    /**
     * Parameter constructor which puts all non-null {@link JtsField} values in the given {@link JtsRecord}s into the table.
     *
     * @param records List of JtsRecords containing JtsFields to put into the table, never null
     */
    @JsonCreator
    public JtsTable( List<JtsRecord<?>> records ) {
        this();
        Assert.notNull( records );
        this.putRecordsByColumn( records );
    }


    public JtsTable( Map<Integer, ? extends T> ids ) {
        this();
        this.index.putAll( ids );
    }

    public static void assertType( DataType type1, DataType type2 ) {
        if( type1 == null || type2 == null )
            return;

        Assert.isTrue( type1 == type2, "DataType mismatch!" );
    }

    public static SeriesChange mergeColumn( SortedMap<DateTime, JtsField> records, SortedMap<DateTime, JtsField> update, WriteMode writeMode ) {
        if( records == null ) {
            throw new IllegalArgumentException( "records must be defined" );
        }
        if( writeMode== null) {
         throw new IllegalArgumentException("writeMode must be defined" );
        }

        final SeriesChange changes = new SeriesChange();
        SortedMap<DateTime, JtsField> existing;

        // No changes if update document is empty
        if( update.isEmpty() )
            return SeriesChange.NO_CHANGE;

        switch( writeMode ) {
            // First delete any existing records falling within the period defined by the new records, then insert the new records
            case INSERT_DELETE_EXISTING:
                existing = records.subMap( update.firstKey(), update.lastKey().plusMillis( 1 ) );

                for( DateTime ts : Sets.newLinkedHashSet( Sets.union( existing.keySet(), update.keySet() ) ) ) {
                    if( records.containsKey( ts ) ) {
                        if( ! update.containsKey( ts ) || update.get( ts ).isDeleted() ) {
                            changes.addDeletedField();
                            records.remove( ts );
                        } else {
                            changes.addModifiedField( records.get( ts ), update.get( ts ) );
                            records.put( ts, update.get( ts ) );
                        }
                    } else {
                        changes.addInsertedField( update.get( ts ) );
                        records.put( ts, update.get( ts ) );
                    }
                }
                break;

            /*
             * If there are any existing records falling within the period defined by the new records, fail the operation without
             * modification; otherwise insert the new records
             */
            case INSERT_FAIL_ON_EXISTING:
                existing = records.subMap( update.firstKey(), update.lastKey().plusMillis( 1 ) );

                Assert.state( existing.isEmpty(), "Insert failed: existing records" );

                for( DateTime ts : update.keySet() ) {
                    if( update.get( ts ).isDeleted() )
                        continue;

                    changes.addInsertedField( update.get( ts ) );
                    records.put( ts, update.get( ts ) );
                }
                break;

            // Merge new records with existing records; on colliding timestamps, overwrite existing records
            case MERGE_OVERWRITE_EXISTING:
                for( final DateTime ts : update.keySet() ) {
                    if( update.get( ts ).isDeleted() ) {
                        if( records.containsKey( ts ) ) {
                            changes.addDeletedField();
                            records.remove( ts );
                        }
                    } else {
                        if( records.containsKey( ts ) )
                            changes.addModifiedField( records.get( ts ), update.get( ts ) );
                        else
                            changes.addInsertedField( update.get( ts ) );

                        records.put( ts, update.get( ts ) );
                    }
                }
                break;

            // Merge new records with existing records; on colliding timestamps, preserve existing records
            case MERGE_PRESERVE_EXISTING:
                for( final DateTime ts : update.keySet() ) {
                    if( records.containsKey( ts ) || update.get( ts ).isDeleted() )
                        continue;

                    changes.addInsertedField( update.get( ts ) );
                    records.put( ts, update.get( ts ) );
                }
                break;

            // Merge new records by overwriting field attributes with supplied sparse fields
            case MERGE_UPDATE_EXISTING:
                for( final DateTime ts : update.keySet() ) {
                    if( update.get( ts ).isDeleted() ) {
                        if( records.containsKey( ts ) ) {
                            changes.addDeletedField();
                            records.remove( ts );
                        }
                    } else if( records.containsKey( ts ) ) {
                        changes.addModifiedField( records.get( ts ), update.get( ts ) );
                        records.get( ts ).merge( update.get( ts ) );
                    } else {
                        changes.addInsertedField( update.get( ts ) );
                        records.put( ts, update.get( ts ) );
                    }
                }
                break;

            // Merge new records with existing records; on colliding timestamps, fail the operation without modification
            case MERGE_FAIL_ON_EXISTING:
                for( final DateTime ts : update.keySet() ) {
                    Assert.state( ! records.containsKey( ts ), "Merge failed: existing records" );

                    if( update.get( ts ).isDeleted() )
                        continue;

                    changes.addInsertedField( update.get( ts ) );
                    records.put( ts, update.get( ts ) );
                }
                break;

            // Delete records encapsulated by the given range; inclusive of start and end
            case DELETE_RANGE:
                existing = records.subMap( update.firstKey(), update.lastKey().plusMillis( 1 ) );
                existing.keySet().forEach( ts -> changes.addDeletedField() );
                existing.clear();
                break;

            // Delete specific records
            case DELETE:
                Sets.intersection( records.keySet(), update.keySet() ).forEach( ts -> changes.addDeletedField() );
                records.keySet().removeAll( update.keySet() );
                break;

            // Discard all records
            case DISCARD:
                break;

            default:
                throw new IllegalStateException( "Invalid write mode: " + writeMode );
        }

        return changes;
    }

    public static void copyColumn( SortedMap<DateTime, JtsField> from, SortedMap<DateTime, JtsField> to ) {
        Assert.notNull( from );
        Assert.notNull( to );

        JtsField field = null;

        for( DateTime ts : from.keySet() ) {
            field = new JtsField( from.get( ts ) );
            to.put( ts, field );
        }
    }

    /**
     * Creates a new JtsTable based on the given CSV string.
     *
     * @param csv String containing CSV data, never null or empty
     * @return a new JtsTable based on the given CSV string
     */
    public static <T> JtsTable<T> fromCsv( final String csv ) {
        Assert.isNotEmpty( csv );

        DateTimeFormatter formatter = DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss" );
        JtsTable<T> jtsTable = new JtsTable<>();

        for( String record : csv.split( "\n" ) ) {
            try {
                String[] tokens = record.split( "," );
                DateTime ts = formatter.parseDateTime( tokens[0] );

                for( int i = 1; i < tokens.length; i++ ) {
                    Object value;

                    try {
                        // Attempt to parse String as Double
                        value = Double.parseDouble( tokens[i] );
                    } catch( Exception e1 ) {
                        try {
                            // String is not Double, attempt to parse String as DateTime
                            value = formatter.parseDateTime( tokens[i] );
                        } catch( Exception e2 ) {
                            // String is not Double or DateTime, keep as String value
                            value = tokens[i];
                        }
                    }

                    jtsTable.put( ts, i - 1, new JtsField( value ) );
                }
            } catch( Exception e ) {
            }
        }

        return jtsTable;
    }

    public static final <T> void moveRecords( JtsTable<T> from, JtsTable<T> to ) {
        Assert.notNull( from );
        Assert.notNull( to );

        moveRecords( from, to, from.recordCount() );
    }

    /**
     * Move records from one table to another.
     *
     * @param from  the table to move records from
     * @param to    the table to move records to
     * @param count the maximum number of records to move
     */
    public static final <T> void moveRecords( JtsTable<T> from, JtsTable<T> to, Integer count ) {
        Assert.notNull( from );
        Assert.notNull( to );
        Assert.notNull( count );

        Iterator<DateTime> it = from.table().rowKeySet().iterator();
        DateTime ts;
        Integer index = 0;

        while( it.hasNext() && index <= count ) {
            ts = it.next();
            to.putFields( ts, from.getFields( ts ) );
            index++;
        }
    }

    @Override
    protected Table<DateTime, Integer, JtsField> delegate() {
        return this.table();
    }

    public void addColumn( final SortedMap<DateTime, JtsField> records ) {
        addColumn( null, records );
    }

    public void addColumn( final T id, final SortedMap<DateTime, JtsField> records ) {
        SortedSet<Integer> columnIndexes = this.getColumnIndexes();
        Integer newColumnIndex = columnIndexes.isEmpty() ? 0 : columnIndexes.last() + 1;

        if( id != null )
            this.index.forcePut( newColumnIndex, id );

        this.putColumn( newColumnIndex, records );
    }

    public void addTable( final JtsTable<T> table ) {
        for( Integer column : table.getColumnIndexes() )
            this.addColumn( table.getColumnId( column ), table.getColumn( column ) );
    }

    @Override
    public void clear() {
        this.table().clear();
    }

    /**
     * Removes from the table all of its records that occur after the given end {@link DateTime} (exclusive).
     *
     * @param end the DateTime after which to remove records (exclusive), never null
     */
    public void clearAfter( final DateTime end ) {
        Assert.notNull( end );

        for( final Integer column : Sets.newHashSet( this.table().columnKeySet() ) )
            this.clearColumnAfter( column, end );
    }

    /**
     * Removes from the table all of its records that occur before the given start {@link DateTime} (exclusive).
     *
     * @param start the DateTime before which to remove records (exclusive), never null
     */
    public void clearBefore( final DateTime start ) {
        Assert.notNull( start );

        for( final Integer column : Sets.newHashSet( this.table().columnKeySet() ) )
            this.clearColumnBefore( column, start );
    }

    /**
     * Removes from the table all records prior to the given record number (exclusive).
     *
     * @param index record number before which to remove records (exclusive), never null
     */
    public void clearBefore( Integer index ) {
        Assert.notNull( index );
        Assert.isTrue( index > 0 );

        if( index >= this.recordCount() )
            this.innerTable.clear();
        else
            this.clearBefore( Iterables.get( this.table().rowKeySet(), index ) );
    }

    /**
     * Clears the column specified by the given column number, if it exists; after this operation, the column will still exist in the table
     * but will contain no records (an empty map).
     *
     * @param column the column number to clear (may not exist in the JtsTable)
     */
    public void clearColumn( final Integer column ) {
        // Existence check is not required; if no mappings in the table have the provided column key, an empty map is returned.
        this.table().column( column ).clear();
    }

    public void clearColumn( final T id ) {
        this.clearColumn( getColumnIndex( id ) );
    }

    /**
     * Removes from the specified column all of its records that occur after the given end {@link DateTime} (exclusive).
     *
     * @param column the column number to remove records from
     * @param end    the DateTime after which to remove records (exclusive), never null
     */
    public void clearColumnAfter( final Integer column, final DateTime end ) {
        Assert.notNull( end );
        this.getColumnModifiableAfter( column, end ).clear();
    }

    public void clearColumnAfter( final T id, final DateTime end ) {
        this.clearColumnAfter( getColumnIndex( id ), end );
    }

    /**
     * Removes from the specified column all of its records that occur before the given start {@link DateTime} (exclusive).
     *
     * @param column the column number to remove records from
     * @param start  the DateTime before which to remove records (exclusive), never null
     */
    public void clearColumnBefore( final Integer column, final DateTime start ) {
        Assert.notNull( start );
        this.getColumnModifiableBefore( column, start ).clear();
    }

    public void clearColumnBefore( final T id, final DateTime start ) {
        this.clearColumnBefore( getColumnIndex( id ), start );
    }

    /**
     * Clears any records occurring between the given start time (inclusive) and end time (exclusive) in the column specified by the given
     * column number.
     *
     * @param column the column number to clear records from (may not exist in the JtsTable)
     * @param start  the inclusive start time of records to clear, never null, never after end
     * @param end    the exclusive end time of records to clear, never null, never before start
     */
    public void clearColumnBetween( final Integer column, final DateTime start, final DateTime end ) {
        Assert.notNull( start );
        Assert.notNull( end );

        this.getColumnModifiable( column ).subMap( start, end ).clear();
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the number of records in the table
     */
    @Override
    public int recordCount() {
        return this.table().rowKeySet().size();
    }

    public int columnCount() {
        return this.table().columnKeySet().size();
    }

    public Duration duration() {
        if( this.table().isEmpty() )
            return null;
        else
            return new Duration( getFirstTimestamp(), getLastTimestamp() );
    }

    @SuppressWarnings("unused")
    private void assertIndex( T id ) {
        Assert.isTrue( this.index.inverse().containsKey( id ) );
    }

    private void ensureIndex( T id ) {
        if( ! this.index.inverse().containsKey( id ) ) {
            Integer column = this.index.isEmpty() ? 0 : Collections.max( this.index.keySet() ) + 1;
            this.index.forcePut( column, id );
        }
    }

    private boolean hasIndex() {
        return this.index != null && this.index.size() > 0;
    }

    /**
     * Returns equals based on the inner table.
     */
    @Override
    public boolean equals( Object obj ) {
        return Objects.equal( this.innerTable, ( (JtsTable<?>) obj ).innerTable );
    }

    /**
     * Returns an unmodifiable view of the specified column (represented as a mapping of DateTime to JtsField), or an empty map if the
     * column does not exist in the table.
     * <p>
     * If a modifiable view is required, use {@link #getColumnModifiable(Integer)} instead.
     *
     * @param column the number of the column to return
     * @return an unmodifiable view of the specified column (represented as a mapping of DateTime to JtsField), or an empty map if the
     * column does not exist in the table
     */
    public SortedMap<DateTime, JtsField> getColumn( final Integer column ) {
        return Collections.unmodifiableSortedMap( this.getColumnModifiable( column ) );
    }

    public SortedMap<DateTime, JtsField> getColumn( final T id ) {
        return getColumn( getColumnIndex( id ) );
    }

    /**
     * Returns an unmodifiable view of the specified column, but only including values which occur after the specified start time
     * (exclusive); if the column does not exist in the table, or if the start time is null, an empty map is returned.
     * <p>
     * The returned view is represented as a mapping of DateTime to JtsField. If a modifiable view is required, use
     * {@link #getColumnModifiableAfter(Integer, DateTime)} instead.
     *
     * @param column the number of the column to return
     * @param start  only return values which occur after this time (exclusive), may be null
     * @return an unmodifiable view of the specified column, but only including values which occur after the specified start time
     * (exclusive); if the column does not exist in the table, or if the start time is null, an empty map is returned
     */
    public SortedMap<DateTime, JtsField> getColumnAfter( final Integer column, final DateTime start ) {
        if( start == null )
            return Maps.newTreeMap();
        else
            return Collections.unmodifiableSortedMap( this.getColumnModifiableAfter( column, start ) );
    }

    public SortedMap<DateTime, JtsField> getColumnAfter( final T id, final DateTime start ) {
        return getColumnAfter( getColumnIndex( id ), start );
    }

    /**
     * Returns an unmodifiable view of the specified column, but only including values which occur before the specified end time
     * (exclusive); if the column does not exist in the table, or if the end time is null, an empty map is returned.
     * <p>
     * The returned view is represented as a mapping of DateTime to JtsField.
     * <p>
     * If a modifiable view is required, use {@link #getColumnModifiableBefore(Integer, DateTime)} instead.
     *
     * @param column the number of the column to return
     * @param end    only return values which occur before this time (exclusive), may be null
     * @return an unmodifiable view of the specified column, but only including values which occur before the specified end time
     * (exclusive); if the end time is null, an empty map is returned
     */
    public SortedMap<DateTime, JtsField> getColumnBefore( final Integer column, final DateTime end ) {
        if( end == null )
            return Maps.newTreeMap();
        else
            return Collections.unmodifiableSortedMap( this.getColumnModifiableBefore( column, end ) );
    }

    public SortedMap<DateTime, JtsField> getColumnBefore( final T id, final DateTime end ) {
        return getColumnBefore( getColumnIndex( id ), end );
    }

    /**
     * Returns an unmodifiable view of the specified column, but only including values which occur between the specified start time
     * (inclusive) and end time (exclusive).
     * <p>
     * If the start time is null, returns all values before the end time (exclusive).
     * <p>
     * If the end time is null, returns all values after the start time (inclusive).
     * <p>
     * If both the start and end times are null, returns an empty map.
     * <p>
     * If the column does not exist in the table, returns an empty map.
     * <p>
     * The returned view is represented as a mapping of DateTime to JtsField.
     *
     * @param column the number of the column to return
     * @param start  only return values which occur after this time (inclusive), may be null
     * @param end    only return values which occur before this time (exclusive), may be null
     * @return an unmodifiable view of the specified column, but only including values which occur between the specified start time
     * (inclusive) and end time (exclusive)
     */
    public SortedMap<DateTime, JtsField> getColumnBetween( final Integer column, final DateTime start, final DateTime end ) {
        if( start == null && end == null )
            return Maps.newTreeMap();
        else if( start == null )
            return this.getColumnBefore( column, end );
        else if( end == null ) {
            // To ensure inclusive start time, subtract one millisecond,
            // because the getColumnAfter method would otherwise be start exclusive
            return this.getColumnAfter( column, start.minusMillis( 1 ) );
        } else
            return Collections.unmodifiableSortedMap( this.getColumn( column ).subMap( start, end ) );
    }

    public SortedMap<DateTime, JtsField> getColumnBetween( final T id, final DateTime start, final DateTime end ) {
        return getColumnBetween( getColumnIndex( id ), start, end );
    }

    public SortedMap<DateTime, JtsField> getColumnDay( final T id, final LocalDate day ) {
        return getColumnDay( getColumnIndex( id ), day );
    }

    public SortedMap<DateTime, JtsField> getColumnDay( final Integer column, final LocalDate day ) {
        return getColumnBetween( column, day.toDateTimeAtStartOfDay( DateTimeZone.UTC ), day.plusDays( 1 ).toDateTimeAtStartOfDay( DateTimeZone.UTC ) );
    }

    public JtsField getColumnFirstField( final Integer column ) {
        final DateTime ts = this.getColumnFirstTimestamp( column );

        if( ts == null )
            return null;
        else
            return this.getColumn( column ).get( ts );
    }

    public JtsField getColumnFirstField( final T id ) {
        return getColumnFirstField( getColumnIndex( id ) );
    }

    /**
     * Returns the {@link JtsSample} representing the first value in the specified column, if the column exists and has at least one record,
     * otherwise returns null.
     *
     * @param column the number of the column to get the first sample
     * @return the {@link JtsSample} representing the first value in the specified column, if the column exists and has at least one record,
     * otherwise returns null
     */
    public JtsSample getColumnFirstSample( final Integer column ) {
        final DateTime ts = this.getColumnFirstTimestamp( column );

        if( ts == null )
            return null;
        else
            return new JtsSample( ts, this.getColumn( column ).get( ts ) );
    }

    public JtsSample getColumnFirstSample( final T id ) {
        return getColumnFirstSample( getColumnIndex( id ) );
    }

    /**
     * Returns the timestamp of the first record in the specified column, if the column exists and has at least one record, otherwise
     * returns null.
     *
     * @param column the number of the column to get the first timestamp
     * @return the timestamp of the first record in the specified column, if the column exists and has at least one record, otherwise
     * returns null
     */
    public DateTime getColumnFirstTimestamp( final Integer column ) {
        if( this.hasColumn( column ) && ! this.getColumn( column ).isEmpty() )
            return this.getColumn( column ).firstKey();
        else
            return null;
    }

    public DateTime getColumnFirstTimestamp( final T id ) {
        return getColumnFirstTimestamp( getColumnIndex( id ) );
    }

    public T getColumnId( Integer column ) {
        return this.index.get( column );
    }

    public Set<T> getColumnIds() {
        return this.index.inverse().keySet();
    }

    public SortedSet<DateTime> getColumnKeys( final Collection<Integer> columns ) {
        SortedSet<DateTime> columnKeys = Sets.newTreeSet();

        for( Integer column : columns )
            columnKeys.addAll( this.table().column( column ).keySet() );

        return columnKeys;
    }

    /**
     * Returns the {@link JtsSample} representing the last value in the specified column, if the column exists and has at least one record,
     * otherwise returns null.
     *
     * @param column the number of the column to get the last sample
     * @return the {@link JtsSample} representing the last value in the specified column, if the column exists and has at least one record,
     * otherwise returns null
     */
    public JtsSample getColumnLastSample( final Integer column ) {
        final DateTime ts = this.getColumnLastTimestamp( column );

        if( ts == null )
            return null;
        else
            return new JtsSample( ts, this.getColumn( column ).get( ts ) );
    }

    public JtsSample getColumnLastSample( final T id ) {
        return getColumnLastSample( getColumnIndex( id ) );
    }

    /**
     * Returns the timestamp of the last record in the specified column, if the column exists and has at least one record, otherwise
     * returns null.
     *
     * @param column the number of the column to get the last timestamp
     * @return the timestamp of the last record in the specified column, if the column exists and has at least one record, otherwise
     * returns null
     */
    public DateTime getColumnLastTimestamp( final Integer column ) {
        if( this.hasColumn( column ) && ! this.getColumn( column ).isEmpty() )
            return this.getColumn( column ).lastKey();
        else
            return null;
    }

    public DateTime getColumnLastTimestamp( final T id ) {
        return getColumnLastTimestamp( getColumnIndex( id ) );
    }

    /**
     * Returns a modifiable view of the specified column (represented as a mapping of DateTime to JtsField), or an empty map if the
     * column does not exist in the table.
     * <p>
     * If an unmodifiable view is required, use {@link #getColumn(Integer)} instead.
     *
     * @param column the number of the column to return
     * @return a modifiable view of the specified column (represented as a mapping of DateTime to JtsField), or an empty map if the
     * column does not exist in the table
     */
    public SortedMap<DateTime, JtsField> getColumnModifiable( final Integer column ) {
        return new JtsMap( (SortedMap<DateTime, JtsField>) this.table().column( column ), dataType -> setType( column, dataType ) );
    }

    public SortedMap<DateTime, JtsField> getColumnModifiable( final T id ) {
        return this.getColumnModifiable( getColumnIndex( id ) );
    }

    private final Integer getColumnIndex( T id ) {
        ensureIndex( id );
        return this.index.inverse().get( id );
    }

    private Set<Integer> getColumnIndexes( Set<T> ids) {
        return ids.stream().map( id -> getColumnIndex( id ) ).collect( Collectors.toSet());
    }

    /**
     * Returns an unmodifiable sorted set of column keys that have one or more values in the table.
     *
     * @return an unmodifiable sorted set of column keys that have one or more values in the table
     */
    public SortedSet<Integer> getColumnIndexes() {
        return Collections.unmodifiableSortedSet( (SortedSet<Integer>) this.table().columnKeySet() );
    }

    /**
     * Returns a modifiable view of the specified column, but only including values which occur after the specified start time
     * (exclusive); if the column does not exist in the table, an empty map is returned.
     * <p>
     * The returned view is represented as a mapping of DateTime to JtsField.
     * <p>
     * If an unmodifiable view is required, use {@link #getColumnAfter(Integer, DateTime)} instead.
     *
     * @param column the number of the column to return
     * @param start  only return values which occur after this time (exclusive), never null
     * @return a modifiable view of the specified column, but only including values which occur after the specified start time
     * (exclusive); if the column does not exist in the table, an empty map is returned
     */
    public SortedMap<DateTime, JtsField> getColumnModifiableAfter( final Integer column, final DateTime start ) {
        Assert.notNull( start );

        // Add one millisecond to the given DateTime, because tailMap is inclusive
        return this.getColumnModifiable( column ).tailMap( start.plusMillis( 1 ) );
    }

    /**
     * Returns a modifiable view of the specified column, but only including values which occur before the specified end time
     * (exclusive); if the column does not exist in the table, an empty map is returned.
     * <p>
     * The returned view is represented as a mapping of DateTime to JtsField.
     * <p>
     * If an unmodifiable view is required, use {@link #getColumnBefore(Integer, DateTime)} instead.
     *
     * @param column the number of the column to return
     * @param end    only return values which occur before this time (exclusive), never null
     * @return a modifiable view of the specified column, but only including values which occur before the specified end time
     * (exclusive); if the column does not exist in the table, an empty map is returned
     */
    public SortedMap<DateTime, JtsField> getColumnModifiableBefore( final Integer column, final DateTime end ) {
        Assert.notNull( end );
        return this.getColumnModifiable( column ).headMap( end );
    }

    public JtsTable<T> getColumns( final Collection<Integer> columns ) {
        JtsTable<T> table = new JtsTable<>();

        for( Integer index : columns )
            table.putColumn( index, this.getColumn( index ) );

        return table;
    }

    public JtsTable<T> getColumns( Set<T> columns) {
        return getColumns( getColumnIndexes(columns) );
    }

    /**
     * Locate the first non-null field value in the given column and return its {@code DataType}.
     */
    public DataType getColumnType( Integer column ) {
        if( getColumn( column ).size() > 0 )
            Assert.isTrue( this.types.containsKey( column ), "Column contains records but has no type defined: " + column );

        return this.types.get( column );
    }

    public DataType getColumnType( T id ) {
        return getColumnType( getColumnIndex( id ) );
    }

    /**
     * Returns the {@link JtsField} value at the specified column number and record timestamp, or null if it does not exist.
     *
     * @param column the number of the column to get the value from, never null
     * @param ts     the timestamp of the record to get the value from, never null
     * @return the {@link JtsField} value at the specified column number and record timestamp, or null if it does not exist
     */
    public JtsField getField( final Integer column, final DateTime ts ) {
        Assert.notNull( ts );
        return this.table().row( ts ).get( column );
    }

    public JtsField getField( final T id, final DateTime ts ) {
        return getField( getColumnIndex( id ), ts );
    }

    public boolean hasField( final Integer column, final DateTime ts ) {
        return getField( column, ts ) != null;
    }

    public boolean hasField( final T id, final DateTime ts ) {
        return getField( id, ts ) != null;
    }

    public JtsField getFieldBefore( final Integer column, final DateTime ts ) {
        Assert.notNull( ts );
        SortedMap<DateTime, JtsField> records = this.getColumnBefore( column, ts );

        if( records.isEmpty() )
            return null;
        else
            return records.get( records.lastKey() );
    }

    public JtsField getFieldBefore( final T id, final DateTime ts ) {
        return getFieldBefore( getColumnIndex( id ), ts );
    }

    /**
     * Returns the {@link JtsField} values in the record specified by the given timestamp, as a mapping of column to JtsField.
     * <p>
     * If there are no records matching the timestamp, an empty map is returned
     *
     * @param ts the timestamp of the record, never null
     * @return the {@link JtsField} values in the record specified by the given timestamp, as a mapping of column to JtsField; if there are
     * no records matching the timestamp, an empty map is returned
     */
    public Map<Integer, JtsField> getFields( final DateTime ts ) {
        Assert.notNull( ts );

        return this.table().row( ts );
    }

    public LocalDate getFirstDay( DateTimeZone timezone ) {
        Assert.notNull( timezone );
        DateTime firstTs = getFirstTimestamp();

        if( firstTs == null )
            return null;
        else
            return firstTs.withZone( timezone ).toLocalDate();
    }

    /**
     * Returns the first {@link JtsRecord} in the table, or null if there are no records in the table.
     *
     * @return the first {@link JtsRecord} in the table, or null if there are no records in the table
     */
    public JtsRecord<T> getFirstRecord() {
        final DateTime firstTs = this.getFirstTimestamp();

        if( firstTs == null )
            return null;
        else
            return new JtsRecord<>( firstTs, this.table().row( firstTs ), this.index );
    }

    /**
     * Returns the timestamp of the first record in the table, or null if there are no records in the table.
     *
     * @return the timestamp of the first record in the table, or null if there are no records in the table
     */
    @Override
    public DateTime getFirstTimestamp() {
        final SortedSet<DateTime> firstTimestamps = new TreeSet<>();

        for( final Integer column : this.table().columnKeySet() )
            firstTimestamps.add( this.getColumn( column ).firstKey() );

        if( firstTimestamps.isEmpty() )
            return null;
        else
            return firstTimestamps.first();
    }

    public LocalDate getLastDay( DateTimeZone timezone ) {
        Assert.notNull( timezone );
        DateTime lastTs = getLastTimestamp();

        if( lastTs == null )
            return null;
        else
            return lastTs.withZone( timezone ).toLocalDate();
    }

    /**
     * Returns the last {@link JtsRecord} in the table, or null if there are no records in the table.
     *
     * @return the last {@link JtsRecord} in the table, or null if there are no records in the table
     */
    public JtsRecord<T> getLastRecord() {
        final DateTime lastTs = this.getLastTimestamp();

        if( lastTs == null )
            return null;
        else
            return new JtsRecord<>( lastTs, this.table().row( lastTs ), this.index );
    }

    /**
     * Returns the timestamp of the last record in the table, or null if there are no records in the table.
     *
     * @return the timestamp of the last record in the table, or null if there are no records in the table
     */
    @Override
    public DateTime getLastTimestamp() {
        final SortedSet<DateTime> lastTimestamps = new TreeSet<>();

        for( final Integer column : this.table().columnKeySet() )
            lastTimestamps.add( this.getColumn( column ).lastKey() );

        if( lastTimestamps.isEmpty() )
            return null;
        else
            return lastTimestamps.last();
    }

    public Interval getRange() {
        DateTime startTs = getFirstTimestamp();
        DateTime endTs = getLastTimestamp();

        if( startTs == null || endTs == null )
            return null;
        else
            return new Interval( startTs, endTs );
    }

    /**
     * Returns an unmodifiable time ordered map of all records in the table, as a mapping of timestamp to column to {@link JtsField}.
     *
     * @return an unmodifiable time ordered map of all records in the table
     */
    public Map<DateTime, Map<Integer, JtsField>> getRecords() {
        return Collections.unmodifiableMap( this.table().rowMap() );
    }

    public JtsRecord<T> getRecordAt( int i ) {
        Map<DateTime, Map<Integer, JtsField>> records = getRecords();

        if( records.isEmpty() || records.size() <= i )
            return null;
        else
            return new JtsRecord<>( Iterables.get( getRecords().entrySet(), i ), this.index );
    }

    public JtsRecord<T> getRecordAfter( DateTime ts, boolean inclusive ) {
        Assert.notNull( ts );
        NavigableMap<DateTime, Map<Integer, JtsField>> records = getRecordsAfter( ts, inclusive );

        if( records.isEmpty() )
            return null;
        else
            return new JtsRecord<>( records.firstEntry(), this.index );
    }

    public JtsRecord<T> getRecordBefore( DateTime ts, boolean inclusive ) {
        Assert.notNull( ts );
        NavigableMap<DateTime, Map<Integer, JtsField>> records = getRecordsBefore( ts, inclusive );

        if( records.isEmpty() )
            return null;
        else
            return new JtsRecord<>( records.lastEntry(), this.index );
    }

    public NavigableMap<DateTime, Map<Integer, JtsField>> getRecordsAfter( DateTime ts, boolean inclusive ) {
        Assert.notNull( ts );
        return new TreeMap<>( this.table().rowMap() ).tailMap( ts, inclusive );
    }

    public NavigableMap<DateTime, Map<Integer, JtsField>> getRecordsBefore( DateTime ts, boolean inclusive ) {
        Assert.notNull( ts );
        return new TreeMap<>( this.table().rowMap() ).headMap( ts, inclusive );
    }

    public Map<DateTime, Map<Integer, JtsField>> getRecordsBetween( DateTime start, DateTime end ) {
        Assert.notNull( start );
        Assert.notNull( end );

        if( start.isAfter( end ) )
            return Maps.newTreeMap();
        else
            return new TreeMap<>( this.table().rowMap() ).subMap( start, end );
    }

    /**
     * Returns the {@link JtsSample} at the specified column and timestamp; if no such value exists in the table, a JtsSample consisting of
     * the given timestamp and a null JtsField value will be returned.
     *
     * @param column the number of the column containing the value
     * @param ts     the timestamp of the row containing the value, never null
     * @return the {@link JtsSample} at the specified column and timestamp; if no such value exists in the table, a JtsSample consisting of
     * the given timestamp and a null JtsField value will be returned
     */
    public JtsSample getSample( final Integer column, final DateTime ts ) {
        Assert.notNull( ts );

        return new JtsSample( ts, this.table().get( ts, column ) );
    }

    public JtsSample getSampleBefore( final T id, final DateTime ts ) {
        SortedMap<DateTime, JtsField> columnBefore = getColumnBefore( id, ts );

        if( columnBefore.isEmpty() )
            return null;
        else
            return new JtsSample( columnBefore.lastKey(), columnBefore.get( columnBefore.lastKey() ) );
    }

    public JtsSample getSampleAfter( final T id, final DateTime ts ) {
        SortedMap<DateTime, JtsField> columnAfter = getColumnAfter( id, ts );

        if( columnAfter.isEmpty() )
            return null;
        else
            return new JtsSample( columnAfter.firstKey(), columnAfter.get( columnAfter.firstKey() ) );
    }

    public BiMap<Integer, T> getIndex() {
        return this.index;
    }

    public T getIndex( Integer column ) {
        return this.index.get( column );
    }

    public <T> JtsTable<T> withIndex( Map<Integer, ? extends T> index ) {
        Assert.notEmpty( index );

        return new JtsTable<>( this, index );
    }

    public <T> JtsTable<T> withIndexEnumerated( Collection<? extends T> ids ) {
        BiMap index = HashBiMap.<Integer, T>create();
        Integer i = 0;

        for( T id : ids )
            index.forcePut( i++, id );

        return new JtsTable<>(this, index);
    }

    /**
     * Returns a collection of all values, which may contain duplicates. Changes to the returned collection will update the underlying
     * table, and vice versa.
     * <p>
     * The order of values is by column, i.e. all the values from column 0, then all the values from column 1, etc.
     *
     * @return a collection of all values
     */
    public Collection<JtsField> getValues() {
        return this.table().values();
    }

    /**
     * Returns true if this JtsTable contains a column specified by the given column number, otherwise returns false.
     *
     * @param column the column number to check for existence
     * @return true if this JtsTable contains a column specified by the given column number, otherwise false
     */
    public boolean hasColumn( final Integer column ) {
        return this.table().containsColumn( column );
    }

    /**
     * Returns hashCode based on the inner table.
     */
    @Override
    public int hashCode() {
        final Integer prime = 31;
        Integer result = 1;
        result = prime * result + ( ( innerTable == null ) ? 0 : innerTable.hashCode() );
        return result;
    }

    /**
     * Returns true if this JtsTable contains a record specified by the given DateTime, otherwise returns false.
     *
     * @param timestamp the record timestamp to check for existence
     * @return true if this JtsTable contains a record specified by the given DateTime, otherwise false
     */
    public boolean hasRecord( final DateTime timestamp ) {
        return this.table().containsRow( timestamp );
    }

    /**
     * Returns true if the table contains no mappings.
     *
     * @return true if the table contains no mappings
     */
    @Override
    public boolean isEmpty() {
        return this.table().isEmpty();
    }

    /**
     * Merges new single-value records into the column specified by the given column number; the given {@link WriteMode} determines
     * behavior when merging the new records, including when the new records have identical timestamps as existing values in the same
     * column.
     *
     * @param id        the column identifier to merge new records into (may not exist in the JtsTable)
     * @param records   the new records to merge into the column, never null
     * @param writeMode determines behavior when merging the new records, including when the new records have identical timestamps as
     *                  existing records; never null
     */
    public void mergeColumn( final T id, final SortedMap<DateTime, JtsField> records, final WriteMode writeMode ) {
        mergeColumn( getColumnModifiable( id ), records, writeMode );
    }

    public void mergeColumn( final Integer column, final SortedMap<DateTime, JtsField> records, final WriteMode writeMode ) {
        mergeColumn( getColumnModifiable( column ), records, writeMode );
    }

    public void mergeRecordByColumn( JtsRecord<?> record ) {
        Assert.notNull( record );

        if( hasRecord( record.getTimestamp() ) )
            record.getFields().forEach( ( column, field ) -> put( record.getTimestamp(), column, field ) );
        else
            putFields( record.getTimestamp(), record.getFields() );
    }

    public void mergeRecordById( JtsRecord<T> record ) {
        Assert.notNull( record );

        if( hasRecord( record.getTimestamp() ) )
            record.getFields().forEach( ( column, field ) -> put( record.getTimestamp(), getColumnId( column ), field ) );
        else
            putRecordById( record );
    }

    /**
     * Put the {@link JtsField} value into the table in the specified column and row.
     *
     * @param column the column number to put the value in, never null
     * @param ts     the timestamp of the row to put the value in, never null
     * @param field  the value to put into the table
     */
    @Override
    public JtsField put( final DateTime ts, final Integer column, final JtsField field ) {
        Assert.notNull( column );
        Assert.notNull( ts );
        Assert.notNull( field );

        setType( column, field.getDataType() );

        return super.put( ts, column, field );
    }

    public void put( final DateTime ts, final T id, final JtsField field ) {
        ensureIndex( id );
        put( ts, getColumnIndex( id ), field );
    }

    private void setType( Integer column, DataType fieldType ) {
        assertType( getColumnType( column ), fieldType );

        if( this.types.containsKey( column ) )
            this.types.replace( column, null, fieldType );
        else
            this.types.put( column, fieldType );
    }

    /**
     * Clears the column specified by the given column number, if it exists, then adds all the given records; after this operation, the
     * column will only contain the given records.
     *
     * @param column  the column number to clear and add records to (may not exist in the JtsTable)
     * @param records the records to add to the column after it is cleared, never null
     */
    public void putColumn( final Integer column, final SortedMap<DateTime, JtsField> records ) {
        Assert.notNull( records );

        clearColumn( column );

        for( DateTime ts : records.keySet() )
            put( ts, column, records.get( ts ) );
    }

    public void putColumn( final T id, final SortedMap<DateTime, JtsField> records ) {
        ensureIndex( id );
        putColumn( getColumnIndex( id ), records );
    }

    /**
     * Puts the given fields into the table; this operation always replaces an existing record
     * in the table with the same timestamp.
     *
     * @param ts     the timestamp of the record, never null
     * @param fields the fields map of the record, never null
     */
    public void putFields(final DateTime ts, final Map<Integer, JtsField> fields) {
        Assert.notNull( ts );
        Assert.notNull( fields );
        Assert.noNullElements( fields.values().toArray(), "fields map must not contain null values" );

        // Delete any existing record for this timestamp
        this.table().row( ts ).clear();

        for( final Integer column : fields.keySet() ) {
            assertType( this.getColumnType( column ), fields.get( column ).getDataType() );
            put( ts, column, fields.get( column ) );
        }
    }

    public void putRecordByColumn(JtsRecord<?> record) {
        Assert.notNull( record );
        this.putFields(record.getTimestamp(), record.getFields());
    }

    public void putRecordById(JtsRecord<T> record) {
        Assert.notNull( record );

        // Delete any existing record for this timestamp
        this.table().row( record.getTimestamp() ).clear();

        for( T id: Sets.intersection(this.index.values(), record.getIndex().values()) ) {
            put(record.getTimestamp(), id, record.getField(id));
        }
    }

    /**
     * Puts the given list of {@link JtsRecord}s into the table; this operation always replaces any existing records in the table with the
     * same timestamps.
     *
     * @param records the records to put, never null, must not contain any null elements
     */
    public void putRecordsById(final Collection<JtsRecord<T>> records) {
        Assert.notNull( records );

        for( final JtsRecord<T> record : records )
            this.putRecordById( record );
    }

    public void putRecordsByColumn(final Collection<JtsRecord<?>> records) {
        Assert.notNull( records );

        for( final JtsRecord<?> record: records )
            this.putRecordByColumn(record);
    }

    /**
     * Puts the given {@link JtsSample} into the column specified by the given column number; this operation always replaces any existing
     * value in the column with the same timestamp.
     *
     * @param column the column number to insert the sample into
     * @param sample the sample to put, never null
     */
    public void putSample(final Integer column, final JtsSample sample) {
        Assert.notNull( sample );

        assertType( getColumnType( column ), sample.getField().getDataType() );
        put( sample.getTimestamp(), column, sample.getField() );
    }

    /**
     * Puts one or more {@link JtsSample}s into the column specified by the given column number; this operation always replaces any existing
     * values in the column with the same timestamps.
     *
     * @param column  the column number to insert the sample into
     * @param samples the samples to put, never null, must not contain any null elements
     */
    public void putSamples( final Integer column, final JtsSample... samples ) {
        Assert.notNull( samples );
        Assert.noNullElements( samples );

        for( final JtsSample sample : samples )
            this.putSample( column, sample );
    }

    /**
     * Puts all records in the given table into the current table; ensures that columns sharing the same index also share the same type.
     *
     * @param other table to put, never null
     */
    public JtsTable<T> mergeTableById( final JtsTable<T> other, WriteMode writeMode ) {
        Assert.notNull( other );

        for( T id : Sets.intersection( this.getColumnIds(), other.getColumnIds() ) )
            assertType( this.getColumnType( id ), other.getColumnType( id ) );

        for( T id : other.getColumnIds() )
            mergeColumn( id, other.getColumn( id ), writeMode );

        return this;
    }

    public JtsTable<T> mergeTableByColumn( final JtsTable<?> other, WriteMode writeMode ) {
        Assert.notNull( other );

        for( Integer column : Sets.intersection( this.getColumnIndexes(), other.getColumnIndexes() ) )
            assertType( this.getColumnType( column ), other.getColumnType( column ) );

        for( Integer column : other.getColumnIndexes() )
            mergeColumn( column, other.getColumn( column ), writeMode );

        return this;
    }

    /**
     * Removes the column specified by the given column number, if it exists; after this operation, the column will not exist in the table.
     *
     * @param column the column number to remove (may not exist in the JtsTable)
     */
    public void removeColumn( final Integer column ) {
        // Existence check is not required; the column is only removed if present
        this.table().columnMap().remove( column );
    }

    public JtsTable<T> removeFirst( final Integer count ) {
        JtsTable<T> records = slice( 0, count );
        clearBefore( count );

        return records;
    }

    public Duration resolution() {
        if( this.table().isEmpty() )
            return null;
        else
            return duration().dividedBy( recordCount() );
    }

    /**
     * Removes all records occurring after the specified record count; if the current number of records is not larger than the specified
     * count, no action is taken.
     *
     * @param count the number of records to retain, counting from the start of the table; must be non-negative
     */
    public void retainFirst( final Integer count ) {
        Assert.isTrue( count >= 0 );

        // If retaining 0 records, clear the table
        if( count == 0 ) {
            this.table().clear();
        } else {
            final Set<DateTime> rowKeySet = this.table().rowKeySet();

            if( rowKeySet.size() > count )
                clearAfter( Iterables.get( rowKeySet, count - 1 ) );
        }
    }

    public void retainColumnFirst( final Integer column, final Integer count ) {
        SortedMap<DateTime, JtsField> records = getColumnModifiable( column );

        if( records.size() > count )
            clearColumnAfter( column, Iterables.get( records.keySet(), count - 1 ) );
    }

    public void retainColumnFirst( final T id, final Integer count ) {
        ensureIndex( id );
        retainColumnFirst( getColumnIndex( id ), count );
    }

    public void retainColumnLast( final Integer column, final Integer count ) {
        SortedMap<DateTime, JtsField> records = getColumnModifiable( column );

        if( records.size() > count )
            clearColumnBefore( column, Iterables.get( records.keySet(), records.keySet().size() - count ) );
    }

    public void retainColumnLast( final T id, final Integer count ) {
        ensureIndex( id );
        retainColumnLast( getColumnIndex( id ), count );
    }

    /**
     * Removes all records occurring before the specified record count; if the table size is not larger than the specified count, no action
     * is taken.
     *
     * @param count the number of records to retain, counting from the end of the table; must be non-negative
     */
    public void retainLast( final Integer count ) {
        Assert.isTrue( count >= 0 );

        // If retaining 0 records, clear the table
        if( count == 0 )
            this.table().clear();
        else {
            final Set<DateTime> rowKeySet = this.table().rowKeySet();

            if( rowKeySet.size() > count )
                clearBefore( Iterables.get( rowKeySet, rowKeySet.size() - count ) );
        }
    }

    /**
     * Returns whether the records in this table encapsulates the given timestamp (start and end inclusive).
     */
    public boolean encapsulates( DateTime time ) {
        DateTime first = this.getFirstTimestamp();
        DateTime last = this.getLastTimestamp();

        return ( time.equals( first ) || time.isAfter( first ) && ( time.equals( last ) || time.isBefore( last ) ) );
    }

    /**
     * Returns the number of row/column mappings in the table.
     *
     * @return the number of fields in the table
     */
    public int fieldCount() {
        return this.table().size();
    }

    public JtsTable<T> slice( DateTime start, DateTime end ) {
        JtsTable<T> table = new JtsTable<>( this );
        table.clearBefore( start );
        table.clearAfter( end );

        return table;
    }

    public JtsTable<T> slice( Integer offset, Integer length ) {
        Assert.notNull( offset );
        Assert.isTrue( offset >= 0 );
        Assert.isTrue( offset < this.recordCount() );
        Assert.notNull( length );
        Assert.isTrue( length >= 0 );

        if( length == 0 )
            return new JtsTable<>();

        DateTime start = Iterables.get( this.table().rowKeySet(), offset );
        DateTime end;

        if( offset + length > this.recordCount() )
            end = this.getLastTimestamp();
        else
            end = Iterables.get( this.table().rowKeySet(), offset + length - 1 );

        return slice( start, end );
    }

    /**
     * Creates a transposed view of the internal table that flips its row and column keys, thus providing {@link DateTime} rows and
     * {@link Integer} column numbers. <br>
     * <b>Once the internal table is created by a constructor, all references to the internal table must be
     * via this method.</b>
     *
     * @return the transposed view of the intern
     */
    private final Table<DateTime, Integer, JtsField> table() {
        return Tables.transpose( this.innerTable );
    }

    @Override
    public String toDelimitedText( JtsDocumentHeader header, DocumentFormat documentFormat, DateTimeZone timezone ) {
        StringBuilder sb = new StringBuilder();
        Map<DateTime, Map<Integer, JtsField>> records = this.getRecords();
        SortedSet<Integer> columnIndexes = this.getColumnIndexes();

        if( columnIndexes.isEmpty() )
            return sb.toString();

        Map<Integer, JtsField> fields;
        JtsField field;

        for( DateTime ts : records.keySet() ) {
            sb.append( JtsDocument.renderTime( ts, timezone, documentFormat.getTimeFormat() ) );

            fields = records.get( ts );

            // Must print a delimited entry for every field key, from zero to the max field key
            // If this record does not contain the current field key, only a delimiter will be printed
            for( int i = 0; i <= getColumnIndexes().last(); i++ ) {
                sb.append( documentFormat.getDelimiter() );

                field = fields.get( i );

                if( field == null )
                    field = JtsField.EMPTY_FIELD;

                if( field.getValue() != null )
                    sb.append( JtsDocument.renderValue( field.getValue(), header.getColumnFormat( i ), documentFormat, timezone ) );

                if( documentFormat.isQualityEnabled() )
                    sb.append( JtsDocument.renderQuality( field.getQuality(), documentFormat ) );

                if( documentFormat.isAnnotationsEnabled() )
                    sb.append( JtsDocument.renderAnnotation( field.getAnnotation(), documentFormat ) );
            }

            sb.append( "\n" );
        }

        return sb.toString();
    }

    @Override
    public String toFixedWidth( JtsDocumentHeader header, DocumentFormat documentFormat, DateTimeZone timezone ) {
        StringBuilder sb = new StringBuilder();
        Map<DateTime, Map<Integer, JtsField>> records = this.getRecords();
        SortedSet<Integer> columnIndexes = this.getColumnIndexes();

        if( columnIndexes.isEmpty() )
            return sb.toString();

        Integer highestColumnIndex = columnIndexes.last();

        for( DateTime ts : records.keySet() ) {
            sb.append( String.format( "%-30s", ts.withZone( timezone ) ) );

            Map<Integer, JtsField> fields = records.get( ts );

            for( int i = 0; i <= highestColumnIndex; i++ ) {
                if( fields.containsKey( i ) && fields.get( i ) != null ) {
                    JtsField jtsField = fields.get( i );

                    if( jtsField.getValue() != null )
                        sb.append( JtsDocument.renderValue( jtsField.getValue(), header.getColumnFormat( i ), documentFormat, timezone ) );

                    if( documentFormat.isQualityEnabled() )
                        sb.append( JtsDocument.renderQuality( jtsField.getQuality(), documentFormat ) );

                    if( documentFormat.isAnnotationsEnabled() )
                        sb.append( JtsDocument.renderAnnotation( jtsField.getAnnotation(), documentFormat ) );

                    if( sb.length() > 24 ) {
                        sb.setLength( 24 );
                        sb.append( "..." );
                    }
                }

                sb.append( String.format( "%-30s", sb.toString() ) );
            }

            sb.append( "\n" );
        }

        return sb.toString();
    }

    public String getSummary() {
        return String.format( "records: %d fields: %d columns: %d first: %s last: %s", this.recordCount(), this.fieldCount(), this.getColumnIndexes().size(), this.getFirstTimestamp(), this.getLastTimestamp() );
    }

    /**
     * Returns a String representation of the JtsTable.
     *
     * @return a String representation of the JtsTable
     */
    @Override
    public String toString() {
        return toString( 10 );
    }

    public String toString( int recordCount ) {
        final Integer columnWidth = 29;
        final StringBuilder sb = new StringBuilder();
        final Map<DateTime, Map<Integer, JtsField>> records = this.table().rowMap();

        sb.append( String.format( "%s\n", getSummary() ) );
        sb.append( String.format( "Index: %s\n", this.index ) );
        sb.append( String.format( "%39s", "" ) );

        // Header: column numbers
        for( final Integer column : this.table().columnKeySet() )
            sb.append( String.format( "%-" + columnWidth + "s", column ) );

        sb.append( "\n" );
        sb.append( String.format( "%39s", "" ) );

        // Header: column type
        for( final Integer column : this.table().columnKeySet() )
            sb.append( String.format( "%-" + columnWidth + "s", getColumnType( column ) ) );

        sb.append( "\n" );
        sb.append( String.format( "%39s", "" ) );

        // Header: column index
        for( final Integer column : this.table().columnKeySet() )
            sb.append( String.format( "%-" + columnWidth + "s", this.index.get( column ) ) );

        sb.append( "\n" );

        Iterable<DateTime> times = Iterables.limit( records.keySet(), recordCount );
        Integer count = 0;

        // Print first 5 records (or the entire table if there are less than recordCount records)
        for( DateTime ts : times ) {
            // Print timestamp of current record
            sb.append( String.format( "[%4d] %-32s", count++, ts ) );
            final Map<Integer, JtsField> record = records.get( ts );

            // Print each JtsField in current record
            for( final Integer column : this.table().columnKeySet() )
                sb.append( String.format( "%-" + columnWidth + "s", record.get( column ) ) );

            sb.append( "\n" );

            if( records.size() > recordCount && count >= ( recordCount / 2 ) )
                break;
        }

        if( records.size() > recordCount ) {
            sb.append( "...\n" );
            count = records.size() - ( recordCount / 2 );
            times = Sets.newTreeSet( Iterables.limit( Sets.newTreeSet( records.keySet() ).descendingSet(), recordCount / 2 ) );

            // Print last bunch records
            for( DateTime ts : times ) {
                // Print timestamp of current record
                sb.append( String.format( "[%4d] %-32s", count++, ts ) );
                final Map<Integer, JtsField> record = records.get( ts );

                // Print each JtsField in current record
                for( final Integer column : this.table().columnKeySet() )
                    sb.append( String.format( "%-" + columnWidth + "s", record.get( column ) ) );

                sb.append( "\n" );
            }
        }

        return sb.toString();
    }

    public String toJson() throws JsonProcessingException {
        return new JtsDocument( this ).toJsonPretty();
    }

    public DateTimeZone getZone() {
        DateTime firstTs = getFirstTimestamp();

        if( firstTs == null )
            return null;
        else
            return firstTs.getZone();
    }


    public JtsTable<T> withZone( DateTimeZone zone ) {
        JtsTable<T> t = new JtsTable<>( this.index );
        Map<DateTime, Map<Integer, JtsField>> records = this.getRecords();

        for( DateTime ts : records.keySet() )
            t.putFields( ts.withZone( zone ), records.get( ts ) );

        return t;
    }

}
