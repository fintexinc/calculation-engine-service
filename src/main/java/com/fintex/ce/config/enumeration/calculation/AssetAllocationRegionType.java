package com.fintex.ce.config.enumeration.calculation;

import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;

public enum AssetAllocationRegionType {
    CASH("Cash"),
    US_EQUITY("US Equity"),
    OTHER("Other"),
    FIXED_INCOME("Fixed Income"),
    INTERNATIONAL_EQUITY("International Equity"),
    CANADIAN_EQUITY("Canadian Equity"),
    UNCLASSIFIED("Unclassified");

    private String region;

    AssetAllocationRegionType(String region) {
        this.region = region;
    }

    public static AssetAllocationRegionType of(final String region) {
        for (AssetAllocationRegionType value : values()) {
            if (value.name().equalsIgnoreCase(region) || value.region.equalsIgnoreCase(region)) {
                return value;
            }
        }
        final String message = String.format("Could not find such Asset Allocation region %s", region);
        throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public String getRegion() {
        return region;
    }
}
