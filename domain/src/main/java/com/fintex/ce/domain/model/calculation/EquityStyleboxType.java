package com.fintex.ce.domain.model.calculation;

public enum EquityStyleboxType {

  LARGE_CORE("Large core"),
  LARGE_GROWTH("Large growth"),
  LARGE_VALUE("Large value"),
  MID_CORE("Mid core"),
  MID_GROWTH("Mid growth"),
  MID_VALUE("Mid value"),
  SMALL_CORE("Small core"),
  SMALL_GROWTH("Small growth"),
  SMALL_VALUE("Small value");
  private final String name;

  EquityStyleboxType(String name) {
    this.name = name;
  }

  public static EquityStyleboxType of(final String typeStr) {
    for (EquityStyleboxType value : values()) {
      if (value.name().equalsIgnoreCase(typeStr)) {
        return value;
      }
    }
    return null;
  }

  public String getName() {
    return name;
  }
}
