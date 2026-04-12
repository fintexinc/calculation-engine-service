package com.fintex.ce.domain.model.calculation;

import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.HttpCode;

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
    final String message = String.format("Could not find region for %s", region);
    throw new SystemException(message, HttpCode.INTERNAL_SERVER_ERROR);
  }

}
