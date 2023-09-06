package io.eagle.util;


import org.bson.BSONObject;

/**
 * Any object which can describe itself with a dollar key, for example {@code $foobar}.
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
public interface ComplexValue {

    /**
     * Key used when representing a
     */
    String TIME_MILLIS_KEY = "$millis";
    String TIME_ISO_KEY = "$time";
    String COORDS_KEY = "$coords";
    String WIND_DIR_KEY = "$winddir";

    /**
     * Returns a {@link BSONObject} representing this ComplexValue.
     *
     * @return a {@link BSONObject} representing this ComplexValue
     */
    BSONObject toDBObject();


    /**
     * Returns a String representing this ComplexValue as delimited text.
     *
     * @param delimiter     delimiter to use when constructing delimited fields, never null
     * @param textQualifier string used to surround any text field values, never null, but may be the empty String
     * @return a String representing this ComplexValue as delimited text
     */
    String toDelimitedText(final String delimiter, final String textQualifier);
}
