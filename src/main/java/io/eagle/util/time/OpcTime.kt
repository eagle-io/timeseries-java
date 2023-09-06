package io.eagle.util.time

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Period
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

/**
 * OPC Relative Time provides a simple, human readable mechanism for expressing periods of time and timestamps relative to the current time.
 * This class provides methods of converting from OPC Relative Time expressions to Joda time formats.
 *
 *
 * e.g. OPC Period "3Y16H" represents '3 years and 16 hours'.
 *
 *
 * e.g. OPC Relative Time "H-10M" represents 'the start of the current hour minus 10 minutes'.
 *
 *
 * Expressions are parsed using [Java 7 Regular
 * Expressions](http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html).
 *
 *
 *
 * @author [Jarrah Watson](mailto:jarrah@argos.io)
 */
object OpcTime {

    private val logger = LoggerFactory.getLogger(OpcTime::class.java)

    /**
     * Regex string describing an OPC Period, e.g. 5H30M (five hours and thirty minutes)
     */
    private const val OPC_PERIOD_UNITS = "(\\d+)(Y|MO|W|D|H|M|S)"
    private const val OPC_PERIOD = "(?:" + OPC_PERIOD_UNITS + ")+"
    private const val OPC_BASE = "(?<BASE>Y|MO|W|D|H|M|S|NOW)"
    private const val OPC_OFFSET = "(?:\\[(?<OFFSET>Z|(?:[+-]\\d\\d:\\d\\d))\\])?"

    /**
     * Regex string describing an OPC Relative Time, e.g. W-9H (start of the current week minus nine hours)
     */
    private const val OPC_RELATIVE_TIME = "^" + OPC_BASE + OPC_OFFSET + "(?:([+-])(" + OPC_PERIOD + "))?$"

    /**
     * Parse an OPC Relative Time string to a Joda DateTime.
     *
     *
     * Format: `base[timezone](+|-)period`
     *
     *
     * e.g. `MO[+10:00]+7D` (start of current month plus seven days in UTC+10 zone)
     *
     * @param expr     OPC Relative Time string
     * @param baseTime the base DateTime
     * @return Joda DateTime representing the parsed the expressed OPC Relative Time
     * @throws IllegalArgumentException if the expression cannot be parsed
     */
    @JvmStatic
    fun parseTime(expr: String, baseTime: DateTime): DateTime? {
        var expr = expr
        var opcTime: DateTime? = null
        expr = expr.replace("\\s+".toRegex(), "")

        // Convert to uppercase to avoid OPC parsing problems
        expr = expr.toUpperCase()
        val match = Pattern.compile(OPC_RELATIVE_TIME, Pattern.CASE_INSENSITIVE).matcher(expr)
        if (match.matches()) {
            // for( int i=0; i<match.groupCount(); i++ )
            // logger.info( "Group " + i + ": " + match.group( i ) );
            var timezone: DateTimeZone? = null
            timezone = if (match.group("OFFSET") == null) baseTime.zone else getTimezone(match.group("OFFSET"))
            val units = OpcUnits.valueOf(match.group("BASE"))
            val base = getBase(baseTime.withZone(timezone), units)
            opcTime = if (match.group(3) != null) {
                val operator = match.group(3)
                val periodStr = match.group(4)
                val period = parsePeriod(periodStr)
                if (operator == "+") base!!.plus(period) else base!!.minus(period)
            } else {
                base
            }
        } else {
            throw IllegalArgumentException("Invalid expression: $expr")
        }
        return opcTime
    }

    @JvmStatic
    fun isValidTime(expr: String?): Boolean {
        return Pattern.compile(OPC_RELATIVE_TIME, Pattern.CASE_INSENSITIVE).matcher(expr).matches()
    }

    /**
     * Converts an ISO timezone string suffix to a Joda DateTimeZone.
     *
     * @param timezoneSuffix timezone suffix from an ISO date string, e.g. 'Z' or '+08:00'
     * @return Joda DateTimeZone
     */
    private fun getTimezone(timezoneSuffix: String): DateTimeZone? {
        var timezone: DateTimeZone? = null
        timezone = if (timezoneSuffix == "Z") {
            DateTimeZone.UTC
        } else {
            val hoursOffset = Integer.valueOf(timezoneSuffix.split(":".toRegex()).toTypedArray()[0])
            val minutesOffset = Integer.valueOf(timezoneSuffix.split(":".toRegex()).toTypedArray()[1])
            DateTimeZone.forOffsetHoursMinutes(hoursOffset, minutesOffset)
        }
        return timezone
    }

    /**
     * Translate an OPC Base Time unit string to a Joda DateTime.
     *
     * @param units String representing an OPC base time
     * @return Joda DateTime of the specified units
     */
    private fun getBase(time: DateTime, units: OpcUnits): DateTime? {
        var base: DateTime? = null
        base = when (units) {
            OpcUnits.S, OpcUnits.SECOND -> time.secondOfMinute().roundFloorCopy()
            OpcUnits.M, OpcUnits.MINUTE -> time.minuteOfHour().roundFloorCopy()
            OpcUnits.H, OpcUnits.HOUR -> time.hourOfDay().roundFloorCopy()
            OpcUnits.D, OpcUnits.DAY -> time.dayOfWeek().roundFloorCopy()
            OpcUnits.W, OpcUnits.WEEK -> time.weekOfWeekyear().roundFloorCopy()
            OpcUnits.MO, OpcUnits.MONTH -> time.monthOfYear().roundFloorCopy()
            OpcUnits.Y, OpcUnits.YEAR -> time.year().roundFloorCopy()
            OpcUnits.NOW -> time
        }
        return base
    }

    /**
     * Parse an OPC Period string to a [Joda Period](http://joda-time.sourceforge.net/apidocs/org/joda/time/Period.html).
     *
     *
     * The concept of an 'OPC Period' expresses a duration of time in the form of one or more discrete time fields. Note
     * that this duration is not an absolute fixed number of millis. The length of time is variable depending on the instant
     * at which the duration is applied, for example:
     *
     *
     * `2012-01-01T00:00:00Z + '3MO'`
     *
     *
     * is a different length of time to:
     *
     *
     * `2012-03-00T00:00:00Z + '3MO'`
     *
     *
     * due to the shorter month of February.
     *
     *
     * The available OPC Period time field identifiers are:
     *
     *  * `Y` - Year
     *  * `MO` - Month
     *  * `W` - Week
     *  * `D` - Day
     *  * `H` - Hour
     *  * `M` - Minute
     *  * `S` - Second
     *
     *
     *
     * An OPC Period string should consist of one or more `time multiple + time unit` pairs expressed as a string, for example:
     *
     *
     * `'6Y3MO65D8H32M'` defines a period of 6 years, 3 months, 65 days, 8 hours, 32 minutes.
     *
     * @param expr OPC Period string expression, e.g. 5H30M
     * @return Joda Period representing the expressed OPC Period
     * @throws IllegalArgumentException if the expression cannot be parsed
     */
    @JvmStatic
    fun parsePeriod(expr: String?): Period? {
        var expr = expr ?: return null
        var period: Period? = null

        // Remove whitespace
        expr = expr.replace("\\s+".toRegex(), "")

        // Convert to uppercase to avoid OPC parsing problems
        expr = expr.toUpperCase()

        // Period match ensures the entire string consists only of value/unit concatenated pairs
        if (isValidPeriod(expr)) {
            // Single match extracts the groups from each concatenated value/unit pair in turn
            val singleMatch = Pattern.compile(OPC_PERIOD_UNITS, Pattern.CASE_INSENSITIVE).matcher(expr)

            // An OPC Period string can having any number of concatenated value/unit pairs (e.g. 2Y3MO28D9H) so we loop through all matches
            while (singleMatch.find()) {
                if (period == null) period = Period()
                val value = Integer.valueOf(singleMatch.group(1))
                val units = OpcUnits.valueOf(singleMatch.group(2))
                period = period.plus(getPeriod(units, value))
            }
        } else {
            throw IllegalArgumentException("Invalid OPC expression '$expr'")
        }
        return period
    }

    @JvmStatic
    fun isValidPeriod(expr: String?): Boolean {
        return Pattern.compile("^" + OPC_PERIOD + "$", Pattern.CASE_INSENSITIVE).matcher(expr).matches()
    }

    /**
     * Translate OPC Period unit string and value to a Joda Period.
     *
     * @param units string representing the OPC units of the period
     * @param value multiple of units to apply to the period
     * @return the translated Joda Period
     */
    private fun getPeriod(units: OpcUnits, value: Int): Period? {
        var period: Period? = null
        period = when (units) {
            OpcUnits.S, OpcUnits.SECOND -> Period().withSeconds(value)
            OpcUnits.M, OpcUnits.MINUTE -> Period().withMinutes(value)
            OpcUnits.H, OpcUnits.HOUR -> Period().withHours(value)
            OpcUnits.D, OpcUnits.DAY -> Period().withDays(value)
            OpcUnits.W, OpcUnits.WEEK -> Period().withWeeks(value)
            OpcUnits.MO, OpcUnits.MONTH -> Period().withMonths(value)
            OpcUnits.Y, OpcUnits.YEAR -> Period().withYears(value)
            else -> throw IllegalArgumentException("Invalid OPC value '$value'")
        }
        return period
    }

    private enum class OpcUnits {
        S, SECOND, M, MINUTE, H, HOUR, D, DAY, W, WEEK, MO, MONTH, Y, YEAR, NOW
    }
}
