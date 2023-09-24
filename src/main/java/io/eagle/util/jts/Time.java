package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Time extends ComplexValue<DateTime> {
    // time expressed as ISO8601 string
    public static final String TIME_KEY = "$time";

    // time expressed as milliseconds since epoch
    public static final String MILLIS_KEY = "$millis";

    @JsonCreator
    public Time(DateTime value) {
        super(value);
    }

    @JsonCreator
    public Time(Long millis) {
        super(new DateTime(millis, DateTimeZone.UTC));
    }

    @Override
    public String getKey() {
        return TIME_KEY;
    }

}
