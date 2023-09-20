package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Objects;
import io.eagle.util.Assert;
import io.eagle.util.jackson.EagleObjectMapper;
import io.eagle.util.jackson.JacksonUtil;
import io.eagle.util.NumberFormat;
import io.eagle.util.jts.JtsViews.*;
import io.eagle.util.jts.complex.ComplexValue;
import io.eagle.util.time.MomentTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;


/**
 * An immutable JTS document as part of the JSON Time Series document specification; a JTS document is the top-level structure of the JTS
 * specification.
 * <p>
 * JtsDocument can be serialized to JSON using the Jackson object mapper available via the JacksonJtsMapperFactory.
 * <p>
 * JtsDocument is <b>immutable</b>:
 * <ul>
 * <li>the class is final and cannot be overridden
 * <li>all fields are private and final
 * <li>callers are forced to construct a JtsDocument in a single step by using one of the constructors, rather than by setXXX methods
 * <li>there are no methods which can change the state of the object in any way; this means no setXXX methods
 * <li>the only mutable object contained in JtsDocument is the records map {@link #data}, and it therefore must be defensively copied when
 * passed between this class and its caller:
 * <ul>
 * <li>when the records map {@link #data} is passed in to any of the constructors, it is copied to a new {@link TreeMap} object
 * <li>when the records map {@link #data} is requested via {@link #getData()}, it returned as an unmodifiable view of the map
 * </ul>
 * </ul>
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JtsDocument {


    private static final Logger logger = LoggerFactory.getLogger( JtsDocument.class );

    /**
     * Default document type indicating this is a JST document
     */
    @JsonView(Document.class)
    private static final String DEFAULT_DOCTYPE = "jts";

    @JsonView(Document.class)
    private static final String DEFAULT_SUBTYPE = DocumentSubType.TIMESERIES;

    /**
     * Current JTS version
     */
    @JsonView(Document.class)
    private static final String CURRENT_VERSION = "1.0";
    @JsonView(Document.class)
    protected final String subType;
    /**
     * The document type
     */
    @JsonView(Document.class)
    private final String docType;
    /**
     * The version of JTS
     */
    @JsonView(Document.class)
    private final String version;

    /**
     * Header information about the document and the columns
     */
    @JsonView({ DocumentAndHeader.class, DocumentAndHeaderAndData.class })
    private final JtsDocumentHeader header;

    /**
     * The map containing records; the keys are a sorted 0-based index of record numbers
     */
    @JsonSubTypes({
            @JsonSubTypes.Type(name = DocumentSubType.TIMESERIES, value = JtsTable.class),
            @JsonSubTypes.Type(name = DocumentSubType.WINDROSE, value = JtsTable.class),
            @JsonSubTypes.Type(name = DocumentSubType.PROFILE, value = JtsProfile.class) })
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "subType", defaultImpl = JtsTable.class)
    @JsonView({ Data.class, DocumentAndData.class, DocumentAndHeaderAndData.class })
    private final JtsData data;

    @JsonCreator
    public JtsDocument( @JsonProperty("docType") String docType, @JsonProperty("subType") String subType,
                        @JsonProperty("version") String version, @JsonProperty("header") JtsDocumentHeader header, @JsonProperty("data") JtsData data ) {
        if( docType == null )
            this.docType = DEFAULT_DOCTYPE;
        else
            this.docType = docType;

        if( subType == null )
            this.subType = DEFAULT_SUBTYPE;
        else
            this.subType = subType;

        if( version == null )
            this.version = CURRENT_VERSION;
        else
            this.version = version;

        this.header = header;
        this.data = data;
    }


    /**
     * Default constructor; looks useless because the fields are final, but in fact this is used by Jackson for object/JSON mapping.
     */
    public JtsDocument() {
        this.docType = JtsDocument.DEFAULT_DOCTYPE;
        this.subType = JtsDocument.DEFAULT_SUBTYPE;
        this.version = JtsDocument.CURRENT_VERSION;
        this.header = null;
        this.data = null;
    }


    /**
     * Copy constructor.
     *
     * @param other the JtsDocument to copy from
     */
    public JtsDocument( JtsDocument other ) {
        this.docType = other.getDocType();
        this.subType = other.getSubType();
        this.version = other.getVersion();
        this.header = other.getHeader();
        this.data = other.getData();
    }


    public JtsDocument( JtsData data, JtsDocumentHeader header ) {
        this.docType = JtsDocument.DEFAULT_DOCTYPE;
        this.subType = JtsDocument.DEFAULT_SUBTYPE;
        this.version = JtsDocument.CURRENT_VERSION;
        this.header = header;
        this.data = data;
    }


    public JtsDocument( JtsData data, JtsDocumentHeader header, String subType ) {
        this.docType = JtsDocument.DEFAULT_DOCTYPE;
        this.subType = subType;
        this.version = JtsDocument.CURRENT_VERSION;
        this.header = header;
        this.data = data;
    }


    public JtsDocument( JtsData dataTable ) {
        this( dataTable, null );
    }


    /**
     * Constructor which reads JSON from an input stream.
     * <p>
     * Uses the static factory {@link #readJsonFromStream(InputStream)} to actually create the new document.
     *
     * @param inputStream the stream to read JSON from
     * @throws IOException             if there is an error reading the JSON stream
     * @throws JsonProcessingException if there is an error reading the JSON stream
     */
    public JtsDocument( InputStream inputStream ) throws JsonProcessingException, IOException {
        this( JtsDocument.readJsonFromStream( inputStream ) );
    }


    /**
     * Constructor which reads JSON from an input String.
     * <p>
     * Uses the static factory method {@link #readJsonFromString(String)} to actually create the new document.
     *
     * @param inputString the String to read JSON from
     * @throws IOException             if there is an error reading the JSON stream
     * @throws JsonProcessingException if there is an error reading the JSON stream
     */
    public JtsDocument( String inputString ) throws JsonProcessingException, IOException {
        this( JtsDocument.readJsonFromString( inputString ) );
    }


    /**
     * Static factory which creates and returns a new JTS document by reading JSON from the given input stream.
     *
     * @param inputStream the input stream to read JSON from
     * @return a new JtsDocument constructed from JSON
     * @throws JsonProcessingException if there is an error reading the JSON stream
     * @throws IOException             if there is an error reading the JSON stream
     */
    public static JtsDocument readJsonFromStream( InputStream inputStream ) throws JsonProcessingException, IOException {
        return getJsonReader().readValue( inputStream );
    }


    /**
     * Static factory which creates and returns a new JTS document by reading JSON from the given input String.
     *
     * @param inputString the input String to read JSON from
     * @return a new JtsDocument constructed from JSON
     * @throws JsonProcessingException if there is an error reading the JSON stream
     * @throws IOException             if there is an error reading the JSON String
     */
    public static JtsDocument readJsonFromString( String inputString ) throws JsonProcessingException, IOException {
        return getJsonReader().readValue( inputString );
    }


    /**
     * Creates and returns a Jackson {@link ObjectReader} capable of deserializing JSON into a JtsDocument.
     *
     * @return a Jackson {@link ObjectReader} capable of deserializing JSON into a JtsDocument.
     */
    public static ObjectReader getJsonReader() {
        return JacksonUtil.getObjectMapper().readerFor( JtsDocument.class );
    }

    public static String renderQuality( Integer quality, DocumentFormat documentFormat ) {
        Assert.isTrue( documentFormat.isQualityEnabled() );
        Assert.notNull( documentFormat.getQualityFormat() );

        switch( documentFormat.getQualityFormat() ) {
            case DELIMITED_WITH_VALUE:
                if( quality == null )
                    return documentFormat.getQualityDelimiter();
                else
                    return documentFormat.getQualityDelimiter() + quality;
            case SEPARATE_VALUE:
                if( quality == null )
                    return documentFormat.getDelimiter();
                else
                    return documentFormat.getDelimiter() + quality;
            default:
                return "";
        }
    }

    public static Object renderAnnotation( String annotation, DocumentFormat documentFormat ) {
        Assert.isTrue( documentFormat.isAnnotationsEnabled() );
        Assert.notNull( documentFormat.getAnnotationFormat() );

        switch( documentFormat.getAnnotationFormat() ) {
            case DELIMITED_WITH_VALUE:
                if( annotation == null )
                    return documentFormat.getAnnotationDelimiter();
                else
                    return documentFormat.getAnnotationDelimiter() + annotation;
            case SEPARATE_VALUE:
                if( annotation == null )
                    return documentFormat.getDelimiter();
                else
                    return documentFormat.getDelimiter() + annotation;
            default:
                return "";
        }
    }

    public static String renderValue( Object value, String format, DocumentFormat documentFormat, DateTimeZone timezone ) {
        Assert.notNull( documentFormat );

        if( value instanceof Double )
            return NumberFormat.getValueFormatted( (Double) value, format );
        else if( value instanceof DateTime )
            return String.valueOf( renderTime( (DateTime) value, timezone, format ) );
        else if( value instanceof String )
            return documentFormat.getTextQualifier() + value + documentFormat.getTextQualifier();
        else if( value instanceof ComplexValue)
            return ( (ComplexValue) value ).toDelimitedText( documentFormat.getDelimiter(), documentFormat.getTextQualifier() );
        else
            return String.valueOf( value );
    }

    /**
     * Formats and returns the given DateTime as a String, using the given zone and formatter (if formatter is null, returns ISO format)
     *
     * @param dateTime       DateTime to format, must not be null
     * @param timezone       DateTimeZone to use, must not be null
     * @return the given DateTime as a String, using the given zone and formatter, or milliseconds if the formatter is null
     */
    public static Object renderTime( final DateTime dateTime, DateTimeZone timezone, final String format ) {
        Assert.notNull( dateTime );
        Assert.notNull( timezone );

        if( format == null || format.isEmpty() ) {
            // When no format was requested, return ISO format
            return String.valueOf( dateTime.withZone( timezone ) );
        } else if( format.equals( "X" ) ) {
            // Unix timestamp (seconds since epoch)
            return String.valueOf( dateTime.getMillis() / 1_000 );
        } else if( format.equals( "x" ) ) {
            // Unix timestamp (millseconds since epoch)
            return String.valueOf( dateTime.getMillis() );
        } else {
            // Use the requested format to return a formatted date string
            return MomentTime.format( dateTime, timezone, format );
        }
    }

    /**
     * @return the defaultDoctype
     */
    public static String getDefaultDoctype() {
        return DEFAULT_DOCTYPE;
    }

    /**
     * @return the currentVersion
     */
    public static String getCurrentVersion() {
        return CURRENT_VERSION;
    }

    /**
     * Returns a string representation of this JtsDocument.
     *
     * @return a string representation of this JtsDocument.
     */
    @Override
    public String toString() {
        String json = null;

        try {
            json = toJsonPretty();
        } catch( JsonProcessingException e ) {
            logger.error( e.getMessage() );
        }

        return json;
    }

    public boolean hasName() {
        return this.header != null && this.header.getName() != null;
    }

    public JtsDocument withHeader( JtsDocumentHeader header ) {
        return new JtsDocument( this.data, header );
    }

    public JtsDocument withoutHeader() {
        return new JtsDocument( this.data, null );
    }

    /**
     * Returns a String representing this JtsDocument as fixed-width data, with the width being 30 characters, using the default UTC
     * timezone when formatting a DateTime.
     * <p>
     * Calls {@link #toFixedWidth(DocumentFormat, DateTimeZone)}} with UTC as the timezone.
     *
     * @return a String representing this JtsDocument as fixed-width data, with the width being 30 characters, using the default UTC
     * timezone when formatting a DateTime
     * @see #toFixedWidth(DocumentFormat, DateTimeZone)
     */
    public String toFixedWidth() {
        return this.toFixedWidth( DocumentFormat.FIXED_WIDTH, DateTimeZone.UTC );
    }

    /**
     * Returns a String representing this JtsDocument as fixed-width data, with the width being 30 characters, using the given timezone
     * when formatting a DateTime.
     * <p>
     * If the header field is not null, a header row will be printed as the first row of the return String; it will contain "Timestamp"
     * followed by the fixed-width column names.
     *
     * @param timezone the timezone to use when formatting the timestamp and any date fields
     * @return a String representing this JtsDocument as fixed-width data, with the width being 30 characters, using the given timezone
     * when formatting a DateTime
     */
    public String toFixedWidth( DocumentFormat documentFormat, DateTimeZone timezone ) {
        StringBuffer sb = new StringBuffer();

        if( this.header != null )
            sb.append( this.header.toFixedWidth( documentFormat ) );

        sb.append( this.data.toFixedWidth( header, documentFormat, timezone ) );

        return sb.toString();
    }

    /**
     * Returns a String representing this JtsDocument as csv data, using the default UTC timezone when formatting a DateTime.
     * <p>
     * Calls {@link #toDelimitedText(DocumentFormat, DateTimeZone)} with UTC as the timezone.
     *
     * @return a String representing this JtsDocument as csv data, using the default UTC timezone when formatting a DateTime.
     * @see #toDelimitedText(DocumentFormat, DateTimeZone)
     */
    public String toCsv() {
        return this.toDelimitedText( DocumentFormat.CSV, DateTimeZone.UTC );
    }

    /**
     * Returns a String representing this JtsDocument as delimited text.
     * <p>
     * <h2>Header row</h2>
     * If the header of this document is not null, a header row will be printed as the first row of the return String; it will contain
     * {@code Timestamp} followed by the delimiter-separated column names. Both the {@code Timestamp} string and column name strings will be
     * enclosed by the given text qualifier. For example, if the text qualifier is double quotes, and the delimiter is comma, the header
     * line would look like this:
     * <p>
     * {@code "Timestamp", "Column 1", "Column 2"}
     * <p>
     * <h2>Quality codes</h2> If the quality code for a field is non-null, that quality is printed in the delimited text according to the
     * given QualityFormat.
     *
     * @return a String representing this JtsDocument as csv data, using the given timezone when formatting a DateTime
     */
    public String toDelimitedText( final DocumentFormat documentFormat, final DateTimeZone timezone ) {
        Assert.notNull( timezone );
        Assert.notNull( documentFormat.getDelimiter() );
        Assert.notNull( documentFormat.getTextQualifier() );
        Assert.notNull( documentFormat.isQualityEnabled() );
        Assert.notNull( documentFormat.isHeaderEnabled() );

        StringBuffer sb = new StringBuffer();

        if( documentFormat.isHeaderEnabled() && this.header != null && this.header.getColumns().size() > 0 )
            sb.append( this.header.toDelimitedText( documentFormat ) );

        sb.append( this.data.toDelimitedText( header, documentFormat, timezone ) );

        return sb.toString();
    }

    public Duration getDuration() {
        return isEmpty() ? Duration.ZERO : new Duration( this.getFirstTimestamp(), this.getLastTimestamp() );
    }

    public String toJson() throws JsonProcessingException {
        return this.toJson( DocumentFormat.JSON_STANDARD, DateTimeZone.UTC );
    }

    /**
     * Returns this JtsDocument as a JSON string.
     * <p>
     * If the {@code formatted} parameter is true, any JtsFields containing a Double value will be formatted according to the {@code format}
     * field of the appropriate column header.
     * <p>
     * If the {@code pretty} parameter is true, the JSON output will be formatted for readability (with line breaks and indents).
     * <p>
     * If the {@code pretty} parameter is true, and a non-null DateTimeZone is provided, any Joda DateTime objects serialized in the JSON
     * output will be be formatted with the given DateTimeZone.
     * <p>
     *
     * @param dateTimeZone the timezone to use when serializing a Joda {@link DateTime} Object; if null, {@link DateTime} Objects will be
     *                     formatted using their current timezone
     * @return this JtsDocument as a JSON string, with formatted Double values
     * @throws JsonProcessingException if there is an error writing the JSON string
     */
    public String toJson( DocumentFormat documentFormat, DateTimeZone dateTimeZone ) throws JsonProcessingException {
        if( documentFormat.isHeaderEnabled() && this.header != null )
            return toJson( documentFormat, dateTimeZone, DocumentAndHeaderAndData.class );
        else
            return toJson( documentFormat, dateTimeZone, DocumentAndData.class );
    }

    public String toJson( DocumentFormat documentFormat, DateTimeZone dateTimeZone, Class<? extends JtsView> view ) throws JsonProcessingException {
        Map<Integer, String> numberFormat = null;

        if( this.header != null )
            numberFormat = this.header.getNumberFormat();

        if( this.subType != null )
            documentFormat.setDocumentSubType( this.subType );

        ObjectMapper mapper = new EagleObjectMapper().withSerializer( new JtsTableSerializer( numberFormat, documentFormat, dateTimeZone ) );
        ObjectWriter writer = mapper.writerWithView( view );

        if( documentFormat.isPretty() )
            writer = writer.withDefaultPrettyPrinter();

        return writer.writeValueAsString( this );
    }

    /**
     * Returns this JtsDocument as a JSON string, formatted to be more easily human-readable, using the default UTC timezone when
     * serializing a Joda {@link DateTime}.
     * <p>
     * Calls {@link #toJsonPretty()} with UTC as the timezone.
     *
     * @return this JtsDocument as a JSON string, formatted to be more easily human-readable, using the default UTC timezone when
     * serializing
     * a Joda {@link DateTime}
     * @throws JsonProcessingException if there is an error writing the JSON string
     * @see #toJsonPretty()
     */
    public String toJsonPretty() throws JsonProcessingException {
        return this.toJson( DocumentFormat.JSON, DateTimeZone.UTC );
    }

    /**
     * Converts the given {@link JtsDocument} to a String, based on the required {@link DocumentFormatType} (for example CSV, JSON).
     *
     * @throws JsonProcessingException if there is an error serializing the JtsDocument to JSON
     */
    public String renderRecords( DocumentFormat format, DateTimeZone timezone, boolean documentStart ) throws JsonProcessingException {
        switch( format.getFormatType() ) {
            case CSV:
                return this.toDelimitedText( format.withHeaderEnabled( false ), timezone );
            case FIXED_WIDTH:
                return this.toFixedWidth( format.withHeaderEnabled( false ), timezone );
            case JSON_CHART:
            case JSON_STANDARD:
                format = DocumentFormat.JSON_STANDARD;
            case JSON:
                String prefix = documentStart ? "\n     " : " ,";
                String json = this.toJson( format, timezone, Data.class );
                json = json.replaceFirst( ".*\\s*.*data.*\\[\\s*", prefix );
                return json.substring( 0, json.length() - 3 );
            default:
                throw new IllegalArgumentException( "Unknown document format: " + format.getFormatType() );
        }
    }

    public String renderHeader( DocumentFormat format, DateTimeZone timezone ) throws JsonProcessingException {
        switch( format.getFormatType() ) {
            case CSV:
                if( format.isHeaderEnabled() )
                    return this.header.toDelimitedText( format );
                else
                    return "";
            case FIXED_WIDTH:
                if( format.isHeaderEnabled() )
                    return this.header.toFixedWidth( format );
                else
                    return "";
            case JSON:
            case JSON_CHART:
            case JSON_STANDARD:
                String json = new JtsDocument( new JtsTable<>(), this.header ).toJson( format, timezone, DocumentAndHeaderAndData.class ) + "\n";
                return json.substring( 0, json.length() - 5 );
            default:
                throw new IllegalArgumentException( "Unknown document format: " + format.getFormatType() );
        }
    }

    public String renderFooter( DocumentFormat format ) {
        switch( format.getFormatType() ) {
            case CSV:
            case FIXED_WIDTH:
                return "";
            case JSON:
            case JSON_CHART:
            case JSON_STANDARD:
                return "]\n}\n";
            default:
                throw new IllegalArgumentException( "Unknown document format: " + format.getFormatType() );
        }
    }

    /**
     * Returns the timestamp of the first {@link JtsRecord} (the one with the lowest key), or null if the map is empty.
     *
     * @return the timestamp of the first {@link JtsRecord} (the one with the lowest key), or null if the map is empty
     */
    public DateTime getFirstTimestamp() {
        return isEmpty() ? null : this.data.getFirstTimestamp();
    }

    /**
     * Returns the timestamp of the last {@link JtsRecord} (the one with the highest key), or null if the map is empty.
     *
     * @return the timestamp of the last {@link JtsRecord} (the one with the highest key), or null if the map is empty
     */
    public DateTime getLastTimestamp() {
        return isEmpty() ? null : this.data.getLastTimestamp();
    }

    public boolean isEmpty() {
        return this.data == null || this.data.isEmpty();
    }

    /**
     * @return the number of records in this JtsDocument
     */
    public int count() {
        return isEmpty() ? 0 : this.data.recordCount();
    }

    /**
     * @return the doctype
     */
    public String getDocType() {
        return docType;
    }

    public String getSubType() {
        return subType;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    public String getName() {
        return this.header == null ? null : this.header.getName();
    }

    /**
     * Returns an unmodifiable view of the records map; this avoid creating a defensive copy, but callers must copy the map if they need to
     * modify it.
     *
     * @return an unmodifiable view of the records map
     */
    public JtsData getData() {
        return this.data;
    }

    public JtsTable<?> getTable() {
        return getData( JtsTable.class );
    }

    @SuppressWarnings("unchecked")
    public <T> JtsTable<T> getTable( Map<Integer, T> index ) {
        return getData( JtsTable.class ).withIndex( index );
    }

    public <T> JtsTable<T> getTable( Class<T> indexClass ) {
        return getData( JtsTable.class );
    }

    @SuppressWarnings("unchecked")
    public <T extends JtsData> T getData( Class<T> clazz ) {
        return this.data == null ? null : (T) this.data;
    }

    /**
     * @return the header
     */
    public JtsDocumentHeader getHeader() {
        return header;
    }


    /**
     * Indicates if this JtsDocument is equal to the given one.
     * <p>
     * Copied from <a href="http://www.javapractices.com/topic/TopicAction.do?Id=17">
     * http://www.javapractices.com/topic/TopicAction.do?Id=17 </a>
     *
     * @param other to compare to this JtsDocument
     * @return true if this JtsDocument is equal to the given one; otherwise returns false
     */
    @Override
    public boolean equals( Object other ) {
        Assert.isInstanceOf( JtsDocument.class, other );
        JtsDocument otherDoc = (JtsDocument) other;

        return Objects.equal( this.data, otherDoc.data )
                && Objects.equal( this.docType, otherDoc.docType )
                && Objects.equal( this.subType, otherDoc.subType )
                && Objects.equal( this.header, otherDoc.header )
                && Objects.equal( this.version, otherDoc.version );
    }


    /**
     * Returns a hash code value for this JtsDocument; hash is based on the JSON form of the object, as output by {@link #toJson()}.
     * <p>
     * Copied from <a href="http://www.javapractices.com/topic/TopicAction.do?Id=28">
     * http://www.javapractices.com/topic/TopicAction.do?Id=28 </a>
     *
     * @return a hash code value for this JtsDocument
     */
    @Override
    public int hashCode() {
        return Objects.hashCode( this.data, this.docType, this.header, this.version, this.subType );
    }

}
