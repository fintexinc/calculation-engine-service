package com.fintex.ce.domain.model.calculation;

public enum GeographicRegionType {

  OTHER("Other"),
  AFRICA("Africa"),
  LATIN_AMERICA("Latin America"),
  CANADA("Canada"),
  US("United States"),
  EUROPE("Europe"),
  ASIA("Asia");

  private final String region;

  GeographicRegionType(String region) {
    this.region = region;
  }

  public static GeographicRegionType fromValue(final String region) {
    for (GeographicRegionType value : values()) {
      if (value.name().equalsIgnoreCase(region) || value.region.equalsIgnoreCase(region)) {
        return value;
      }
    }
    return null;
  }

  public String getRegion() {
    return region;
  }

}
