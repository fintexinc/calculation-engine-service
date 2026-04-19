package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.error.ErrorCode;

import lombok.Getter;

@Getter
public enum CountryRegionType {

  CANADA("Canada"),
  UNITED_STATES("United States"),
  INTERNATIONAL_DEVELOPED("International-Developed"),
  EMERGING_MARKET("Emerging Market");

  private final String region;

  CountryRegionType(String region) {
    this.region = region;
  }

  public static CountryRegionType fromValue(final String region) {
    for (CountryRegionType value : values()) {
      if (value.name().equalsIgnoreCase(region) || value.region.equalsIgnoreCase(region)) {
        return value;
      }
    }
    throw ErrorCode.INTERNAL_SERVER_ERROR.toException(String.format("Could not find region for %s", region));
  }

}
