package io.eagle.util.time

import org.joda.time.DateTime
import org.joda.time.Period
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory


class JodaTimeTest {


    @Test
    fun testDateTimeFloor() {
        val dateTimeFloor = JodaTime.floor(DateTime(2013, 1, 1, 0, 47, 0), Period(0, 15, 0, 0))

        assertEquals(DateTime(2013, 1, 1, 0, 45, 0), dateTimeFloor)
        logger.info("dateTimeFloor: '{}'", dateTimeFloor)
    }

    @Test
    @Disabled("legacy test")
    fun testDateTimeCeil() {
        val dateTimeCeil = JodaTime.ceiling(DateTime(2013, 1, 1, 0, 32, 0), Period(2, 15, 0, 0))

        assertEquals(DateTime(2013, 1, 1, 2, 45, 0), dateTimeCeil)
        logger.info("dateTimeCeil: '{}'", dateTimeCeil)
    }


    /**
     * Test method for [io.eagle.util.time.JodaTime.periodisNotEmpty].
     */
    @Test
    fun testPeriodisNotEmpty() {
        // Check new Period with no parameters has no length
        assertFalse(JodaTime.periodisNotEmpty(Period()))

        // Check any new Periods created with a zero of any unit have no length
        assertFalse(JodaTime.periodisNotEmpty(Period().withMillis(0)))
        assertFalse(JodaTime.periodisNotEmpty(Period().withSeconds(0)))
        assertFalse(JodaTime.periodisNotEmpty(Period().withMinutes(0)))
        assertFalse(JodaTime.periodisNotEmpty(Period().withHours(0)))
        assertFalse(JodaTime.periodisNotEmpty(Period().withDays(0)))
        assertFalse(JodaTime.periodisNotEmpty(Period().withWeeks(0)))
        assertFalse(JodaTime.periodisNotEmpty(Period().withMonths(0)))
        assertFalse(JodaTime.periodisNotEmpty(Period().withYears(0)))

        // Check any new Periods created with 1 of any unit have length
        assertTrue(JodaTime.periodisNotEmpty(Period().withMillis(1)))
        assertTrue(JodaTime.periodisNotEmpty(Period().withSeconds(1)))
        assertTrue(JodaTime.periodisNotEmpty(Period().withMinutes(1)))
        assertTrue(JodaTime.periodisNotEmpty(Period().withHours(1)))
        assertTrue(JodaTime.periodisNotEmpty(Period().withDays(1)))
        assertTrue(JodaTime.periodisNotEmpty(Period().withWeeks(1)))
        assertTrue(JodaTime.periodisNotEmpty(Period().withMonths(1)))
        assertTrue(JodaTime.periodisNotEmpty(Period().withYears(1)))
    }


    @Test
    fun min() {
        assertNull(JodaTime.min(null, null));
    }

    @Test
    fun max() {
        assertNull(JodaTime.max(null, null));
    }

    companion object {

        /** slf4j static logger  */
        private val logger = LoggerFactory.getLogger(JodaTimeTest::class.java)
    }
}
