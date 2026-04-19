package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.error.ErrorCode;

import lombok.Getter;

@Getter
public enum AssetAllocationRegionEmType {
  CASH("Cash"),
  US_EQUITY("US Equity"),
  OTHER("Other"),
  FIXED_INCOME("Fixed Income"),
  INTERNATIONAL_EQUITY("International Equity"),
  EMERGING_MARKET_EQUITY("Equity Market Equity"),
  CANADIAN_EQUITY("Canadian Equity"),
  UNCLASSIFIED("Unclassified");

  private final String region;

  AssetAllocationRegionEmType(String region) {
    this.region = region;
  }

  public static AssetAllocationRegionEmType fromValue(final String region) {
    for (AssetAllocationRegionEmType value : values()) {
      if (value.name().equalsIgnoreCase(region) || value.region.equalsIgnoreCase(region)) {
        return value;
      }
    }
    throw ErrorCode.INTERNAL_SERVER_ERROR.toException(
        String.format("Could not find such Asset Allocation Em region %s", region));
  }

}
