package io.eagle.util.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;


public class OpcTimeTest {
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger( OpcTimeTest.class );

    @Test
    public void testInvalidPeriod() {
        assertThrows( IllegalArgumentException.class, () -> {
            OpcTime.parsePeriod( "3MOP" );
        } );
    }

    @Test
    public void testParsePeriod() {
        Period parsed = null;
        Period fixed = null;

        parsed = OpcTime.parsePeriod( "3MO" );
        fixed = new Period()
                .withMonths( 3 );
        assertEquals( parsed, fixed, "Expect parsed period matches static" );

        parsed = OpcTime.parsePeriod( "30D6Y390S" );
        fixed = new Period()
                .withDays( 30 )
                .withYears( 6 )
                .withSeconds( 390 );
        assertEquals( parsed, fixed, "Expect parsed period matches static" );
    }

    @Test
    @Disabled("legacy test")
    public void testParseRelativeTime() {
        DateTime parsed = null;
        DateTime fixed = null;
        DateTime now = DateTime.now( DateTimeZone.UTC );

        parsed = OpcTime.parseTime( "", now );
        assertNull( parsed, "Expect invalid time is null" );

        parsed = OpcTime.parseTime( "Y", now );
        assertNull( parsed, "Expect invalid time is null" );

        parsed = OpcTime.parseTime( "W[Z]+10M9H+56M", now );
        assertNull( parsed, "Expect invalid time is null" );

        parsed = OpcTime.parseTime( "W[+9:00]", now );
        assertNull( parsed, "Expect invalid time is null" );

        parsed = OpcTime.parseTime( "W[+09:00]", now );
        fixed = new DateTime()
                .withZone( DateTimeZone.forOffsetHours( 9 ) )
                .weekOfWeekyear().roundFloorCopy();
        assertEquals( parsed, fixed, "Expect base with timezone matches static" );

        parsed = OpcTime.parseTime( "MO[-08:00]+2H30M", now );
        fixed = new DateTime()
                .withZone( DateTimeZone.forOffsetHours( - 8 ) )
                .monthOfYear().roundFloorCopy()
                .plusHours( 2 )
                .plusMinutes( 30 );
        assertEquals( parsed, fixed, ", Expect base parsed time matches static" );

        parsed = OpcTime.parseTime( "W[Z]+10M9H", now );
        fixed = new DateTime()
                .withZone( DateTimeZone.UTC )
                .weekOfWeekyear().roundFloorCopy()
                .plusHours( 9 )
                .plusMinutes( 10 );
        assertEquals( parsed, fixed, "Expect base , with offset parsed time matches static" );
    }

}
