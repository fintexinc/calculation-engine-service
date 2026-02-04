package com.fintex.ce.domain.enumeration.calculation;

import lombok.Getter;

@Getter
public enum FixedIncomeStyleboxType {

  HIGH_LIMITED("High limited"),
  HIGH_MODERATE("High moderate"),
  HIGH_EXTENSIVE("High extensive"),
  MEDIUM_LIMITED("Medium limited"),
  MEDIUM_MODERATE("Medium moderate"),
  MEDIUM_EXTENSIVE("Medium extensive"),
  LOW_LIMITED("Low limited"),
  LOW_MODERATE("Low Moderate"),
  LOW_EXTENSIVE("Low Extensive");
  private final String name;

  FixedIncomeStyleboxType(String name) {
    this.name = name;
  }

  public static FixedIncomeStyleboxType of(final String typeStr) {
    for (FixedIncomeStyleboxType value : values()) {
      if (value.name().equalsIgnoreCase(typeStr)) {
        return value;
      }
    }
    return null;
  }

}
