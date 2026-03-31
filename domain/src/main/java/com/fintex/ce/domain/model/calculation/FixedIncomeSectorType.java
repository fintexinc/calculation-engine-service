package com.fintex.ce.domain.model.calculation;

public enum FixedIncomeSectorType {

  GOVERNMENT_BONDS,
  CORPORATE_BONDS,
  OTHER_BONDS,
  MORTGAGE_BACKED_SECURITIES,
  ST_INVESTMENTS,
  ASSET_BACKED_SECURITIES;

  public static FixedIncomeSectorType fromValue(final String typeStr) {
    for (FixedIncomeSectorType value : values()) {
      if (value.name().equalsIgnoreCase(typeStr)) {
        return value;
      }
    }
    return null;
  }

}
