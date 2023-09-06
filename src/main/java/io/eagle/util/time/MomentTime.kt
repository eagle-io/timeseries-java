package io.eagle.util.time

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Iterators
import com.google.common.collect.Lists
import java.lang.IllegalStateException
import humanize.Humanize
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.ArrayList

object MomentTime {
    private const val MOMENT_LITERAL_START = '['
    private const val MOMENT_LITERAL_END = ']'
    private const val JODA_LITERAL_START = '\''
    private const val JODA_LITERAL_END = '\''

    /**
     * Immutable Map of parsing/formatting tokens: Moment.js->Joda.
     *
     *
     * http://momentjs.com/docs/#/displaying/format/
     * http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html
     *
     *
     * Tokens not supported for parsing:
     * Mo, Do, d, do, dd, e, wo, Wo
     *
     *
     * Tokens not supported for formatting:
     * d, do, dd, e
     */
    private val FORMAT_TOKENS: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("M", "M")
            .put("Mo", "") // ORDINAL
            .put("MM", "MM")
            .put("MMM", "MMM")
            .put("MMMM", "MMMM")
            .put("D", "d") // TRANSLATED
            .put("Do", "") // ORDINAL
            .put("DD", "dd") // TRANSLATED
            .put("DDD", "D") // TRANSLATED
            .put("DDDo", "") // ORDINAL
            .put("DDDD", "DDD") // TRANSLATED
            .put("ddd", "E") // TRANSLATED
            .put("dddd", "EEEE") // TRANSLATED
            .put("E", "e") // TRANSLATED
            .put("w", "w")
            .put("wo", "") // ORDINAL
            .put("ww", "ww")
            .put("W", "w") // TRANSLATED
            .put("Wo", "") // ORDINAL
            .put("WW", "ww") // TRANSLATED
            .put("YY", "YY")
            .put("YYYY", "YYYY")
            .put("gg", "xx") // TRANSLATED
            .put("gggg", "xxxx") // TRANSLATED
            .put("GG", "xx") // TRANSLATED
            .put("GGGG", "xxxx") // TRANSLATED
            .put("A", "a") // TRANSLATED
            .put("a", "a")
            .put("H", "H")
            .put("HH", "HH")
            .put("h", "h")
            .put("hh", "hh")
            .put("m", "m")
            .put("mm", "mm")
            .put("s", "s")
            .put("ss", "ss")
            .put("S", "S")
            .put("SS", "SS")
            .put("SSS", "SSS")
            .put("SSSS", "SSSS")
            .put("SSSSS", "SSSSS")
            .put("SSSSSS", "SSSSSS")
            .put("SSSSSSS", "SSSSSSS")
            .put("SSSSSSSS", "SSSSSSSS")
            .put("SSSSSSSSS", "SSSSSSSSS")
            .put("Z", "ZZ") // TRANSLATED
            .put("ZZ", "Z") // TRANSLATED
            .build()

    @JvmStatic
    fun format(dateTime: DateTime, timezone: DateTimeZone, pattern: String): String {
        var dateTime = dateTime
        val tokens = parseTokens(pattern)
        var translated: String? = ""
        dateTime = dateTime.withZone(timezone)
        for (token in tokens) {
            if (FORMAT_TOKENS.containsKey(token)) {
                if (token.endsWith("o")) {
                    var ordinalTimeUnit: Int? = null

                    /* This is an ordinal token which is not supported by Joda, so detect the 
					   time unit and include in the translated string as a rendered ordinal, e.g. 21st */ordinalTimeUnit =
                            if (token == "Mo") dateTime.monthOfYear else if (token == "Do") dateTime.dayOfMonth else if (token == "DDDo") dateTime.dayOfYear else if (token == "wo" || token == "Wo") dateTime.weekOfWeekyear else throw IllegalStateException(
                                    "Unhandled Moment.js ordinal token: $token"
                            )
                    translated += JODA_LITERAL_START.toString() + Humanize.ordinal(ordinalTimeUnit) + JODA_LITERAL_END
                } else {
                    // Token exists in our translation map, so append the related token
                    translated += FORMAT_TOKENS[token]
                }
            } else if (token.matches("[^a-zA-Z]+".toRegex())) {
                // Token is some other random character which does not need to be escaped, so just append
                translated += token
            } else {
                // Token is a letter, so escape with literal qualifiers
                translated += JODA_LITERAL_START.toString() + token + JODA_LITERAL_END
            }
        }
        return dateTime.toString(translated)
    }

    @Suppress("unused")//because we want it to bre parallel with the toMoment stuff
    @JvmStatic
    fun toJoda(pattern: String): String? {
        val tokenList = parseTokens(pattern)
        var translated = translateTokens(tokenList, FORMAT_TOKENS)

        // Strip adjacent single quotes to allow Joda to parse multi-char literals
        translated = translated.replace("''", "")
        return translated
    }

    @JvmStatic
    private fun parseTokens(pattern: String): List<String> {
        val chars = Lists.charactersOf(pattern)
        val it = Iterators.peekingIterator(chars.listIterator())
        val tokens: MutableList<String> = ArrayList()
        var token: String? = null
        while (it.hasNext()) {
            token = ""
            if (it.peek() == MOMENT_LITERAL_START) {
                it.next()

                // String literal
                while (it.hasNext()) {
                    if (it.peek() == MOMENT_LITERAL_END) {
                        it.next()
                        break
                    }
                    token += it.next()
                }
            } else {
                // Token or unescaped literal
                token += it.next()
                while (it.hasNext()) {
                    token += if (it.peek() == token[0] || it.peek() == 'o') it.next() else break
                }
            }
            tokens.add(token)
        }
        return tokens
    }

    @JvmStatic
    private fun translateTokens(tokens: List<String>, tokenMap: Map<String, String>): String {
        var translated = ""
        for (token in tokens) {
            translated += if (tokenMap.containsKey(token)) {
                // Token exists in our translation map, so append the related token
                tokenMap[token]
            } else if (token.matches("[^a-zA-Z]+".toRegex())) {
                // Token is some other random character which does not need to be escaped, so just append
                token
            } else {
                // Token is a letter, so escape with literal qualifiers
                JODA_LITERAL_START.toString() + token + JODA_LITERAL_END
            }
        }
        return translated
    }
}
