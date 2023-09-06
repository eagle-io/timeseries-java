package io.eagle.util.jts;

import io.eagle.util.jackson.JacksonUtil;
import io.eagle.util.FieldFormat;

import java.util.Set;


public class DocumentFormat {

    /**
     * Default Moment time format used for writing delimited text (when a format was not supplied)
     */
    public static final String DEFAULT_TIME_FORMAT = "YYYY-MM-DDTHH:mm:ss.SSSZZ";

    public static DocumentFormat CSV = new DocumentFormat.Builder()
            .formatType(DocumentFormatType.CSV)
            .delimiter(",")
            .headerEnabled(true)
            .textQualifier("\"")
            .qualityFormat(FieldFormat.DELIMITED_WITH_VALUE).qualityDelimiter(":")
            .qualityEnabled(false)
            .annotationFormat(FieldFormat.DELIMITED_WITH_VALUE).annotationDelimiter(";")
            .annotationsEnabled(true)
            .timeFormat(DEFAULT_TIME_FORMAT)
            .build();

    public static DocumentFormat FIXED_WIDTH = new DocumentFormat.Builder()
            .formatType(DocumentFormatType.FIXED_WIDTH)
            .headerEnabled(true).qualityEnabled(false)
            .timeFormat(DEFAULT_TIME_FORMAT)
            .build();

    public static DocumentFormat JSON = new DocumentFormat.Builder()
            .formatType(DocumentFormatType.JSON)
            .headerEnabled(true)
            .textQualifier("")
            .qualityEnabled(false)
            .annotationsEnabled(true)
            .timeFormat(DEFAULT_TIME_FORMAT)
            .build();

    public static DocumentFormat JSON_STANDARD = new DocumentFormat.Builder()
            .formatType(DocumentFormatType.JSON_STANDARD)
            .headerEnabled(true)
            .textQualifier("")
            .qualityEnabled(false)
            .annotationsEnabled(true)
            .timeFormat(DEFAULT_TIME_FORMAT)
            .build();

    /**
     * {@link DocumentFormatType} ENUM describing the desired format of the data
     */
    private DocumentFormatType formatType = DocumentFormatType.JSON_STANDARD;

    /**
     * If no timeFormat is supplied, we use the built-in ISO formatter that displays UTC as zone 'Z' and UTC+2 as '+0200'
     */
    private String timeFormat = null;

    /**
     * Sub type of the document (not specified on the message, inferred from JtsDocument.subType)
     */
    private String documentSubType = DocumentSubType.TIMESERIES;

    private Boolean headerEnabled = true;
    private Boolean qualityEnabled = false;
    private Boolean annotationsEnabled = true;

    /**
     * on format DELIMITED_TEXT
     */
    private String delimiter = ",";
    private String textQualifier = "\"";
    private NameFormat headerNameFormat = NameFormat.NAME;
    private FieldFormat qualityFormat = FieldFormat.DELIMITED_WITH_VALUE;
    private String qualityDelimiter = ":";
    private FieldFormat annotationFormat = FieldFormat.DELIMITED_WITH_VALUE;
    private String annotationDelimiter = ";";


    public DocumentFormat() {
    }


    public DocumentFormat(DocumentFormatType formatType) {
        this.formatType = formatType;
    }


    public DocumentFormat(DocumentFormat other) {
        this.formatType = other.formatType;
        this.timeFormat = other.timeFormat;
        this.documentSubType = other.documentSubType;
        this.headerEnabled = other.headerEnabled;
        this.qualityEnabled = other.qualityEnabled;
        this.annotationsEnabled = other.annotationsEnabled;
        this.delimiter = other.delimiter;
        this.textQualifier = other.textQualifier;
        this.headerNameFormat = other.headerNameFormat;
        this.qualityFormat = other.qualityFormat;
        this.qualityDelimiter = other.qualityDelimiter;
        this.annotationFormat = other.annotationFormat;
        this.annotationDelimiter = other.annotationDelimiter;
    }


    private DocumentFormat(Builder builder) {
        this.formatType = builder.formatType;
        this.timeFormat = builder.timeFormat;
        this.documentSubType = builder.documentSubType;
        this.headerEnabled = builder.headerEnabled;
        this.qualityEnabled = builder.qualityEnabled;
        this.annotationsEnabled = builder.annotationsEnabled;
        this.delimiter = builder.delimiter;
        this.textQualifier = builder.textQualifier;
        this.headerNameFormat = builder.headerNameFormat;
        this.qualityFormat = builder.qualityFormat;
        this.qualityDelimiter = builder.qualityDelimiter;
        this.annotationFormat = builder.annotationFormat;
        this.annotationDelimiter = builder.annotationDelimiter;
    }

    @Override
    public String toString() {
        return JacksonUtil.toPrettyJson(this);
    }

    public boolean isPretty() {
        return this.formatType == DocumentFormatType.JSON;
    }

    public DocumentFormat withHeaderEnabled(Boolean headerEnabled) {
        DocumentFormat format = new DocumentFormat(this);
        format.headerEnabled = headerEnabled;
        return format;
    }

    public DocumentFormatType getFormatType() {
        return formatType;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public String getDocumentSubType() {
        return documentSubType;
    }

    public void setDocumentSubType(String documentSubType) {
        this.documentSubType = documentSubType;
    }

    public NameFormat getHeaderNameFormat() {
        return headerNameFormat;
    }

    public Boolean isHeaderEnabled() {
        return headerEnabled;
    }

    public Boolean isQualityEnabled() {
        return qualityEnabled;
    }

    public Boolean isAnnotationsEnabled() {
        return annotationsEnabled;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getTextQualifier() {
        return textQualifier;
    }

    public FieldFormat getQualityFormat() {
        return qualityFormat;
    }

    public String getQualityDelimiter() {
        return qualityDelimiter;
    }

    public FieldFormat getAnnotationFormat() {
        return annotationFormat;
    }

    public String getAnnotationDelimiter() {
        return annotationDelimiter;
    }

    public static class Builder {

        private DocumentFormatType formatType;
        private String timeFormat;
        private String documentSubType;
        private Boolean headerEnabled;
        private Boolean qualityEnabled;
        private Boolean annotationsEnabled;
        private Set<QualityType> qualityExcluded;
        private String delimiter;
        private String textQualifier;
        private NameFormat headerNameFormat;
        private FieldFormat qualityFormat;
        private String qualityDelimiter;
        private FieldFormat annotationFormat;
        private String annotationDelimiter;


        public Builder formatType(DocumentFormatType formatType) {
            this.formatType = formatType;
            return this;
        }


        public Builder timeFormat(String timeFormat) {
            this.timeFormat = timeFormat;
            return this;
        }


        public Builder documentSubType(String documentSubType) {
            this.documentSubType = documentSubType;
            return this;
        }


        public Builder headerEnabled(Boolean headerEnabled) {
            this.headerEnabled = headerEnabled;
            return this;
        }


        public Builder qualityEnabled(Boolean qualityEnabled) {
            this.qualityEnabled = qualityEnabled;
            return this;
        }


        public Builder annotationsEnabled(Boolean annotationsEnabled) {
            this.annotationsEnabled = annotationsEnabled;
            return this;
        }


        public Builder qualityExcluded(Set<QualityType> qualityExcluded) {
            this.qualityExcluded = qualityExcluded;
            return this;
        }


        public Builder delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }


        public Builder textQualifier(String textQualifier) {
            this.textQualifier = textQualifier;
            return this;
        }


        public Builder headerNameFormat(NameFormat headerNameFormat) {
            this.headerNameFormat = headerNameFormat;
            return this;
        }


        public Builder qualityFormat(FieldFormat qualityFormat) {
            this.qualityFormat = qualityFormat;
            return this;
        }


        public Builder qualityDelimiter(String qualityDelimiter) {
            this.qualityDelimiter = qualityDelimiter;
            return this;
        }


        public Builder annotationFormat(FieldFormat annotationFormat) {
            this.annotationFormat = annotationFormat;
            return this;
        }


        public Builder annotationDelimiter(String annotationDelimiter) {
            this.annotationDelimiter = annotationDelimiter;
            return this;
        }


        public DocumentFormat build() {
            return new DocumentFormat(this);
        }
    }
}
