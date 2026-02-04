package com.fintex.ce.domain.enumeration.calculation;

public enum GeographicRegionType {

  OTHER("Other"),
  AFRICA("Africa"),
  LATIN_AMERICA("Latin America"),
  CANADA("Canada"),
  US("United States"),
  EUROPE("Europe"),
  ASIA("Asia");

  private String region;

  GeographicRegionType(String region) {
    this.region = region;
  }

  public static GeographicRegionType of(final String region) {
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
