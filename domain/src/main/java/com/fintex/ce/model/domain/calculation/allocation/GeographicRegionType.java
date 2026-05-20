package com.fintex.ce.model.domain.calculation.allocation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GeographicRegionType {

  OTHER("Other"),
  AFRICA("Africa"),
  LATIN_AMERICA("Latin America"),
  CANADA("Canada"),
  US("United States"),
  EUROPE("Europe"),
  ASIA("Asia");

  private final String region;

  public static GeographicRegionType fromValue(final String region) {
    for (GeographicRegionType value : values()) {
      if (value.name().equalsIgnoreCase(region) || value.region.equalsIgnoreCase(region)) {
        return value;
      }
    }
    return null;
  }

}
