package io.eagle.util;

import java.text.DecimalFormat;


public class NumberFormat {

    public static final String FORMAT_DEFAULT = "0.###";


    public static String getValueFormatted(Double value, String format) {
        String valueFormatted = null;

        if (value == null)
            valueFormatted = "";
        else if (value.isNaN())
            valueFormatted = "NaN";
        else if (format == null || format.isEmpty())
            valueFormatted = String.valueOf(value);
        else {
            try {
                valueFormatted = new DecimalFormat(format).format(value);
            } catch (IllegalArgumentException e) {
                valueFormatted = String.valueOf(value);
            }
        }

        return valueFormatted;
    }
}
