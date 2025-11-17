package com.fintex.ce.config.enumeration.calculation;

import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;

public enum GeographicRegionType {

    OTHER("Other"),
    AFRICA("Africa"),
    LATIN_AMERICA("Latin America"),
    CANADA("Canada"),
    US("United States"),
    EUROPE("Europe"),
    ASIA("Asia");

    private String region;

    GeographicRegionType(String region) {
        this.region = region;
    }

    public static GeographicRegionType of(final String region) {
        for (GeographicRegionType value : values()) {
            if (value.name().equalsIgnoreCase(region) || value.region.equalsIgnoreCase(region)) {
                return value;
            }
        }
        final String message = String.format("Could not find region for %s", region);
        throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public String getRegion() {
        return region;
    }

}
