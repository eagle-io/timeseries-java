/**
 *
 */
package io.eagle.util.jts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import io.eagle.util.DataType;
import io.eagle.util.FieldFormat;
import io.eagle.util.geo.Coordinates;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 *
 */
public class JtsTest {

    /** slf4j static logger */
    private static final Logger logger = LoggerFactory.getLogger( JtsTest.class );

    private static final String COUNT = "10";
    private static final String TOTAL = "500";
    private static final String AVERAGE = "50";
    private static final String MINIMUM = "25";
    private static final String MAXIMUM = "75";
    private static final TreeSet<String> TIMESTAMPS_OF_MINIMUM = new TreeSet<>( Arrays.asList( "2009-08-07T06:05:04.321Z", "2010-09-08T07:06:05.432Z" ) );
    private static final TreeSet<String> TIMESTAMPS_OF_MAXIMUM = new TreeSet<>( Arrays.asList( "2011-10-09T08:07:06.543Z", "2012-11-10T09:08:07.654Z" ) );

    private static final DateTime RECORD_1_TIMESTAMP = DateTime.parse( "2009-08-07T06:05:04.321Z" );
    private static final DateTime RECORD_2_TIMESTAMP = DateTime.parse( "2010-09-08T07:06:05.432Z" );
    private static final DateTime RECORD_3_TIMESTAMP = DateTime.parse( "2011-10-09T08:07:06.543Z" );
    private static final DateTime RECORD_4_TIMESTAMP = DateTime.parse( "2012-11-10T09:08:07.654Z" );

    private static final String INTERVAL = "60";

    /** Start date for sample data */
    private static final DateTime jan1st = new DateTime( 2013, 1, 1, 0, 0, DateTimeZone.UTC );


    /**
     * Generates JTS documents to be used for testing. Three samples of random data will be generated; each sample covers 3 days of 5-minute
     * data, and will produce both a .csv file and a .jts file.
     *
     * @throws Exception if there is an error generating or writing the sample files
     */
    @Disabled
    @Test
    public final void generateRandomTestData() throws Exception {
        int numSamples = 3;

        String dataPath = "C:\\Users\\jessmitc\\Documents\\Google Drive\\eagle.io\\data\\random_jts_samples\\";

        // Generate the sample data
        for( int i = 1; i <= numSamples; i++ ) {
            // Create a JtsDocument with 3 columns of 5 minute data, with 864 records (3 days) starting on Jan 1st
            JtsDocument jtsDocument = this.generateRandomTestData( 3, 5, 864, jan1st );

            // Print the JTS version of the sample data to a file
            PrintWriter jsonPrinter = new PrintWriter( new File( dataPath + "sample_" + i + ".jts" ) );
            jsonPrinter.write( jtsDocument.toJsonPretty() );
            jsonPrinter.close();

            // Print the CSV version of the sample data to a file
            PrintWriter csvPrinter = new PrintWriter( new File( dataPath + "sample_" + i + ".csv" ) );
            csvPrinter.write( jtsDocument.toCsv() );
            csvPrinter.close();
        }
    }


    @Test
    public void test() {
        Integer i = 0;

        Integer system = 155;
        Integer user = 192;

    }


    @Test
    public void testJtsSample() {
        DateTime ts1 = DateTime.now().plus( 1 );
        DateTime ts2 = DateTime.now().plus( 2 );
        DateTime ts3 = DateTime.now().plus( 3 );
        DateTime ts4 = DateTime.now().plus( 4 );
        DateTime ts5 = DateTime.now().plus( 5 );

        JtsField f1 = new JtsField( 5.0 );
        JtsField f2 = new JtsField( 10.0 );
        JtsField f3 = new JtsField( 15.0 );

        JtsSample s1 = new JtsSample( ts1, f1 );
        JtsSample s2 = new JtsSample( ts2, f2 );
        JtsSample s3 = new JtsSample( ts3, f3 );

        JtsRecord r1 = new JtsRecord( ts1, f1 );
        JtsRecord r2 = new JtsRecord( ts2, f2 );
        JtsRecord r3 = new JtsRecord( ts3, f3 );

        JtsTable table = new JtsTable();

        table.put( ts2, 2, f2 );
        table.put( ts3, 5, f3 );
        table.put( ts5, 3, f3 );
        table.put( ts1, 5, f3 );

        logger.debug( table.toString() );
        logger.debug( table.getColumnIndexes().toString() );

        // logger.debug( records.toString() );

    }


    @Test
    public void testJtsCreator() throws Exception {
        String json = generateRandomTestData( 3, 5, 864, jan1st ).toJsonPretty();
        JtsDocument jts = null;

        try {
            logger.info("JSON: {}", json);
            jts = JtsDocument.readJsonFromString( json );
        } catch( Exception e ) {
            logger.debug( e.toString() );
        }

        logger.info( "JTS: {}", jts );
    }


    @Test
    public void testSerializer() {
        String json = null;

        try {
            JtsDocument jts = generateRandomTestData( 3, 5, 864, jan1st );
            json = jts.toJson();
        } catch( Exception e ) {
            logger.debug( e.toString() );
        }

        logger.debug( json );
    }


    /**
     * Generates random data in a JtsDocument.
     *
     * @param numColumns number of columns in the JtsDocument
     * @param intervalMinutes interval between records (for example 1 minute, 5 minutes)
     * @param numRecords total number of records in the JtsDocument
     * @param startDate timestamp of the first record
     * @return a JtsDocument containing random Double values (each formatted to 2 decimal places)
     *
     * @throws Exception if there is an error generating the JtsDocument
     */
    private final JtsDocument generateRandomTestData( int numColumns, int intervalMinutes, int numRecords, DateTime startDate ) throws Exception {
        DateTime timestamp = startDate;
        JtsTable<?> table = new JtsTable<>();

        // Process records
        for( int i = 0; i < numRecords; i++ ) {
            Map<Integer, JtsField> fields = new HashMap<>();

            // Process fields
            for( int j = 0; j < numColumns; j++ ) {
                // Random number between -50.0 and 50.0
                fields.put( j, new JtsField( new Random().nextDouble() * 100 - 50 ) );
            }

            table.putRecordByColumn( new JtsRecord<>( timestamp, fields ) );
            timestamp = timestamp.plusMinutes( intervalMinutes );
        }

        SortedMap<Integer, JtsColumnHeader> columns = Maps.newTreeMap();

        // Create column headers so the Double values will be formatted
        for( int k = 0; k < numColumns; k++ ) {
            columns.put( k, new JtsColumnHeader( null, "Column_" + k, null, null, null, null, "#.##", null, null, null ) );
        }

        // Create a document header
        JtsDocumentHeader jtsDocumentHeader = new JtsDocumentHeader.Builder()
                .recordCount( numRecords )
                .columns( columns )
                .build();

        // Create the JtsDocument from the record set and header
        JtsDocument jtsDocument = new JtsDocument( table, jtsDocumentHeader );

        // Format the JtsDocument by serializing it with the formatting flag set to true, then deserializing it to a new JtsDocument
        // This trick is required to force the formatting of the Double values based on the format string in the column headers
        // JtsDocument formattedJtsDocument = new JtsDocument( jtsDocument.toJson( true, true, DateTimeZone.UTC ) );

        return jtsDocument;
    }


    @Test
    @Disabled("legacy test")
    public final void testJtsDocumentToFormattedJson() throws JsonProcessingException {

        JtsTable<?> jtsRecordSet = new JtsTable<>();

        for( int i = 0; i < 10; i++ ) {
            jtsRecordSet.putRecordByColumn( this.getSampleRecord() );
        }

        JtsDocument jtsDocument = new JtsDocument( jtsRecordSet, this.getSampleHeader() );
        DocumentFormat pretty = new DocumentFormat( DocumentFormatType.JSON );
        DocumentFormat standard = new DocumentFormat( DocumentFormatType.JSON );

        logger.info( jtsDocument.toJson() );
        logger.info( jtsDocument.toJson( standard, null ) );
        logger.info( jtsDocument.toJson( pretty, null ) );
        logger.info( jtsDocument.toJson( pretty, DateTimeZone.forOffsetHours( 10 ) ) );
        logger.info( jtsDocument.toJson( standard, DateTimeZone.forOffsetHours( 10 ) ) );
        logger.info( jtsDocument.toJson( standard, DateTimeZone.forOffsetHours( 10 ) ) );
        logger.info( jtsDocument.toJson( pretty, DateTimeZone.forOffsetHours( 10 ) ) );
        logger.info( jtsDocument.toJson( pretty, null ) );

        logger.info( jtsDocument.toJson( pretty.withHeaderEnabled( false ), null ) );
    }


    @Test
    @Disabled("legacy test")
    public final void testJtsDocumentToDelimitedText() {

        JtsTable<?> jtsRecordSet = new JtsTable<>();

        for( int i = 0; i < 10; i++ ) {
            jtsRecordSet.putRecordByColumn( this.getSampleRecord() );
        }

        JtsDocument jtsDocument = new JtsDocument( jtsRecordSet, this.getSampleHeader() );
        Map<Integer, String> numberFormat = jtsDocument.getHeader().getNumberFormat();

        logger.info( "CSV document:\n{}", jtsDocument.toCsv() );

        DocumentFormat documentFormat = new DocumentFormat.Builder()
                .timeFormat( "YYYY,DD,DDDo,MM,Z" )
                .delimiter( "__" )
                .textQualifier( "'" )
                .headerEnabled( false )
                .qualityFormat( FieldFormat.DELIMITED_WITH_VALUE )
                .qualityDelimiter( "==" )
                .qualityEnabled( true )
                .build();

        logger.info( "Custom delimited document:\n{}", jtsDocument.toDelimitedText( documentFormat, DateTimeZone.forID( "Australia/Sydney" ) ) );

        // TODO: check that a sparse record set contains correct commas
    }


    @Test
    @Disabled("legacy test")
    public final void testJtsDocumentToFixedWidth() {
        JtsTable<?> jtsRecordSet = new JtsTable<>();

        for( int i = 0; i < 10; i++ ) {
            jtsRecordSet.putRecordByColumn( this.getSampleRecord() );
        }

        JtsDocument jtsDocument = new JtsDocument( jtsRecordSet, this.getSampleHeader() );

        logger.info( "Fixed width document:\n{}", jtsDocument.toFixedWidth() );

    }


    @Test
    public final void testJtsDocumentHeader() throws JsonProcessingException {
        JtsDocumentHeader jtsDocumentHeader = this.getSampleHeader();

        logger.info( jtsDocumentHeader.toJsonPretty( DateTimeZone.UTC ) );
        logger.info( jtsDocumentHeader.toString() );

    }


    private JtsDocumentHeader getSampleHeader() {
        TreeMap<String, String> metadata = new TreeMap<>();

        metadata.put( "foo", "bar" );
        metadata.put( "earl", "grey" );

        SortedMap<Integer, JtsColumnHeader> columns = Maps.newTreeMap();

        columns.put( 0, new JtsColumnHeader( new ObjectId().toString(), "Column 0", DataType.NUMBER, null, null, null, "#.##", null, null, null ) );
        columns.put( 1, new JtsColumnHeader( new ObjectId().toString(), null, DataType.TEXT, null, null, null, null, null, null, null ) );
        columns.put( 2, new JtsColumnHeader( new ObjectId().toString(), "Column 2", DataType.TIME, null, null, null, null, null, null, null ) );

        JtsDocumentHeader header = new JtsDocumentHeader.Builder()
                .startTime( RECORD_1_TIMESTAMP )
                .endTime( RECORD_4_TIMESTAMP )
                .recordCount( 4 )
                .columns( columns )
                .build();

        return header;
    }


    private JtsRecord<Object> getSampleRecord() {
        Double doubleValue = new Double( 3.14159265359 );
        String stringValue = "he{ll}o,: \"wor\\d\"";
        DateTime dateValue = RECORD_1_TIMESTAMP;

        JtsField doubleField = new JtsField( doubleValue );
        doubleField.setUserQuality( 1 );

        JtsField stringField = new JtsField( stringValue );

        JtsField dateField = new JtsField( RECORD_1_TIMESTAMP );
        dateField.setUserQuality( 2 );

        TreeMap<Integer, JtsField> fields = new TreeMap<>();

        fields.put( 0, doubleField );
        fields.put( 1, stringField );
        fields.put( 2, dateField );

        DateTime randomDate = DateTime.now()
                .minusDays( new Random().nextInt( 1000 ) )
                .minusHours( new Random().nextInt( 1000 ) )
                .minusMinutes( new Random().nextInt( 1000 ) )
                .minusSeconds( new Random().nextInt( 1000 ) );

        return new JtsRecord<>( randomDate, fields );
    }


    @Test
    @Disabled("legacy test")
    public final void testJtsFieldStringMethods() throws JsonProcessingException {
        Double doubleValue = new Double( 3.14159265359 );
        String stringValue = "he{ll}o: \"wor\\d\"";
        DateTime dateValue = RECORD_1_TIMESTAMP;// .toDate();

        JtsField doubleField = new JtsField( doubleValue );
        doubleField.setUserQuality( - 1 );

        JtsField stringField = new JtsField( stringValue );
        stringField.setUserQuality( 1 );

        JtsField dateField = new JtsField( RECORD_1_TIMESTAMP );
        dateField.setUserQuality( 2 );

        logger.info( "doubleField.toString():  '{}'", doubleField.toString() );
        logger.info( "stringField.toString():  '{}'", stringField.toString() );
        logger.info( "dateField.toString():    '{}'", dateField.toString() );
        logger.info( "\n" );
        logger.info( "doubleField.getValue():  '{}'", doubleField.getValue() );
        logger.info( "stringField.getValue():  '{}'", stringField.getValue() );
        logger.info( "dateField.getValue():    '{}'", dateField.getValue() );
        logger.info( "\n" );

        Double newDouble = (Double) doubleField.getValue();
        String newString = (String) stringField.getValue();
        DateTime newDate = (DateTime) dateField.getValue();

        logger.info( "(Double)  doubleField.getValue(): '{}'", newDouble );
        logger.info( "(String)  stringField.getValue(): '{}'", newString );
        logger.info( "(DateTime) dateField.getValue(): '{}'", newDate );

        assertEquals( doubleValue, newDouble, "Double value from getValue() should be the same as the original" );
        assertEquals( stringValue, newString, "String value from getValue() should be the same as the original" );
        assertEquals( dateValue, newDate, "Date value from getValue() should be the same as the original" );
    }


    @Test
    @Disabled("legacy test")
    public final void testSerialization() throws IOException {
        Double doubleValue = new Double( 3.14159265359 );
        String stringValue = "This is a long string, longer than 19 characters";
        DateTime dateValue = RECORD_1_TIMESTAMP;
        Coordinates coordsValue = new Coordinates( 3.22, 2.33 );

        logger.info( "doubleValue: '{}'", doubleValue );
        logger.info( "stringValue: '{}'", stringValue );
        logger.info( "dateValue: '{}'", dateValue );
        logger.info( "coordsValue: '{}'", coordsValue );

        JtsField doubleField = new JtsField( doubleValue );
        doubleField.setUserQuality( 1 );

        JtsField stringField = new JtsField( stringValue );

        JtsField dateField = new JtsField( RECORD_1_TIMESTAMP );
        dateField.setUserQuality( 2 );

        JtsField coordsField = new JtsField( coordsValue );
        coordsField.setUserQuality( 34 );

        logger.info( "doubleField.toString: '{}'", doubleField.toString() );
        logger.info( "stringField.toString: '{}'", stringField.toString() );
        logger.info( "dateField.toString: '{}'", dateField.toString() );
        logger.info( "coordsField.toString: '{}'", coordsField.toString() );

        TreeMap<Integer, JtsField> fields = new TreeMap<>();

        fields.put( 0, doubleField );
        fields.put( 1, stringField );
        fields.put( 2, dateField );
        fields.put( 3, coordsField );

        JtsRecord<?> record1 = new JtsRecord<>( new DateTime( DateTimeZone.UTC ), fields );
        JtsRecord<?> record2 = new JtsRecord<>( record1 );
        JtsRecord<?> record3 = new JtsRecord<>( record1 );
        JtsRecord<?> record4 = new JtsRecord<>( record1 );

        JtsTable<Object> table = new JtsTable<>();
        table.putRecordByColumn( record1 );
        table.putRecordByColumn( record2 );
        table.putRecordByColumn( record3 );
        table.putRecordByColumn( record4 );

        JtsDocument jts = new JtsDocument( table, this.getSampleHeader() );

        logger.info( "jts.toJson:\n" + jts.toJson() );
        logger.info( "jts.toPrettyJson:\n" + jts.toJsonPretty() );

        logger.info( "newJts.toJson:\n" + jts.toJson() );
        logger.info( "newJts.toPrettyJson:\n" + jts.toJson( DocumentFormat.JSON, DateTimeZone.forOffsetHours( 12 ) ) );

        JtsDocument fromPrettyJson = new JtsDocument( jts.toJson( DocumentFormat.JSON, DateTimeZone.forOffsetHours( 12 ) ) );
        logger.info( "fromPrettyJson.toJson:\n" + fromPrettyJson.toJson() );
        logger.info( "fromPrettyJson.toPrettyJson:\n" + fromPrettyJson.toJsonPretty() );
        logger.info( "fromPrettyJson.toString:\n" + fromPrettyJson.toString() );
        logger.info( "fromPrettyJson.toCsv:\n" + fromPrettyJson.toCsv() );
        logger.info( "fromPrettyJson.toFixedWidth:\n" + fromPrettyJson.toFixedWidth() );

        // Check that the two JTS documents are the same
        assertEquals( jts, jts, "JTS document should be the same after reserializing" );

        Object newDoubleObject = jts.getData( JtsTable.class ).getFirstRecord().getField( 0 ).getValue();
        Object newStringObject = jts.getData( JtsTable.class ).getFirstRecord().getField( 1 ).getValue();
        Object newDateObject = jts.getData( JtsTable.class ).getFirstRecord().getField( 2 ).getValue();
        Object newCoordsObject = jts.getData( JtsTable.class ).getFirstRecord().getField( 3 ).getValue();

        logger.info( "Double class after reserializing new JtsDocument:  '{}'", newDoubleObject.getClass().getSimpleName() );
        logger.info( "String class after reserializing new JtsDocument:  '{}'", newStringObject.getClass().getSimpleName() );
        logger.info( "Date class after reserializing new JtsDocument:    '{}'", newDateObject.getClass().getSimpleName() );
        logger.info( "Coordinates class after reserializing new JtsDocument:    '{}'", newCoordsObject.getClass().getSimpleName() );
        logger.info( "\n" );

        Double newDouble = (Double) newDoubleObject;
        String newString = (String) newStringObject;
        DateTime newDate = (DateTime) newDateObject;
        Coordinates newCoords = (Coordinates) newCoordsObject;

        logger.info( "Double value after reserializing new JtsDocument:  '{}'", newDouble );
        logger.info( "String value after reserializing new JtsDocument:  '{}'", newString );
        logger.info( "DateTime value after reserializing new JtsDocument:'{}'", newDate );
        logger.info( "Coordinates value after reserializing new JtsDocument:'{}'", newCoords );
        logger.info( "\n" );

        assertEquals(  doubleValue, newDouble , "Double value from reserialized JtsDocument should be the same as the original");
        assertEquals(  stringValue, newString , "String value from reserialized JtsDocument should be the same as the original");
        assertEquals(  dateValue, newDate , "DateTime value from reserialized JtsDocument should be the same as the original");
        assertEquals(  coordsValue, newCoords , "Coordinates value from reserialized JtsDocument should be the same as the original");

    }

}
