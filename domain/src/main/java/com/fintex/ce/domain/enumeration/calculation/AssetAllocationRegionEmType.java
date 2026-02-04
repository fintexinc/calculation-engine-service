package com.fintex.ce.domain.enumeration.calculation;

import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.ErrorCode;
import lombok.Getter;

// AssetAllocationEmergingMarket
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

  public static AssetAllocationRegionEmType of(final String region) {
    for (AssetAllocationRegionEmType value : values()) {
      if (value.name().equalsIgnoreCase(region) || value.region.equalsIgnoreCase(region)) {
        return value;
      }
    }
    final String message = String.format("Could not find such Asset Allocation Em region %s", region);
    throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
  }

}
