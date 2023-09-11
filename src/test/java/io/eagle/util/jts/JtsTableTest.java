package io.eagle.util.jts;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.eagle.util.WriteMode;
import io.eagle.util.jackson.JacksonUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for {@link JtsTable}.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
public class JtsTableTest {


    private static final Logger logger = LoggerFactory.getLogger( JtsTableTest.class );
    // Number of records in the test table
    private static final int RECORD_COUNT = 10;
    private static final int FIELD_COUNT = 30;
    // Column numbers of the test table
    private static final int COLUMN_0 = 0;
    private static final int COLUMN_1 = 1;
    private static final int COLUMN_2 = 2;
    // Non-existent column number
    private static final int COLUMN_99 = 99;
    // Timestamps of each record in the test table
    private static final DateTime TS_0 = new DateTime( 0 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_1 = new DateTime( 1 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_2 = new DateTime( 2 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_3 = new DateTime( 3 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_4 = new DateTime( 4 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_5 = new DateTime( 5 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_6 = new DateTime( 6 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_7 = new DateTime( 7 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_8 = new DateTime( 8 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_9 = new DateTime( 9 ).withZone( DateTimeZone.UTC );
    private static final DateTime TS_10 = new DateTime( 10 ).withZone( DateTimeZone.UTC );
    // All the record timestamps as an array
    private static final DateTime[] TS = { TS_0, TS_1, TS_2, TS_3, TS_4, TS_5, TS_6, TS_7, TS_8, TS_9, TS_10 };
    // String values of the fields in the table
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String DOG = "dog";
    // String value for new fields, not yet in the table
    private static final String NEW = "NEW";
    private static final String NEW_FOO = "NEW_FOO";
    private static final String NEW_BAR = "NEW_BAR";
    // JtsFields in the table
    private static final JtsField FOO_FIELD = new JtsField( FOO );
    private static final JtsField BAR_FIELD = new JtsField( BAR );
    private static final JtsField DOG_FIELD = new JtsField( DOG );
    // JtsField not in the table
    private static final JtsField NEW_FIELD = new JtsField( NEW );
    private static final JtsField NEW_FOO_FIELD = new JtsField( NEW_FOO );
    private static final JtsField NEW_BAR_FIELD = new JtsField( NEW_BAR );
    // Special JtsField "delete" field
    private static final JtsField DELETE_FIELD = JtsField.DELETE_FIELD;
    // Mapping of column => JtsField
    private static final Map<Integer, JtsField> FIELDS_MAP = Maps.newHashMap();
    /**
     * Exception used to test that expected exceptions are thrown
     */
    /**
     * The table used by tests.
     */
    JtsTable<?> masterTable;

    /**
     * Creates a simple table of 10 rows and 3 columns. For easy comparison, row numbers correspond with milliseconds since epoch of the
     * row DateTime, e.g. the third row is number 2, with a DateTime of 1970-01-01T00:00:00.002Z, and can be compared to
     * {@code new DateTime( 2 )}.
     *
     * <pre>
     *                                 0                        1                        2
     * 1970-01-01T00:00:00.000Z        foo:null                 bar:null                 dog:null
     * 1970-01-01T00:00:00.001Z        foo:null                 bar:null                 dog:null
     * 1970-01-01T00:00:00.002Z        foo:null                 bar:null                 dog:null
     * 1970-01-01T00:00:00.003Z        foo:null                 bar:null                 dog:null
     * 1970-01-01T00:00:00.004Z        foo:null                 bar:null                 dog:null
     * 1970-01-01T00:00:00.005Z        foo:null                 bar:null                 dog:null
     * 1970-01-01T00:00:00.006Z        foo:null                 bar:null                 dog:null
     * 1970-01-01T00:00:00.007Z        foo:null                 bar:null                 dog:null
     * 1970-01-01T00:00:00.008Z        foo:null                 bar:null                 dog:null
     * 1970-01-01T00:00:00.009Z        foo:null                 bar:null                 dog:null
     *
     * </pre>
     */
    @BeforeEach
    public void setUp() {
        this.masterTable = new JtsTable<>();

        for( int i = 0; i < RECORD_COUNT; i++ ) {
            Map<Integer, JtsField> fields = Maps.newHashMap();
            fields.put( COLUMN_0, new JtsField( FOO_FIELD ) );
            fields.put( COLUMN_1, new JtsField( BAR_FIELD ) );
            fields.put( COLUMN_2, new JtsField( DOG_FIELD ) );
            this.masterTable.putFields( TS[i], fields );
        }
    }


    /**
     * Tests parameter constructor {@link JtsTable#JtsTable(Integer, JtsSample...)}.
     */
    @Test
    public void testJtsTableWithColumnSamples() {
        // Create 10 JtsSamples, matching column 0 of the master table.
        JtsSample[] jtsSamples = new JtsSample[RECORD_COUNT];
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            jtsSamples[i] = new JtsSample( TS[i], FOO_FIELD );
        }

        // Use the parameter constructor to create a new table with the JtsSamples
        JtsTable<?> testTable = new JtsTable<>( COLUMN_0, jtsSamples );
        logger.debug( String.valueOf( testTable ) );

        // Check the number of columns and number of rows
        assertEquals( 1, testTable.getColumnIndexes().size(), "Number of columns should be 1" );
        assertTrue( testTable.getColumnIndexes().contains( 0 ), "Column index should be 0" );
        assertEquals( RECORD_COUNT, testTable.recordCount(), "Column not expected size" );

        // Compare the column to the first column of the master table
        assertEquals( this.masterTable.getColumn( COLUMN_0 ), testTable.getColumn( COLUMN_0 ), "Column not equal to master table" );

        assertEquals( 1, testTable.columnCount() );

        // Try to use constructor with null JtsSample; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            new JtsTable( COLUMN_0, (JtsSample) null );
        } );
    }


    /**
     * Tests parameter constructor {@link JtsTable#JtsTable(java.util.SortedMap)}.
     */
    @Test
    public void testJtsTableWithColumnFieldMap() {
        // Create fields map with 10 JtsFields, matching column 0 of the master table.
        SortedMap<DateTime, JtsField> fieldsMap = new TreeMap<>();

        for( int i = 0; i < RECORD_COUNT; i++ ) {
            fieldsMap.put( TS[i], FOO_FIELD );
        }

        // Use the parameter constructor to create a new table with the fields map
        JtsTable<?> testTable = new JtsTable<>( fieldsMap );
        logger.debug( String.valueOf( testTable ) );

        // Check the number of columns and number of rows
        assertEquals( 1, testTable.getColumnIndexes().size(), "Number of columns should be 1" );
        assertTrue( testTable.getColumnIndexes().contains( 0 ), "Column index should be 0" );

        // Compare the column to the first column of the master table
        assertEquals( this.masterTable.getColumn( COLUMN_0 ),
                testTable.getColumn( COLUMN_0 ),
                "Column should be equal to master table column 0" );

        // Try to use constructor with null fields map; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            new JtsTable<>( (SortedMap<DateTime, JtsField>) null );
        } );
    }


    /**
     * Tests parameter constructor {@link JtsTable#JtsTable(JtsRecord...)}.
     */
    @Test
    public void testJtsTableWithJtsRecords() {
        // Create 3 JtsFields, which will go into every record
        JtsField[] jtsFields = { FOO_FIELD, BAR_FIELD, DOG_FIELD };

        // Create 10 JtsRecords, matching the master table
        List<JtsRecord<?>> jtsRecords = Lists.newArrayList();

        for( int i = 0; i < RECORD_COUNT; i++ )
            jtsRecords.add( new JtsRecord<>( TS[i], jtsFields ) );

        // Use the parameter constructor to create a new table with the JtsRecords
        JtsTable<?> testTable = new JtsTable<>( jtsRecords );
        logger.debug( String.valueOf( testTable ) );

        // Compare the table to the master table
        assertEquals( this.masterTable, testTable, "Test table should be equal to master table" );

        // Try to use constructor with null JtsRecord; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            new JtsTable<>( (JtsRecord<?>) null );
        } );
    }


    /**
     * Tests copy constructor {@link JtsTable#JtsTable(JtsTable)}.
     */
    @Test
    public void testJtsTableWithJtsTable() {
        // Use the copy constructor to create a new table from the master table
        JtsTable<?> testTable = new JtsTable<>( masterTable );
        logger.debug( String.valueOf( testTable ) );

        // Compare the table to the master table
        assertEquals( this.masterTable, testTable, "Test table should be equal to master table" );

        // Try to use copy constructor with null table; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            new JtsTable<>( (JtsTable<Object>) null );
        } );
    }


    @Test
    public void testClearColumn() {
        // Make a copy of the master table
        JtsTable<?> testTable = new JtsTable<>( this.masterTable );

        // Clearing a column that does not exist should have no effect
        testTable.clearColumn( COLUMN_99 );

        // Test table should be unchanged
        assertEquals( this.masterTable, testTable, "Test table not equal to master table" );

        // Clear column 1
        testTable.clearColumn( COLUMN_1 );
        logger.debug( String.valueOf( testTable ) );

        // Check the test table now has 2 columns
        assertEquals( 2, testTable.getColumnIndexes().size(), "Test table should contain 2 columns" );

        // Check that column 1 is missing but columns 0 and 2 are still present
        assertEquals( 2, testTable.getColumnIndexes().size(), "Number of columns should be 2" );
        assertTrue( testTable.getColumnIndexes().contains( COLUMN_0 ), "Table should still contain column 0" );
        assertFalse( testTable.getColumnIndexes().contains( COLUMN_1 ), "Table should NOT contain column 1" );
        assertTrue( testTable.getColumnIndexes().contains( COLUMN_2 ), "Table should still contain column 2" );
    }


    /**
     * Tests {@link JtsTable#clearColumnBetween(Integer, DateTime, DateTime)}.
     */
    @Test
    public void testClearColumnBetween() {
        // Make a copy of the master table
        JtsTable<?> testTable = new JtsTable<>( this.masterTable );

        // Clearing a column that does not exist should have no effect
        testTable.clearColumnBetween( COLUMN_99, TS[3], TS[7] );

        // Test table should be unchanged
        assertEquals( this.masterTable, testTable, "Test table not equal to master table" );

        // Clear column 1 between 3 milliseconds (inclusive) and 7 milliseconds (exclusive)
        testTable.clearColumnBetween( COLUMN_1, TS[3], TS[7] );
        logger.debug( String.valueOf( testTable ) );

        // Check that column 1 is missing rows between 3 milliseconds (inclusive) and 7 milliseconds (exclusive)

        Map<DateTime, JtsField> column1 = testTable.getColumn( COLUMN_1 );

        assertEquals( BAR_FIELD, column1.get( TS[0] ), "Column 1 should retain 'bar' field in record 0" );
        assertEquals( BAR_FIELD, column1.get( TS[1] ), "Column 1 should retain 'bar' field in record 1" );
        assertEquals( BAR_FIELD, column1.get( TS[2] ), "Column 1 should retain 'bar' field in record 2" );
        assertFalse( column1.containsKey( TS[3] ), "Column 1 should NOT retain record 3" );
        assertFalse( column1.containsKey( TS[4] ), "Column 1 should NOT retain record 4" );
        assertFalse( column1.containsKey( TS[5] ), "Column 1 should NOT retain record 5" );
        assertFalse( column1.containsKey( TS[6] ), "Column 1 should NOT retain record 6" );
        assertEquals( BAR_FIELD, column1.get( TS[7] ), "Column 1 should retain 'bar' field in record 7" );
        assertEquals( BAR_FIELD, column1.get( TS[8] ), "Column 1 should retain 'bar' field in record 8" );
        assertEquals( BAR_FIELD, column1.get( TS[9] ), "Column 1 should retain 'bar' field in record 9" );

        // Check that columns 0 and 2 are still intact
        assertEquals( masterTable.getColumn( COLUMN_0 ), testTable.getColumn( COLUMN_0 ), "Column 0 should still match master table" );
        assertEquals( masterTable.getColumn( COLUMN_2 ), testTable.getColumn( COLUMN_2 ), "Column 2 should still match master table" );

        // Try to get column between null timestamps; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            this.masterTable.clearColumnBetween( COLUMN_1, null, null );
        } );
    }


    /**
     * Tests {@link JtsTable#getColumn(Integer)}.
     */
    @Test
    public void testFromCsv() {
        String csv = "2014-02-14 08:58:53,25.12,88.5,8.49,144.7,8.7,191,13.79\n2014-02-14 08:59:53,25.29,88.4,6.629,144.1,21.75,190.5,13.88\n2014-02-14 09:00:53,25.23,88.3,5.901,140.9,18.04,189.6,13.86";

        JtsTable<Object> jtsTable = JtsTable.fromCsv( csv );

        logger.debug( String.valueOf( jtsTable ) );
    }


    /**
     * Tests {@link JtsTable#getColumn(Integer)}.
     */
    @Test
    public void testGetColumn() {
        // Create fields map with 10 JtsFields, matching column 1 of the master table.
        SortedMap<DateTime, JtsField> expectedFieldsMap = new TreeMap<>();

        for( int i = 0; i < RECORD_COUNT; i++ ) {
            expectedFieldsMap.put( TS[i], BAR_FIELD );
        }

        // Get column 1 of the master table
        SortedMap<DateTime, JtsField> column1 = this.masterTable.getColumn( COLUMN_1 );
        logger.debug( String.valueOf( column1 ) );

        // Check the column from the master table matches the expected fields map
        assertEquals( expectedFieldsMap, column1, "Column 1 should match expected fields map" );

        // Try to clear the column; this should fail because it is unmodifiable
        assertThrows( UnsupportedOperationException.class, () -> {
            column1.clear();
        } );
    }


    /**
     * Tests {@link JtsTable#getColumnAfter(Integer, DateTime)}.
     */
    @Test
    public void testGetColumnAfter() {
        // Create fields map with 4 JtsFields, matching column 1 of the master table after record 5.
        SortedMap<DateTime, JtsField> expectedFieldsMap = new TreeMap<>();

        for( int i = 6; i < RECORD_COUNT; i++ ) {
            expectedFieldsMap.put( TS[i], BAR_FIELD );
        }

        // Get column 1 of the master table after record 5
        SortedMap<DateTime, JtsField> column1after5 = this.masterTable.getColumnAfter( 1, new DateTime( 5 ) );
        logger.debug( String.valueOf( column1after5 ) );

        // Check the partial column from the master table matches the expected fields map
        assertEquals( expectedFieldsMap, column1after5, "Column 1 after record 5 should match expected fields map" );

        // Try to clear the partial column; this should fail because it is unmodifiable
        assertThrows( UnsupportedOperationException.class, () -> {
            column1after5.clear();
        } );
    }


    /**
     * Tests {@link JtsTable#getColumnBefore(Integer, DateTime)}.
     */
    @Test
    public void testGetColumnBefore() {
        // Create fields map with 5 JtsFields, matching column 1 of the master table before record 5.
        SortedMap<DateTime, JtsField> expectedFieldsMap = new TreeMap<>();

        for( int i = 0; i <= 4; i++ ) {
            expectedFieldsMap.put( TS[i], BAR_FIELD );
        }

        // Get column 1 of the master table before record 5
        SortedMap<DateTime, JtsField> column1before5 = this.masterTable.getColumnBefore( COLUMN_1, TS[5] );
        logger.debug( String.valueOf( column1before5 ) );

        // Check the partial column from the master table matches the expected fields map
        assertEquals( expectedFieldsMap, column1before5, "Column 1 before record 5 should match expected fields map" );

        // Try to clear the partial column; this should fail because it is unmodifiable
        assertThrows( UnsupportedOperationException.class, () -> {
            column1before5.clear();
        } );
    }


    /**
     * Tests {@link JtsTable#getColumnBetween(Integer, DateTime, DateTime)}.
     */
    @Test
    public void testGetColumnBetween() {
        // Create fields map with 4 JtsFields, matching column 1 of the master table between records 3 (inclusive) and 7 (exclusive)
        SortedMap<DateTime, JtsField> expectedFieldsMap = new TreeMap<>();

        for( int i = 3; i < 7; i++ ) {
            expectedFieldsMap.put( TS[i], BAR_FIELD );
        }

        // Get column 1 of the master table between records 3 (inclusive) and 7 (exclusive)
        SortedMap<DateTime, JtsField> column1between3and7 = this.masterTable.getColumnBetween( COLUMN_1, TS[3], TS[7] );
        logger.debug( String.valueOf( column1between3and7 ) );

        // Check the partial column from the master table matches the expected fields map
        assertEquals( expectedFieldsMap, column1between3and7, "Column 1 between records 3 and 7 should match expected fields map" );

        // Try to clear the partial column; this should fail because it is unmodifiable
        assertThrows( UnsupportedOperationException.class, () -> {
            column1between3and7.clear();
        } );
    }


    /**
     * Tests {@link JtsTable#getColumnFirstSample(Integer)}.
     */
    @Test
    public void testGetColumnFirstSample() {
        // Create expected JtsSample matching the first sample in column 1 of the master table
        JtsSample expectedSample = new JtsSample( TS[0], BAR_FIELD );

        // Get the first sample from column 1 of the master table
        JtsSample firstSample = this.masterTable.getColumnFirstSample( COLUMN_1 );
        logger.debug( String.valueOf( firstSample ) );

        // Check the first sample from column 1 of the master table matches the expected sample
        assertEquals( expectedSample, firstSample, "Column 1 first sample should match expected sample" );
    }


    /**
     * Tests {@link JtsTable#getColumnFirstTimestamp(Integer)}.
     */
    @Test
    public void testGetColumnFirstTimestamp() {
        // Get the timestamp of the first sample from column 1 of the master table
        DateTime firstTimestamp = this.masterTable.getColumnFirstTimestamp( COLUMN_1 );
        logger.debug( String.valueOf( firstTimestamp ) );

        // Check the timestamp of the first sample from column 1 of the master table matches the expected timestamp
        assertEquals( TS[0], firstTimestamp, "Column 1 first timestamp should match expected timestamp" );
    }


    /**
     * Tests {@link JtsTable#getColumnIndexes()}.
     */
    @Test
    public void testGetColumnIndexes() {
        // Create expected set of column indexes
        SortedSet<Integer> expectedColumnIndexes = ImmutableSortedSet.<Integer>naturalOrder().add( COLUMN_0, COLUMN_1, COLUMN_2 ).build();

        // Get the column indexes of the master table
        SortedSet<Integer> columnIndexes = this.masterTable.getColumnIndexes();
        logger.debug( String.valueOf( columnIndexes ) );

        // Check the column indexes of the master table match the expected set
        assertEquals( expectedColumnIndexes, columnIndexes, "Column indexes should match expected set" );
    }


    /**
     * Tests {@link JtsTable#getColumnLastSample(Integer)}.
     */
    @Test
    public void testGetColumnLastSample() {
        // Create expected JtsSample matching the last sample in column 1 of the master table
        JtsSample expectedSample = new JtsSample( TS[9], BAR_FIELD );

        // Get the last sample from column 1 of the master table
        JtsSample lastSample = this.masterTable.getColumnLastSample( COLUMN_1 );
        logger.debug( String.valueOf( lastSample ) );

        // Check the last sample from column 1 of the master table matches the expected sample
        assertEquals( expectedSample, lastSample, "Column 1 last sample should match expected sample" );
    }


    /**
     * Tests {@link JtsTable#getColumnLastTimestamp(Integer)}.
     */
    @Test
    public void testGetColumnLastTimestamp() {
        // Get the timestamp of the last sample from column 1 of the master table
        DateTime lastTimestamp = this.masterTable.getColumnLastTimestamp( 1 );
        logger.debug( String.valueOf( lastTimestamp ) );

        assertEquals( TS[9], lastTimestamp, "Column 1 last timestamp should match expected timestamp" );
    }


    /**
     * Tests {@link JtsTable#getColumnModifiable(Integer)}.
     */
    @Test
    public void testGetColumnModifiable() {
        // Check that getting a non-existent column number returns an empty map
        assertTrue( this.masterTable.getColumnModifiable( COLUMN_99 ).isEmpty(), "Column 99 does not exist and should return empty map" );

        // Create fields map with 10 JtsFields, matching column 1 of the master table.
        SortedMap<DateTime, JtsField> expectedFieldsMap = new TreeMap<>();

        for( int i = 0; i < RECORD_COUNT; i++ ) {
            expectedFieldsMap.put( TS[i], BAR_FIELD );
        }

        // Get a modifiable version of column 1 from the master table
        SortedMap<DateTime, JtsField> column1 = this.masterTable.getColumnModifiable( COLUMN_1 );
        logger.debug( String.valueOf( column1 ) );

        // Check column 1 from the master table matches the expected fields map
        assertEquals( expectedFieldsMap, column1, "Column 1 should match expected fields map" );

        // Clear the modifiable column to prove this affects the backing table
        column1.clear();

        // Check that column 1 is missing but columns 0 and 2 are still present
        assertEquals( 2, this.masterTable.getColumnIndexes().size(), "Number of columns should be 2" );
        assertTrue( this.masterTable.getColumnIndexes().contains( COLUMN_0 ), "Table should still contain column 0" );
        assertFalse( this.masterTable.getColumnIndexes().contains( COLUMN_1 ), "Table should NOT contain column 1" );
        assertTrue( this.masterTable.getColumnIndexes().contains( COLUMN_2 ), "Table should still contain column 2" );
    }


    /**
     * Tests {@link JtsTable#getColumnModifiableAfter(Integer, DateTime)}.
     */
    @Test
    public void testGetColumnModifiableAfter() {
        // Create fields map with 4 JtsFields, matching column 1 of the master table after record 5.
        SortedMap<DateTime, JtsField> expectedFieldsMap = new TreeMap<>();

        for( int i = 6; i < RECORD_COUNT; i++ ) {
            expectedFieldsMap.put( TS[i], BAR_FIELD );
        }

        // Get column 1 of the master table after record 5
        SortedMap<DateTime, JtsField> column1after5 = this.masterTable.getColumnModifiableAfter( 1, new DateTime( 5 ) );
        logger.debug( String.valueOf( column1after5 ) );

        // Check the partial column from the master table matches the expected fields map
        assertEquals( expectedFieldsMap, column1after5, "Column 1 after record 5 should match expected fields map" );

        // Try to clear the partial column; this should work because it is modifiable
        column1after5.clear();

        // Check the column is now empty
        assertTrue( column1after5.isEmpty(), "Column 1 after being cleared should be empty" );
    }


    /**
     * Tests {@link JtsTable#getColumnModifiableBefore(Integer, DateTime)}.
     */
    @Test
    public void testGetColumnModifiableBefore() {
        // Create fields map with 5 JtsFields, matching column 1 of the master table before record 5.
        SortedMap<DateTime, JtsField> expectedFieldsMap = new TreeMap<>();

        for( int i = 0; i <= 4; i++ ) {
            expectedFieldsMap.put( TS[i], BAR_FIELD );
        }

        // Get column 1 of the master table before record 5
        SortedMap<DateTime, JtsField> column1before5 = this.masterTable.getColumnModifiableBefore( COLUMN_1, TS[5] );
        logger.debug( String.valueOf( column1before5 ) );

        // Check the partial column from the master table matches the expected fields map
        assertEquals( expectedFieldsMap, column1before5, "Column 1 before record 5 should match expected fields map" );

        // Try to clear the partial column; this should work because it is modifiable
        column1before5.clear();

        // Check the column is now empty
        assertTrue( column1before5.isEmpty(), "Column 1 after being cleared should be empty" );
    }


    /**
     * Tests {@link JtsTable#getField(Integer, DateTime)}.
     */
    @Test
    public void testGetField() {
        // Put a special field into the middle of the table
        Map<Integer, JtsField> expectedFieldsMap = Maps.newHashMap();
        JtsField expectedField = new JtsField( "xxx" );

        expectedFieldsMap.put( COLUMN_0, FOO_FIELD );
        expectedFieldsMap.put( COLUMN_1, expectedField );
        expectedFieldsMap.put( COLUMN_2, DOG_FIELD );

        this.masterTable.putFields( TS[5], expectedFieldsMap );
        logger.debug( String.valueOf( this.masterTable ) );

        // Get the new field from the master table
        JtsField actualField = this.masterTable.getField( COLUMN_1, TS[5] );

        // Check the new field in the master table is the same as expected
        assertEquals( expectedField, actualField, "Field at row 5 column 1 should equal test field" );

        // Try to get field with a null timestamp; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            this.masterTable.getField( COLUMN_1, null );
        } );
    }


    /**
     * Tests {@link JtsTable#getFields(DateTime)}.
     */
    @Test
    public void testGetFields() {
        // Put special test fields into the middle of the table
        Map<Integer, JtsField> expectedFieldsMap = Maps.newHashMap();

        expectedFieldsMap.put( COLUMN_0, new JtsField( "aaa" ) );
        expectedFieldsMap.put( COLUMN_1, new JtsField( "bbb" ) );
        expectedFieldsMap.put( COLUMN_2, new JtsField( "ccc" ) );

        this.masterTable.putFields( TS[5], expectedFieldsMap );
        logger.debug( String.valueOf( this.masterTable ) );

        // Get the new fields from the master table
        Map<Integer, JtsField> actualFields = this.masterTable.getFields( TS[5] );

        // Check the new fields in the master table are the same as expected
        assertEquals( expectedFieldsMap, actualFields, "Fields at row 5 should equal test fields" );

        // Try to get fields with a null timestamp; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            this.masterTable.getFields( null );
        } );
    }


    /**
     * Tests {@link JtsTable#getFirstRecord()}.
     */
    @Test
    @Disabled("legacy test")
    public void testGetFirstRecord() {
        // Create the expected first JtsRecord
        JtsRecord expectedFirstRecord = new JtsRecord( TS[0], FIELDS_MAP );

        // Get the first record from the master table
        JtsRecord actualRecord = this.masterTable.getFirstRecord();
        logger.debug( String.valueOf( actualRecord ) );

        // Check the first record from the master table matches the expected record
        assertEquals( expectedFirstRecord, actualRecord, "First record should match expected record" );
    }


    /**
     * Tests {@link JtsTable#getFirstTimestamp()}.
     */
    @Test
    public void testGetFirstTimestamp() {
        // Get the timestamp of the first record from the master table
        DateTime actualTimestamp = this.masterTable.getFirstTimestamp();
        logger.debug( String.valueOf( actualTimestamp ) );

        // Check the timestamp of the first record from the master table matches the expected timestamp
        assertEquals( TS[0], actualTimestamp, "First timestamp should match expected timestamp" );
    }


    /**
     * Tests {@link JtsTable#getLastRecord()}.
     */
    @Test
    @Disabled("legacy test")
    public void testGetLastRecord() {
        // Create the expected last JtsRecord
        JtsRecord<?> expectedLastRecord = new JtsRecord<>( TS[9], FIELDS_MAP );

        // Get the last record from the master table
        JtsRecord<?> actualRecord = this.masterTable.getLastRecord();
        logger.debug( String.valueOf( actualRecord ) );

        // Check the last record from the master table matches the expected record
        assertEquals( expectedLastRecord, actualRecord, "Last record should match expected record" );
    }


    /**
     * Tests {@link JtsTable#getLastTimestamp()}.
     */
    @Test
    public void testGetLastTimestamp() {
        // Get the timestamp of the last record from the master table
        DateTime actualTimestamp = this.masterTable.getLastTimestamp();
        logger.debug( String.valueOf( actualTimestamp ) );

        // Check the timestamp of the last record from the master table matches the expected timestamp
        assertEquals( TS[9], actualTimestamp, "Last timestamp should match expected timestamp" );
    }


    /**
     * Tests {@link JtsTable#getRecords()}.
     */
    @Test
    @Disabled("legacy test")
    public void testGetRecords() {
        // Construct the expected records as a mapping of timestamp => column => JtsField
        Map<DateTime, Map<Integer, JtsField>> expectedRecords = Maps.newHashMap();
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            expectedRecords.put( TS[i], FIELDS_MAP );
        }

        // Get all the records from the master table
        Map<DateTime, Map<Integer, JtsField>> actualRecords = this.masterTable.getRecords();

        // Check all the records from the master table match the expected records
        assertEquals( expectedRecords, actualRecords, "Records should match expected records" );
    }


    /**
     * Tests {@link JtsTable#getSample(Integer, DateTime)}.
     */
    @Test
    @Disabled("legacy test")
    public void testGetSample() {
        // For each record in the master table, construct 3 expected samples (one per column) and compare with the actual samples
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            JtsSample expectedSample = new JtsSample( TS[i], FOO );
            JtsSample actualSample = this.masterTable.getSample( COLUMN_0, TS[i] );
            assertEquals( expectedSample, actualSample, "Sample should match expected sample" );

            expectedSample = new JtsSample( TS[i], BAR );
            actualSample = this.masterTable.getSample( COLUMN_1, TS[i] );
            assertEquals( expectedSample, actualSample, "Sample should match expected sample" );

            expectedSample = new JtsSample( TS[i], DOG );
            actualSample = this.masterTable.getSample( COLUMN_2, TS[i] );
            assertEquals( expectedSample, actualSample, "Sample should match expected sample" );
        }

        // Try to get a sample with a null timestamp; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            this.masterTable.getSample( COLUMN_1, null );
        } );
    }


    /**
     * Tests {@link JtsTable#getValues()}.
     */
    @Test
    public void testGetValues() {
        // Construct list of all expected JtsField values
        // Note the order of values is by column, i.e. all the values from column 0, then column 1, then column 2.
        List<JtsField> expectedValues = Lists.newArrayList();
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            expectedValues.add( FOO_FIELD );
        }
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            expectedValues.add( BAR_FIELD );
        }
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            expectedValues.add( DOG_FIELD );
        }

        // Get all the values from the master table, as a List
        List<JtsField> actualValues = Lists.newArrayList( this.masterTable.getValues() );
        logger.debug( String.valueOf( actualValues ) );

        // Check all the values from the master table match the expected values
        assertEquals( expectedValues, actualValues, "JtsField values should match expected values" );
    }


    /**
     * Tests {@link JtsTable#hasColumn(Integer)}.
     */
    @Test
    public void testHasColumn() {
        assertFalse( this.masterTable.hasColumn( - 1 ), "Table should NOT have column -1" );
        assertTrue( this.masterTable.hasColumn( COLUMN_0 ), "Table should have column 0" );
        assertTrue( this.masterTable.hasColumn( COLUMN_1 ), "Table should have column 1" );
        assertTrue( this.masterTable.hasColumn( COLUMN_2 ), "Table should have column 2" );
        assertFalse( this.masterTable.hasColumn( COLUMN_99 ), "Table should NOT have column 99" );
    }


    /**
     * Tests {@link JtsTable#isEmpty()}.
     */
    @Test
    public void testIsEmpty() {
        // Check the test table is not empty
        assertTrue( ! this.masterTable.isEmpty(), "Table should NOT be empty" );

        // Check a newly created table is empty
        assertTrue( new JtsTable().isEmpty(), "Table should be empty" );
    }


    /**
     * Tests {@link JtsTable#mergeColumn(Integer, SortedMap, WriteMode)}.
     */
    @Test
    public void testMergeColumn() {
        // All tests will be on column 1, the "bar" column
        int columnNum = COLUMN_1;

        // Make a copy of column 1 from the master table
        SortedMap<DateTime, JtsField> originalColumn1 = Maps.newTreeMap( this.masterTable.getColumn( columnNum ) );

        // Create two new records, 3 and 7
        SortedMap<DateTime, JtsField> newRecords = Maps.newTreeMap();
        newRecords.put( TS[3], NEW_FIELD );
        newRecords.put( TS[7], NEW_FIELD );

        /*
         * INSERT_DELETE_EXISTING
         * This should delete column 1 of the master table between records 3 and 7 (inclusive) then insert new values at records 3 and 7
         */
        this.masterTable.mergeColumn( columnNum, newRecords, WriteMode.INSERT_DELETE_EXISTING );
        logger.debug( String.valueOf( this.masterTable ) );

        /*
         * We expect records 0, 1 and 2 to have the original "bar" field.
         * We expect record 3 to have the "new" field
         * We expect records 4, 5 and 6 to have a null field
         * We expect record 7 to have the "new" field
         * We expect records 8 and 9 to have the original "bar" field
         */
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[0] ), "Original value should exist in record 0" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[1] ), "Original value should exist in record 1" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[2] ), "Original value should exist in record 2" );
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, TS[3] ), "NEW value should exist in record 3" );
        assertNull( this.masterTable.getField( columnNum, TS[4] ), "null value should exist in record 4" );
        assertNull( this.masterTable.getField( columnNum, TS[5] ), "null value should exist in record 5" );
        assertNull( this.masterTable.getField( columnNum, TS[6] ), "null value should exist in record 6" );
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, TS[7] ), "NEW value should exist in record 7" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[8] ), "Original value should exist in record 8" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[9] ), "Original value should exist in record 9" );

        /*
         * MERGE_PRESERVE_EXISTING
         * Put the original "bar" column back in the table, but preserve existing values
         * This should result in the null records (4, 5 and 6) having the original "bar" value again
         */
        this.masterTable.mergeColumn( columnNum, originalColumn1, WriteMode.MERGE_PRESERVE_EXISTING );
        logger.debug( String.valueOf( this.masterTable ) );

        /*
         * We expect records 0, 1 and 2 to have the original "bar" field.
         * We expect record 3 to have the "new" field
         * We expect records 4, 5 and 6 to have the original "bar" field.
         * We expect record 7 to have the "new" field
         * We expect records 8 and 9 to have the original "bar" field
         */
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[0] ), "Original value should exist in record 0" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[1] ), "Original value should exist in record 1" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[2] ), "Original value should exist in record 2" );
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, TS[3] ), "NEW value should exist in record 3" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[4] ), "Original value should exist in record 4" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[5] ), "Original value should exist in record 5" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[6] ), "Original value should exist in record 6" );
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, TS[7] ), "NEW value should exist in record 7" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[8] ), "Original value should exist in record 8" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[9] ), "Original value should exist in record 9" );

        /*
         * INSERT_DELETE_EXISTING
         * Put the original "bar" column back in the table, replacing all existing records in column 1
         */
        this.masterTable.mergeColumn( columnNum, originalColumn1, WriteMode.INSERT_DELETE_EXISTING );
        logger.debug( String.valueOf( this.masterTable ) );

        // We expect the original "bar" field exists for all records in column 1
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[i] ), "Original value should exist in record '" + i + "'" );
        }

        /*
         * MERGE_OVERWRITE_EXISTING
         * This should overwrite records 3 and 7 with "NEW" values, but preserve the values in between (records 4, 5 and 6)
         */
        this.masterTable.mergeColumn( columnNum, newRecords, WriteMode.MERGE_OVERWRITE_EXISTING );
        logger.debug( String.valueOf( this.masterTable ) );

        /*
         * We expect records 0, 1 and 2 to have the original "bar" value.
         * We expect records 3 to have the "NEW" value
         * We expect records 4, 5 and 6 to have the original "bar" value.
         * We expect records 7 to have the "NEW" value
         * We expect records 8 and 9 to have the original "bar" value
         */
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[0] ), "Original value should exist in record 0" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[1] ), "Original value should exist in record 1" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[2] ), "Original value should exist in record 2" );
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, TS[3] ), "NEW value should exist in record 3" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[4] ), "Original value should exist in record 4" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[5] ), "Original value should exist in record 5" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[6] ), "Original value should exist in record 6" );
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, TS[7] ), "NEW value should exist in record 7" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[8] ), "Original value should exist in record 8" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[9] ), "Original value should exist in record 9" );

        /*
         * INSERT_DELETE_EXISTING
         * Put the original "bar" column back in the table, replacing all existing records in column 1
         */
        this.masterTable.mergeColumn( columnNum, originalColumn1, WriteMode.INSERT_DELETE_EXISTING );
        logger.debug( String.valueOf( this.masterTable ) );

        // We expect the original "bar" field exists for all records in column 1
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[i] ), "Original value should exist in record '" + i + "'" );
        }

        // Try to merge a null timestamp and fields map; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            this.masterTable.mergeColumn( COLUMN_1, null, null );
        } );
    }


    /**
     * Tests {@link JtsTable#mergeColumn(Integer, SortedMap, WriteMode)} using {@link WriteMode#MERGE_FAIL_ON_EXISTING};
     * expect an {@link IllegalStateException} containing "Merge failed: existing records".
     */
    @Test
    public void testMergeColumnMergeFailOnExisting() {
        // All tests will be on column 1, the "bar" column
        int columnNum = COLUMN_1;

        // Create two new records with timestamps 3 and 7, which will collide with existing records
        SortedMap<DateTime, JtsField> collidingRecords = Maps.newTreeMap();
        collidingRecords.put( TS[3], NEW_FIELD );
        collidingRecords.put( TS[7], NEW_FIELD );

        // Create two new records with timestamps 10 and 11, which will not collide with existing records
        SortedMap<DateTime, JtsField> nonCollidingRecords = Maps.newTreeMap();
        nonCollidingRecords.put( new DateTime( 10 ), NEW_FIELD );
        nonCollidingRecords.put( new DateTime( 11 ), NEW_FIELD );

        /*
         * MERGE_FAIL_ON_EXISTING
         * Put the non-colliding records in the table, which should not fail
         */
        this.masterTable.mergeColumn( columnNum, nonCollidingRecords, WriteMode.MERGE_FAIL_ON_EXISTING );
        logger.debug( String.valueOf( this.masterTable ) );

        // We expect the original "bar" field exists for all records in column 1
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[i] ), "Original value should exist in record '" + i + "'" );
        }

        // We expect the "new" field exists for the new non-colliding records in column 1
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, new DateTime( 10 ) ), "NEW value should exist in record 10" );
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, new DateTime( 11 ) ), "NEW value should exist in record 11" );

        /*
         * MERGE_FAIL_ON_EXISTING
         * Put the colliding records in the table, which should fail
         */
        assertThrows( IllegalStateException.class, () -> {
            this.masterTable.mergeColumn( columnNum, collidingRecords, WriteMode.MERGE_FAIL_ON_EXISTING );
        }, "Merge failed: existing records" );
    }


    /**
     * Tests {@link JtsTable#mergeColumn(Integer, SortedMap, WriteMode)} using {@link WriteMode#INSERT_FAIL_ON_EXISTING};
     * expect an {@link IllegalStateException} containing "Insert failed: existing records".
     */
    @Test
    public void testMergeColumnInsertFailOnExisting() {
        // All tests will be on column 1, the "bar" column
        int columnNum = 1;

        // Create two new records, 10 and 12, which define a range in the master table that currently does not have any records
        SortedMap<DateTime, JtsField> nonCollidingRecords = Maps.newTreeMap();
        nonCollidingRecords.put( new DateTime( 10 ), NEW_FIELD );
        nonCollidingRecords.put( new DateTime( 12 ), NEW_FIELD );

        /*
         * INSERT_FAIL_ON_EXISTING
         * Put the new records in the table, which should not fail
         */
        this.masterTable.mergeColumn( columnNum, nonCollidingRecords, WriteMode.INSERT_FAIL_ON_EXISTING );
        logger.debug( String.valueOf( this.masterTable ) );

        // We expect the original "bar" field exists for all records in column 1
        for( int i = 0; i < RECORD_COUNT; i++ ) {
            assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[i] ), "Original value should exist in record '" + i + "'" );
        }

        // We expect the "new" field exists for the new non-colliding records in column 1
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, new DateTime( 10 ) ), "NEW value should exist in record 10" );
        assertEquals( NEW_FIELD, this.masterTable.getField( columnNum, new DateTime( 12 ) ), "NEW value should exist in record 12" );

        // Create two new records, 11 and 13, which do not exist in the master table,
        // but which define a range in the master table that does contain a record (because record 12 exists)
        SortedMap<DateTime, JtsField> collidingRecords = Maps.newTreeMap();
        collidingRecords.put( new DateTime( 11 ), NEW_FIELD );
        collidingRecords.put( new DateTime( 13 ), NEW_FIELD );

        /*
         * MERGE_FAIL_ON_EXISTING
         * Insert records 11 and 13, which should fail because record 12 exists
         */
        logger.debug( "Inserting records which define a non-empty range; expect an IllegalStateException containing 'Insert failed: existing records'" );
        assertThrows( IllegalStateException.class, () -> {
            this.masterTable.mergeColumn( columnNum, collidingRecords, WriteMode.INSERT_FAIL_ON_EXISTING );
        }, "Insert failed: existing records" );
    }


    @Test
    public void testMergeColumnDelete() {
        // All tests will be on column 1, the "bar" column
        int columnNum = 1;

        // Create two new records, 10 and 12, which define a range in the master table that currently does not have any records
        SortedMap<DateTime, JtsField> nonCollidingRecords = Maps.newTreeMap();
        nonCollidingRecords.put( new DateTime( 10 ), NEW_FIELD );
        nonCollidingRecords.put( new DateTime( 12 ), NEW_FIELD );

        this.masterTable.mergeColumn( columnNum, nonCollidingRecords, WriteMode.DELETE_RANGE );
        logger.debug( String.valueOf( this.masterTable ) );

        // We expect the original "bar" field exists for all records in column 1
        for( int i = 0; i < RECORD_COUNT; i++ )
            assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[i] ), "Original value should exist in record '" + i + "'" );

        // Create two new records, 11 and 13, which do not exist in the master table,
        // but which define a range in the master table that does contain a record (because record 12 exists)
        SortedMap<DateTime, JtsField> collidingRecords = Maps.newTreeMap();
        collidingRecords.put( new DateTime( 8 ), NEW_FIELD );
        collidingRecords.put( new DateTime( 12 ), NEW_FIELD );

        this.masterTable.mergeColumn( 0, collidingRecords, WriteMode.DELETE_RANGE );
        this.masterTable.mergeColumn( 1, collidingRecords, WriteMode.DELETE_RANGE );
        this.masterTable.mergeColumn( 2, collidingRecords, WriteMode.DELETE_RANGE );

        logger.debug( String.valueOf( this.masterTable ) );

        // We expect the original "bar" field exists for all records in column 1
        for( int i = 0; i < 8; i++ )
            assertTrue( this.masterTable.hasRecord( TS[i] ), "Original record should exist @ '" + TS[i] + "'" );

        for( int i = 8; i < 12; i++ )
            assertFalse( this.masterTable.hasRecord( new DateTime( i ) ), "Original record should NOT exist @ '" + new DateTime( i ) + "'" );
    }


    /**
     * Tests {@link JtsTable#mergeColumn(Integer, SortedMap, WriteMode)} using {@link WriteMode#MERGE_UPDATE_EXISTING}.
     */
    @Test
    public void testMergeColumnUpdate() {
        // All tests will be on column 1, the "bar" column
        int columnNum = COLUMN_1;

        // Make a copy of column 1 from the master table
        SortedMap<DateTime, JtsField> newRecords = Maps.newTreeMap();

        // Add two new records, 1 and 3, with the special "delete" field
        newRecords.put( TS[1], DELETE_FIELD );
        newRecords.put( TS[3], DELETE_FIELD );

        // Update record 5 with just a new value (quality and annotation should remain unchanged)
        String expectedValue = FOO;
        JtsField BAR_FIELD_MODIFIED_VALUE_EXPECTED = new JtsField( BAR_FIELD );
        BAR_FIELD_MODIFIED_VALUE_EXPECTED.setValue( expectedValue );
        JtsField FIELD_VALUE_UPDATE = JacksonUtil.parseObject( "{\"v\":\"" + expectedValue + "\"}", JtsField.class );
        newRecords.put( TS[5], FIELD_VALUE_UPDATE );

        // Update record 7 with just a new quality (value and annotation should remain unchanged)
        int expectedQuality = 555;
        JtsField BAR_FIELD_MODIFIED_QUALITY_EXPECTED = new JtsField( BAR_FIELD );
        BAR_FIELD_MODIFIED_QUALITY_EXPECTED.setCombinedQuality( expectedQuality );
        JtsField FIELD_QUALITY_UPDATE = JacksonUtil.parseObject( "{\"q\":" + expectedQuality + "}", JtsField.class );
        newRecords.put( TS[7], FIELD_QUALITY_UPDATE );

        // Update record 9 with just a new annotation (value and quality should remain unchanged)
        String expectedAnnotation = "foobar annotation";
        JtsField BAR_FIELD_MODIFIED_ANNOTATION_EXPECTED = new JtsField( BAR_FIELD );
        BAR_FIELD_MODIFIED_ANNOTATION_EXPECTED.setAnnotation( expectedAnnotation );
        JtsField FIELD_ANNOTATION_UPDATE = JacksonUtil.parseObject( "{\"a\":\"" + expectedAnnotation + "\"}", JtsField.class );
        newRecords.put( TS[9], FIELD_ANNOTATION_UPDATE );

        // Update record 9 with just a new annotation (value and quality should remain unchanged)
        JtsField NEW_RECORD_EXPECTED = new JtsField( expectedValue, expectedQuality, expectedAnnotation );
        String json = String.format( "{\"v\":\"%s\", \"q\":%d, \"a\":\"%s\"}", expectedValue, expectedQuality, expectedAnnotation );
        JtsField NEW_RECORD_UPDATE = JacksonUtil.parseObject( json, JtsField.class );
        newRecords.put( TS[10], NEW_RECORD_UPDATE );

        logger.debug( "UPDATE: {}", new JtsTable<>( newRecords ) );

        /*
         * MERGE_UPDATE_EXISTING
         * This should delete records 1 and 3, and update value/quality/annotation for records 5, 7 and 9 respectively
         */
        this.masterTable.mergeColumn( columnNum, newRecords, WriteMode.MERGE_UPDATE_EXISTING );
        logger.debug( String.valueOf( this.masterTable ) );

        /*
         * We expect record 0 to have the original "bar" field.
         * We expect record 1 to be null
         * We expect record 2 to have the original "bar" field.
         * We expect record 3 to be null
         * We expect record 4 to have the original "bar" field.
         * We expect record 5 to have the original "bar" field but with a modified value.
         * We expect record 6 to have the original "bar" field.
         * We expect record 7 to have the original "bar" field but with a modified quality.
         * We expect record 8 to have the original "bar" field.
         * We expect record 9 to have the original "bar" field but with a modified annotation.
         */
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[0] ), "Original value should exist in record 0" );
        assertNull( this.masterTable.getField( columnNum, TS[1] ), "null value should exist in record 1" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[2] ), "Original value should exist in record 2" );
        assertNull( this.masterTable.getField( columnNum, TS[3] ), "null value should exist in record 3" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[4] ), "Original value should exist in record 4" );
        assertEquals( BAR_FIELD_MODIFIED_VALUE_EXPECTED, this.masterTable.getField( columnNum, TS[5] ), "Updated value should exist in record 5" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[6] ), "Original value should exist in record 6" );
        assertEquals( BAR_FIELD_MODIFIED_QUALITY_EXPECTED, this.masterTable.getField( columnNum, TS[7] ), "Updated quality should exist in record 7" );
        assertEquals( BAR_FIELD, this.masterTable.getField( columnNum, TS[8] ), "Original value should exist in record 8" );
        assertEquals( BAR_FIELD_MODIFIED_ANNOTATION_EXPECTED, this.masterTable.getField( columnNum, TS[9] ), "Updated annotation should exist in record 9" );
        assertEquals( NEW_RECORD_EXPECTED, this.masterTable.getField( columnNum, TS[10] ), "New record should exist in record 10" );
    }


    @Test
    public void testMergeTableById() {
        String a = "a";
        String b = "b";
        String c = "c";

        JtsTable<String> table1 = new JtsTable<>().withIndexEnumerated( Lists.newArrayList( a, b, c ) );
        JtsTable<String> table2 = new JtsTable<>().withIndexEnumerated( Lists.newArrayList( c, a, b ) );

        table1.mergeColumn( 0, this.masterTable.getColumn( 0 ), WriteMode.MERGE_FAIL_ON_EXISTING );
        table1.mergeColumn( 1, this.masterTable.getColumn( 1 ), WriteMode.MERGE_FAIL_ON_EXISTING );
        table1.mergeColumn( 2, this.masterTable.getColumn( 2 ), WriteMode.MERGE_FAIL_ON_EXISTING );

        table2.mergeTableById( table1, WriteMode.MERGE_OVERWRITE_EXISTING );

        logger.debug( "{}", table1 );
        logger.debug( "{}", table2 );

        assertEquals( table1.getColumn( 0 ), table2.getColumn( 1 ) );
        assertEquals( table1.getColumn( 1 ), table2.getColumn( 2 ) );
        assertEquals( table1.getColumn( 2 ), table2.getColumn( 0 ) );

    }


    @Test
    public void testPut() {
        JtsTable<?> emptyTable = new JtsTable<>();

        /*
         * Generate 100,000 random doubles.
         * With each value, create a new JtsField.
         * Put each field the table in random columns (between 0 and 9 inclusive) and random timestamps (between 0 and 99 inclusive)
         * For each value put in the table, check it exists in the table.
         */
        for( int i = 0; i < 100000; i++ ) {
            int columnNum = new Random().nextInt( 10 );
            DateTime timestamp = new DateTime( new Random().nextInt( 100 ) );
            JtsField jtsField = new JtsField( Math.random() * 100 );
            emptyTable.put( timestamp, columnNum, jtsField );

            assertEquals( jtsField, emptyTable.getField( columnNum, timestamp ), "New field should exist in table" );
        }

        logger.debug( String.valueOf( emptyTable ) );

        // Try to put a null timestamp and field; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            emptyTable.put( null, COLUMN_1, null );
        } );
    }


    /**
     * Tests {@link JtsTable#putColumn(Integer, SortedMap)}.
     * <p>
     * Using {@link JtsTable#putColumn(Integer, SortedMap)}, the table will be manipulated to look like this:
     *
     * <pre>
     * 0 1 2 3
     * 1970-01-01T00:00:00.000Z null null dog:null dog:null
     * 1970-01-01T00:00:00.001Z null null dog:null dog:null
     * 1970-01-01T00:00:00.002Z null null dog:null dog:null
     * 1970-01-01T00:00:00.003Z NEW_FOO:null null dog:null dog:null
     * 1970-01-01T00:00:00.004Z NEW_FOO:null null dog:null dog:null
     * 1970-01-01T00:00:00.005Z NEW_FOO:null null dog:null dog:null
     * 1970-01-01T00:00:00.006Z null null dog:null dog:null
     * 1970-01-01T00:00:00.007Z null null dog:null dog:null
     * 1970-01-01T00:00:00.008Z null null dog:null dog:null
     * 1970-01-01T00:00:00.009Z null NEW_BAR:null dog:null dog:null
     * 1970-01-01T00:00:00.010Z null NEW_BAR:null null null
     * 1970-01-01T00:00:00.011Z null NEW_BAR:null null null
     *
     * </pre
     */
    @Test
    public void testPutColumn() {
        // Create 3 new records, put in column 0 (use timestamps that already exist in the table)
        SortedMap<DateTime, JtsField> newColumn0 = Maps.newTreeMap();
        newColumn0.put( TS[3], NEW_FOO_FIELD );
        newColumn0.put( TS[4], NEW_FOO_FIELD );
        newColumn0.put( TS[5], NEW_FOO_FIELD );
        this.masterTable.putColumn( 0, newColumn0 );

        // Create 3 new records, put in column 1 (use timestamps that overlap the end of the current column)
        SortedMap<DateTime, JtsField> newColumn1 = Maps.newTreeMap();
        newColumn1.put( TS[9], NEW_BAR_FIELD );
        newColumn1.put( new DateTime( 10 ), NEW_BAR_FIELD );
        newColumn1.put( new DateTime( 11 ), NEW_BAR_FIELD );
        this.masterTable.putColumn( 1, newColumn1 );

        // Copy column 2 and put as column 3
        this.masterTable.putColumn( 3, this.masterTable.getColumn( 2 ) );
        logger.debug( String.valueOf( this.masterTable ) );

        // Check every record to make sure the master table now looks like the one in the method javadoc
        for( int i = 0; i < 3; i++ ) {
            assertNull( this.masterTable.getField( COLUMN_0, TS[i] ), "Value incorrect for column 0, record '" + i + "'" );
            assertNull( this.masterTable.getField( COLUMN_1, TS[i] ), "Value incorrect for column 1, record '" + i + "'" );
            assertEquals( DOG_FIELD, this.masterTable.getField( COLUMN_2, TS[i] ), "Value incorrect for column 2, record '" + i + "'" );
            assertEquals( DOG_FIELD, this.masterTable.getField( 3, TS[i] ), "Value incorrect for column 3, record '" + i + "'" );
        }

        for( int i = 3; i < 6; i++ ) {
            assertEquals( NEW_FOO_FIELD, this.masterTable.getField( COLUMN_0, TS[i] ), "Value incorrect for column 0, record '" + i + "'" );
            assertNull( this.masterTable.getField( COLUMN_1, TS[i] ), "Value incorrect for column 1, record '" + i + "'" );
            assertEquals( DOG_FIELD, this.masterTable.getField( COLUMN_2, TS[i] ), "Value incorrect for column 2, record '" + i + "'" );
            assertEquals( DOG_FIELD, this.masterTable.getField( 3, new DateTime( i ) ), "Value incorrect for column 3, record '" + i + "'" );
        }

        for( int i = 6; i < 9; i++ ) {
            assertNull( this.masterTable.getField( COLUMN_0, TS[i] ), "Value incorrect for column 0, record '" + i + "'" );
            assertNull( this.masterTable.getField( COLUMN_1, TS[i] ), "Value incorrect for column 1, record '" + i + "'" );
            assertEquals( DOG_FIELD, this.masterTable.getField( COLUMN_2, TS[i] ), "Value incorrect for column 2, record '" + i + "'" );
            assertEquals( DOG_FIELD, this.masterTable.getField( 3, TS[i] ), "Value incorrect for column 3, record '" + i + "'" );
        }

        assertNull( this.masterTable.getField( COLUMN_0, TS[9] ), "Value incorrect for column 0, record 9" );
        assertEquals( NEW_BAR_FIELD, this.masterTable.getField( COLUMN_1, TS[9] ), "Value incorrect for column 1, record 9" );
        assertEquals( DOG_FIELD, this.masterTable.getField( COLUMN_2, TS[9] ), "Value incorrect for column 2, record 9" );
        assertEquals( DOG_FIELD, this.masterTable.getField( 3, TS[9] ), "Value incorrect for column 3, record 9" );

        for( int i = 10; i < 12; i++ ) {
            assertNull( this.masterTable.getField( COLUMN_0, new DateTime( i ) ), "Value incorrect for column 0, record '" + i + "'" );
            assertEquals(
                    NEW_BAR_FIELD,
                    this.masterTable.getField( COLUMN_1, new DateTime( i ) ), "Value incorrect for column 1, record '" + i + "'"
            );
            assertNull( this.masterTable.getField( COLUMN_2, new DateTime( i ) ), "Value incorrect for column 2, record '" + i + "'" );
            assertNull( this.masterTable.getField( 3, new DateTime( i ) ), "Value incorrect for column 3, record '" + i + "'" );
        }

        // Try to put a null column; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            this.masterTable.putColumn( COLUMN_1, null );
        } );
    }


    @Test
    public void testPutRecord() {
        // Create a new record containing new fields only for columns 0 and 1
        SortedMap<Integer, JtsField> fieldsMap = Maps.newTreeMap();
        fieldsMap.put( COLUMN_0, NEW_FOO_FIELD );
        fieldsMap.put( COLUMN_1, NEW_BAR_FIELD );

        // Put the new record in the master table as record 5
        this.masterTable.putRecordByColumn( new JtsRecord<>(TS[5], fieldsMap) );
        logger.debug( String.valueOf( this.masterTable ) );

        // Check the new record has the expected fields: column 0 should have NEW_FOO, column 1 should have NEW_BAR, column 2 should be null
        assertEquals( NEW_FOO_FIELD, this.masterTable.getField( COLUMN_0, TS[5] ), "Value incorrect for column 0, record 5" );
        assertEquals( NEW_BAR_FIELD, this.masterTable.getField( COLUMN_1, TS[5] ), "Value incorrect for column 1, record 5" );
        assertNull( this.masterTable.getField( COLUMN_2, TS[5] ), "Value incorrect for column 2, record 5" );

    }


    @Test
    public void testPutRecordsCollection() {
        // Create a new record containing new fields only for columns 0 and 1
        SortedMap<Integer, JtsField> fieldsMap = Maps.newTreeMap();
        fieldsMap.put( COLUMN_0, NEW_FOO_FIELD );
        fieldsMap.put( COLUMN_1, NEW_BAR_FIELD );

        // Put the new record in the master table as record 5
        JtsRecord<?> jtsRecord = new JtsRecord<>( TS[5], fieldsMap );
        List<JtsRecord<?>> records = Lists.newArrayList( jtsRecord );
        this.masterTable.putRecordsByColumn( records );
        logger.debug( String.valueOf( this.masterTable ) );

        // Check the new record has the expected fields: column 0 should have NEW_FOO, column 1 should have NEW_BAR, column 2 should be null
        assertEquals( NEW_FOO_FIELD, this.masterTable.getField( COLUMN_0, TS[5] ), "Value incorrect for column 0, record 5" );
        assertEquals( NEW_BAR_FIELD, this.masterTable.getField( COLUMN_1, TS[5] ), "Value incorrect for column 1, record 5" );
        assertNull( this.masterTable.getField( COLUMN_2, TS[5] ), "Value incorrect for column 2, record 5" );

    }


    /**
     * Tests {@link JtsTable#putSample(Integer, JtsSample)}.
     */
    @Test
    public void testPutSample() {
        JtsTable<?> emptyTable = new JtsTable<>();

        /*
         * Generate 100000 random doubles.
         * With each value, create a new JtsField.
         * Put each field the table in random columns (between 0 and 9 inclusive) and random timestamps (between 0 and 99 inclusive)
         * For each value put in the table, check it exists in the table.
         */
        for( int i = 0; i < 100000; i++ ) {
            int columnNum = new Random().nextInt( 10 );
            DateTime timestamp = new DateTime( new Random().nextInt( 100 ) ).withZone( DateTimeZone.UTC );
            JtsField jtsField = new JtsField( Math.random() * 100 );
            JtsSample jtsSample = new JtsSample( timestamp, jtsField );
            emptyTable.putSample( columnNum, jtsSample );

            assertEquals( jtsField, emptyTable.getField( columnNum, timestamp ), "New field should exist in table" );
        }

        logger.debug( String.valueOf( this.masterTable ) );

        // Try to put a null sample; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            emptyTable.putSample( COLUMN_1, null );
        } );
    }


    /**
     * Tests {@link JtsTable#putSamples(Integer, JtsSample...)}.
     */
    @Test
    public void testPutSamples() {
        JtsTable<?> emptyTable = new JtsTable<>();

        /*
         * Generate 100000 random doubles.
         * With each value, create a new JtsField.
         * Put each field the table in random columns (between 0 and 9 inclusive) and random timestamps (between 0 and 99 inclusive)
         * For each value put in the table, check it exists in the table.
         */
        for( int i = 0; i < 100000; i++ ) {
            int columnNum = new Random().nextInt( 10 );
            DateTime timestamp = new DateTime( new Random().nextInt( 100 ) ).withZone( DateTimeZone.UTC );
            JtsField jtsField = new JtsField( Math.random() * 100 );
            JtsSample jtsSample = new JtsSample( timestamp, jtsField );
            emptyTable.putSamples( columnNum, jtsSample );

            assertEquals( jtsField, emptyTable.getField( columnNum, timestamp ), "New field should exist in table" );
        }

        logger.debug( String.valueOf( this.masterTable ) );

        // Try to put a null sample; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            emptyTable.putSamples( COLUMN_1, (JtsSample) null );
        } );
    }


    /**
     * Tests {@link JtsTable#removeColumn(Integer)}.
     */
    @Test
    public void testRemoveColumn() {
        for( int i = COLUMN_0; i <= COLUMN_2; i++ ) {
            // Check the table currently contains column i
            assertTrue( this.masterTable.hasColumn( i ), "Table should contain column '" + i + "'" );

            // Remove column i
            this.masterTable.removeColumn( i );
            logger.debug( this.masterTable.toString() );

            // Check the table no longer contains column i
            assertFalse( this.masterTable.hasColumn( i ), "Table should not contain column '" + i + "'" );
            assertEquals( 2 - i, this.masterTable.getColumnIndexes().size(), "Table should contain '" + ( 2 - i ) + "' columns" );
        }
    }


    /**
     * Tests {@link JtsTable#retainFirst(Integer)}.
     */
    @Test
    public void testRetainFirst() {
        // Retain all records in the table; this should have no effect
        this.masterTable.retainFirst( RECORD_COUNT );
        logger.debug( String.valueOf( this.masterTable ) );
        assertEquals( RECORD_COUNT, this.masterTable.recordCount(), "Table should still contain '" + RECORD_COUNT + "' records" );

        // Retain the first 5 records
        this.masterTable.retainFirst( 5 );
        logger.debug( String.valueOf( this.masterTable ) );

        // Check table size is now only 5 records
        assertEquals( 5, this.masterTable.recordCount(), "Table should contain 5 records" );

        // Check the table currently contains the first 5 records
        for( int i = 0; i <= 4; i++ ) {
            assertTrue( this.masterTable.getRecords().containsKey( TS[i] ), "Table should contain record '" + i + "'" );
        }

        // Check the table no longer contains the last 5 records
        for( int i = 5; i < RECORD_COUNT; i++ ) {
            assertFalse( this.masterTable.getRecords().containsKey( TS[i] ), "Table should NOT contain record '" + i + "'" );
        }

        // Try to retain with negative number; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            this.masterTable.retainFirst( - 1 );
        } );
    }


    /**
     * Tests {@link JtsTable#retainLast(Integer)}.
     */
    @Test
    public void testRetainLast() {
        // Retain all records in the table; this should have no effect
        this.masterTable.retainLast( RECORD_COUNT );
        logger.debug( String.valueOf( this.masterTable ) );
        assertEquals( RECORD_COUNT, this.masterTable.recordCount(), "Table should still contain '" + RECORD_COUNT + "' records" );

        // Retain the last 5 records
        this.masterTable.retainLast( 5 );
        logger.debug( String.valueOf( this.masterTable ) );

        // Check table size is now only 5 records
        assertEquals( 5, this.masterTable.recordCount(), "Table should contain 5 records" );

        // Check the table no longer contains the first 5 records
        for( int i = 0; i <= 4; i++ ) {
            assertFalse( this.masterTable.getRecords().containsKey( TS[i] ), "Table should NOT contain record '" + i + "'" );
        }

        // Check the table currently contains the last 5 records
        for( int i = 5; i < RECORD_COUNT; i++ ) {
            assertTrue( this.masterTable.getRecords().containsKey( TS[i] ), "Table should contain record '" + i + "'" );
        }

        // Try to retain with negative number; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            this.masterTable.retainLast( - 1 );
        } );
    }


    /**
     * Tests {@link JtsTable#recordCount()}.
     */
    @Test
    public void testRecordCount() {
        assertEquals( RECORD_COUNT, this.masterTable.recordCount(), "Table should contain '" + RECORD_COUNT + "' records" );
    }


    @Test
    public void testFieldCount() throws Exception {
        assertEquals( FIELD_COUNT, this.masterTable.fieldCount().intValue(), "Table should contain '" + FIELD_COUNT + "' fields" );
    }


    /**
     * Tests {@link JtsTable#clearColumnAfter(Integer, DateTime)}.
     */
    @Test
    public void testClearColumnAfter() {
        // Make a copy of the master table
        JtsTable testTable = new JtsTable( this.masterTable );

        // Trimming a column that does not exist should have no effect
        testTable.clearColumnAfter( COLUMN_99, TS[5] );

        // Compare the table to the master table; they should still be equal
        assertEquals( this.masterTable, testTable, "Test table not equal to master table" );

        // Trim column 1 of the test table after record 5
        testTable.clearColumnAfter( COLUMN_1, TS[5] );
        logger.debug( String.valueOf( this.masterTable ) );

        // Create fields map to match first 5 records of column 1
        SortedMap<DateTime, JtsField> expectedFieldsMap = new TreeMap<>();

        for( int i = 0; i <= 5; i++ ) {
            expectedFieldsMap.put( TS[i], BAR_FIELD );
        }

        // Check column 1 of the test table matches the expected fields map
        assertEquals( expectedFieldsMap, testTable.getColumn( COLUMN_1 ) );

        // Check columns 0 and 2 are still intact
        assertEquals( masterTable.getColumn( COLUMN_0 ), testTable.getColumn( COLUMN_0 ), "Column 0 should still match master table" );
        assertEquals( masterTable.getColumn( COLUMN_2 ), testTable.getColumn( COLUMN_2 ), "Column 2 should still match master table" );

        // Try to trim with a null DateTime parameter; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            testTable.clearColumnAfter( COLUMN_1, null );
        } );
    }


    /**
     * Tests {@link JtsTable#clearColumnBefore(Integer, DateTime)}.
     */
    @Test
    public void testClearColumnBefore() {
        // Make a copy of the master table
        JtsTable testTable = new JtsTable( this.masterTable );

        // Trimming a column that does not exist should have no effect
        testTable.clearColumnBefore( COLUMN_99, TS[5] );

        // Compare the table to the master table; they should still be equal
        assertEquals( this.masterTable, testTable, "Test table not equal to master table" );

        // Trim the test table before record 5
        testTable.clearColumnBefore( COLUMN_1, TS[5] );
        logger.debug( String.valueOf( this.masterTable ) );

        // Create fields map to match last 5 records of column 1
        SortedMap<DateTime, JtsField> expectedFieldsMap = new TreeMap<>();

        for( int i = 5; i < RECORD_COUNT; i++ ) {
            expectedFieldsMap.put( TS[i], BAR_FIELD );
        }

        // Check column 1 of the test table matches the expected fields map
        assertEquals( expectedFieldsMap, testTable.getColumn( COLUMN_1 ) );

        // Check columns 0 and 2 are still intact
        assertEquals( masterTable.getColumn( COLUMN_0 ), testTable.getColumn( COLUMN_0 ), "Column 0 should still match master table" );
        assertEquals( masterTable.getColumn( COLUMN_2 ), testTable.getColumn( COLUMN_2 ), "Column 2 should still match master table" );

        // Try to trim with a null DateTime parameter; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            testTable.clearColumnAfter( COLUMN_1, null );
        } );
    }


    /**
     * Tests {@link JtsTable#clearAfter(DateTime)}.
     */
    @Test
    public void testClearAfter() {
        // Trim the master table after record 5 (exclusive)
        this.masterTable.clearAfter( TS[5] );
        logger.debug( String.valueOf( this.masterTable ) );

        // Check table currently contains the first 6 records
        for( int i = 0; i <= 5; i++ ) {
            assertTrue( this.masterTable.hasRecord( TS[i] ), "Table should still contain record '" + i + "'" );
        }

        // Check table no longer contains the last 4 records
        for( int i = 6; i < RECORD_COUNT; i++ ) {
            assertFalse( this.masterTable.hasRecord( TS[i] ), "Table should not contain record '" + i + "'" );
        }

        // Try to trim with null DateTime parameters; this should fail with IllegalArgumentException
        assertThrows( IllegalArgumentException.class, () -> {
            this.masterTable.clearAfter( null );
        } );
    }


    /**
     * Tests {@link JtsTable#clearBefore(DateTime)}.
     */
    @Test
    public void testClearBeforeAndAfterBefore() {
        // Trim the master table before record 5 (exclusive)
        this.masterTable.clearBefore( TS[5] );
        logger.debug( String.valueOf( this.masterTable ) );

        // Check table no longer contains the first 5 records
        for( int i = 0; i <= 4; i++ ) {
            assertFalse( this.masterTable.hasRecord( TS[i] ), "Table should not contain record '" + i + "'" );
        }

        // Check table currently contains the last 5 records
        for( int i = 5; i < RECORD_COUNT; i++ ) {
            assertTrue( this.masterTable.hasRecord( TS[i] ), "Table should still contain record '" + i + "'" );
        }

    }

}
