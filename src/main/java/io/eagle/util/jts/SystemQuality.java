package io.eagle.util.jts;

import java.util.HashMap;
import java.util.Map;


public enum SystemQuality {

    UNCERTAIN_SUBNORMAL(149),
    BAD_NO_DATA(155),            // No data available after bad quality values have been excluded
    BAD_DATA_UNAVAILABLE(158),
    GOOD_ENTRY_INSERTED(162),
    GOOD_NO_DATA(165),            // No data available for the period

    /* Internal use only */
    BAD_NO_BOUNDS(-156),        // No data available to calculate bounding value (chart bounds for raw data)
    GOOD_BOUNDS(-156),        // Calculated bounding value (chart bounds for raw data)
    DELETE(-666),                // JtsField marked for deletion
    UNSET(-1),                // Quality code not set
    UNKNOWN(-999);            // Quality code not defined on owner

    private static final Map<Integer, SystemQuality> intToTypeMap = new HashMap<>();

    /**
     * Initialize the Map so that every type in the {@link SystemQuality} ENUM can be located by its associated outcomeCode int.
     */
    static {
        for (SystemQuality type : SystemQuality.values())
            intToTypeMap.put(type.code, type);
    }

    private final Integer code;

    SystemQuality(Integer code) {
        this.code = code;
    }

    public static SystemQuality getEnum(Integer code) {
        if (intToTypeMap.containsKey(code))
            return intToTypeMap.get(code);
        else
            return null;
    }

    public Integer getCode() {
        return this.code;
    }
}
