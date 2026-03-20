package com.fintex.ce.domain.model.calculation;

import lombok.Getter;

@Getter
public enum AssetAllocationRegion {
  CASH("Cash", AssetAllocationRegionType.CASH),
  FIXED_INCOME("Fixed Income", AssetAllocationRegionType.FIXED_INCOME),
  OTHER("Other", AssetAllocationRegionType.OTHER),
  CANADIAN_EQUITIES("Canadian Equities", AssetAllocationRegionType.CANADIAN_EQUITY),
  US_EQUITIES("U.S. Equities", AssetAllocationRegionType.US_EQUITY),
  EUROPEAN_EQUITIES("European Equities", AssetAllocationRegionType.INTERNATIONAL_EQUITY),
  ASIA_PACIFIC_EQUITIES("Asia-Pacific Equities", AssetAllocationRegionType.INTERNATIONAL_EQUITY),
  EM_EQUITIES("Emerging Markets Equities", AssetAllocationRegionType.INTERNATIONAL_EQUITY),
  INTERNATIONAL_EQUITIES("International Equities", AssetAllocationRegionType.INTERNATIONAL_EQUITY),
  REAL_ESTATE("Real Estate", AssetAllocationRegionType.OTHER),
  UNCLASSIFIED("Unclassified", AssetAllocationRegionType.UNCLASSIFIED);

  private final String name;
  private final AssetAllocationRegionType assetAllocationRegionType;

  AssetAllocationRegion(String name, AssetAllocationRegionType assetAllocationRegionType) {
    this.name = name;
    this.assetAllocationRegionType = assetAllocationRegionType;
  }

  public static AssetAllocationRegion of(final String regionName) {
    for (AssetAllocationRegion value : values()) {
      if (value.getName().equalsIgnoreCase(regionName)) {
        return value;
      }
    }
    return null;
  }

}
