package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.error.ErrorCode;

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

  public static AssetAllocationRegionType fromValue(final String region) {
    for (AssetAllocationRegionType value : values()) {
      if (value.name().equalsIgnoreCase(region) || value.region.equalsIgnoreCase(region)) {
        return value;
      }
    }
    throw ErrorCode.INTERNAL_SERVER_ERROR.toException(String.format("Could not find such Asset Allocation region %s",
        region));
  }

  public String getRegion() {
    return region;
  }
}
