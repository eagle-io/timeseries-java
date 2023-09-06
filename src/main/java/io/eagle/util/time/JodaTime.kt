package io.eagle.util.time

import humanize.Humanize
import org.apache.commons.lang3.time.DurationFormatUtils
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Predicate

/**
 * Helper class to store methods associated with the Joda time library.
 *
 * @author [Jarrah Watson](mailto:jarrah@eagle.io)
 */
object JodaTime {
    /**
     * Maximum possible Joda DateTime
     */
    @JvmField
    val MAX_DATETIME = DateTime(9999, 1, 1, 0, 0, DateTimeZone.UTC)

    /**
     * Minimum possible Joda DateTime
     */
    @JvmField
    val MIN_DATETIME = DateTime(1800, 1, 1, 0, 0, DateTimeZone.UTC)
    @JvmField
    val MIN_LOCALDATE = LocalDate(MIN_DATETIME, DateTimeZone.UTC)
    @JvmField
    val MAX_LOCALDATE = LocalDate(MAX_DATETIME, DateTimeZone.UTC)


    private val logger = LoggerFactory.getLogger(JodaTime::class.java)
    @JvmStatic
    fun min(vararg dateTimes: DateTime?): DateTime? {
        return Arrays.asList(*dateTimes).stream()
                .filter(Predicate { obj: DateTime? -> Objects.nonNull(obj) })
                .min(Comparator.naturalOrder())
                .orElse(null)
    }

    @JvmStatic
    fun max(vararg dateTimes: DateTime?): DateTime? {
        return Arrays.asList(*dateTimes).stream()
                .filter(Predicate { obj: DateTime? -> Objects.nonNull(obj) })
                .max(Comparator.naturalOrder())
                .orElse(null)
    }

    /**
     * Returns true if the given Period defines some length of time, otherwise returns false.
     *
     * @param period the Period to check
     * @return true if the given Period defines some length of time, otherwise returns false
     */
    @JvmStatic
    fun periodisNotEmpty(period: Period?): Boolean {
        // Check if the interval has some length, by adding it to the minimum DateTime; this should create a DateTime after the minimum
        // DateTime
        return MIN_DATETIME.plus(period).isAfter(MIN_DATETIME)
    }

    /**
     * Determine the Standard Time UTC offset for the specified time zone.
     *
     * @param timezone time zone to resolve
     * @return DateTimeZone describing the non-DST UTC Offset of the specified time zone
     */
    @JvmStatic
    fun getStandardZone(timezone: DateTimeZone): DateTimeZone {
        return DateTimeZone.forOffsetMillis(timezone.getStandardOffset(DateTime.now(DateTimeZone.UTC).millis))
    }

    /**
     * Determine whether two times share the same calendar day (using zone of first time).
     *
     * @param t1 first time
     * @param t2 second time
     * @return boolean indicating whether they share the same day
     */
    @JvmStatic
    fun isSameDay(t1: DateTime?, t2: DateTime?): Boolean {
        return if (t1 == null || t2 == null) false else t1.toLocalDate() == t2.withZone(t1.zone).toLocalDate()
    }

    @JvmStatic
    fun getTimezoneAdjusted(timezone: DateTimeZone, adjustForDst: Boolean): DateTimeZone {
        return if (adjustForDst) timezone else getStandardZone(timezone)
    }

    /**
     * Parse string as OPC Time and render to DateTime using the timestamp provided.
     *
     * If the string cannot be parsed as an OPC Time, attempt to parse as ISO Date string.
     */
    @JvmStatic
    fun calcBaseTime(baseTimeStr: String, timestamp: DateTime?): DateTime? {
        var baseTime: DateTime? = if (baseTimeStr.isNotBlank()) {
            try {
                // Attempt to parse as OPC, using the given timestamp
                OpcTime.parseTime(baseTimeStr, timestamp!!)
            } catch (e: Exception) {
                // Could not parse as OPC, so parse as Joda instead
                parseDateTimeISO(baseTimeStr)
                null
            }
        } else {
            timestamp
        }
        return baseTime
    }

    @JvmStatic
    fun parseDateISO(text: String): LocalDate? {
        var date = try {
            ISODateTimeFormat.date().parseLocalDate(text)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid expression: $text")
        }
        return date
    }

    @JvmStatic
    fun parseDateTime(text: String, baseTime: DateTime?): DateTime? {
        return try {
            parseDateTimeISO(text)
        } catch (e: RuntimeException) {
            OpcTime.parseTime(text, baseTime!!)
        }
    }

    /**
     * Parse a string to a Joda DateTime, or null if the given expression could not be parsed.
     *
     * @param text string expression conforming to USER_TIME_FORMAT or USER_TIME_MILLIS_FORMAT
     * @return parsed Joda DateTime, or null if the given expression could not be parsed
     */
    @JvmStatic
    fun parseDateTimeISO(text: String): DateTime? {
        var dateTime = try {
            try {
                ISODateTimeFormat.dateTimeNoMillis().withOffsetParsed().parseDateTime(text)
            } catch (userTimeException: IllegalArgumentException) {
                ISODateTimeFormat.dateTime().withOffsetParsed().parseDateTime(text)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid expression: $text")
        }
        return dateTime
    }

    @JvmStatic
    fun parseLocalSeconds(text: String, timezone: DateTimeZone?): DateTime {
        val localMillis = text.toLong() * 1000
        val localDateTime = LocalDateTime(localMillis, DateTimeZone.UTC)
        return localDateTime.toDateTime(timezone).withZone(DateTimeZone.UTC)
    }

    @JvmStatic
    fun parseDateTime(text: String, pattern: String, timezone: DateTimeZone?): DateTime? {
        // Ensure we found some pattern string in the config, and some dateTime string in the record data
        var text = text
        if (text.isBlank()) {
            throw IllegalArgumentException("Invalid datetime string '%s'".format(text))
        }
        if (pattern.isBlank()) {
            throw IllegalArgumentException("Invalid datetime format '%s'".format(pattern))
        }

        var dateTime: DateTime? = null
        val dateTimeFormatter = buildDateTimeFormatter(
                pattern,  // e.g. "YYYY-MM-dd HH:mm:ssZ"
                timezone
        ) // null, or e.g. "Australia/Sydney"

        // TODO: this is a hack just to get some Aquamonix files working
        // Should be replaced with a better way of parsing the various "am/pm" options
        text = text.replace("a.m.", "am")
        text = text.replace("p.m.", "pm")
        dateTime = try {
            // Attempt to build a Joda DateTime using the formatter and the dateTime string
            dateTimeFormatter.parseDateTime(text)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                    String.format(
                            "Error parsing datetime string '%s' with format '%s'",
                            text,
                            pattern
                    )
            )
        }
        return dateTime
    }

    @JvmStatic
    fun parseDate(text: String, pattern: String): LocalDate? {
        if (text.isBlank()) {
            throw IllegalArgumentException("Invalid datetime string '%s'".format(text))
        }
        if (pattern.isBlank()) {
            throw IllegalArgumentException("Invalid datetime format '%s'".format(pattern))
        }

        var date: LocalDate? = null
        val dateTimeFormatter = buildDateTimeFormatter(
                pattern,  // e.g. "YYYY-MM-dd HH:mm:ssZ"
                null
        ) // null, or e.g. "Australia/Sydney"
        date = try {
            // Attempt to build a Joda LocalDate using the formatter and the date string
            dateTimeFormatter.parseLocalDate(text)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                    String.format(
                            "Error parsing date string '%s' with format '%s'",
                            text,
                            pattern
                    )
            )
        }
        return date
    }

    @JvmStatic
    fun buildDateTimeFormatter(pattern: String, timezone: DateTimeZone?): DateTimeFormatter {
        var dateTimeFormatter = DateTimeFormat.forPattern(pattern)

        // Check if the pattern string includes timezone information
        dateTimeFormatter = if (pattern.contains("Z")) {
            logger.trace("Timezone information in DateTime String will be applied")

            // The dateTime will have a fixed offset based on the parsed timezone
            dateTimeFormatter.withOffsetParsed()
        } else {
            // The dateTime will have the timezone specified by the property (or UTC if the property was not set)
            if (timezone == null) dateTimeFormatter.withZone(DateTimeZone.UTC) else dateTimeFormatter.withZone(
                    timezone
            )
        }
        return dateTimeFormatter
    }

    @JvmStatic
    fun floor(dateTime: DateTime, period: Period): DateTime {
        var dateTimeFloor = dateTime
        if (period.years != 0) dateTimeFloor = dateTime.yearOfEra().roundFloorCopy()
                .minusYears(dateTime.yearOfEra % period.years) else if (period.months != 0) dateTimeFloor =
                dateTime.monthOfYear().roundFloorCopy()
                        .minusMonths((dateTime.monthOfYear - 1) % period.months) else if (period.weeks != 0) dateTimeFloor =
                dateTime.weekOfWeekyear().roundFloorCopy()
                        .minusWeeks((dateTime.weekOfWeekyear - 1) % period.weeks) else if (period.days != 0) dateTimeFloor =
                dateTime.dayOfMonth().roundFloorCopy()
                        .minusDays((dateTime.dayOfMonth - 1) % period.days) else if (period.hours != 0) dateTimeFloor =
                dateTime.hourOfDay().roundFloorCopy()
                        .minusHours(dateTime.hourOfDay % period.hours) else if (period.minutes != 0) dateTimeFloor =
                dateTime.minuteOfHour().roundFloorCopy()
                        .minusMinutes(dateTime.minuteOfHour % period.minutes) else if (period.seconds != 0) dateTimeFloor =
                dateTime.secondOfMinute().roundFloorCopy()
                        .minusSeconds(dateTime.secondOfMinute % period.seconds) else if (period.millis != 0) dateTimeFloor =
                dateTime.millisOfSecond().roundCeilingCopy().minusMillis(dateTime.millisOfSecond % period.millis)
        return dateTimeFloor
    }

    @JvmStatic
    fun ceiling(dateTime: DateTime, period: Period): DateTime {
        return floor(dateTime, period).plus(period).minusMillis(1)
    }

    @JvmStatic
    fun nowSeconds(): Int {
        return (now().millis / 1000).toInt()
    }

    fun nowMillis(): Long {
        return now().millis
    }

    @JvmStatic
    fun now(): DateTime {
        return DateTime.now(DateTimeZone.UTC)
    }

    fun difference(t1: DateTime?, t2: DateTime?): Duration {
        return Duration.millis(Math.abs(Duration(t1, t2).millis))
    }

    @JvmStatic
    fun differenceFromNow(t: DateTime?): Duration {
        return difference(now(), t)
    }

    @JvmStatic
    fun toPretty(start: DateTime?): String {
        return if (start == null) "never" else Humanize.naturalTime(start.toDate())
    }

    @JvmStatic
    fun formatDuration(dateTime: DateTime?): String {
        return DurationFormatUtils.formatDurationWords(differenceFromNow(dateTime).millis, true, true)
    }
}
