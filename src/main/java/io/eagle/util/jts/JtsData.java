package io.eagle.util.jts;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


public interface JtsData {
    String toDelimitedText( JtsDocumentHeader header, DocumentFormat documentFormat, DateTimeZone timezone );

    String toFixedWidth( JtsDocumentHeader header, DocumentFormat documentFormat, DateTimeZone timezone );

    DateTime getFirstTimestamp();

    DateTime getLastTimestamp();

    boolean isEmpty();

    int recordCount();

    int fieldCount();
}
