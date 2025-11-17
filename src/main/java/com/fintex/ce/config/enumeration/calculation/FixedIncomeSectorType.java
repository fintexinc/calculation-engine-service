package com.fintex.ce.config.enumeration.calculation;

public enum FixedIncomeSectorType {

    GOVERNMENT_BONDS,
    CORPORATE_BONDS,
    OTHER_BONDS,
    MORTGAGE_BACKED_SECURITIES,
    ST_INVESTMENTS,
    ASSET_BACKED_SECURITIES;


    public static FixedIncomeSectorType of(final String typeStr) {
        for (FixedIncomeSectorType value : values()) {
            if (value.name().equalsIgnoreCase(typeStr)) {
                return value;
            }
        }
        return null;
    }

}
