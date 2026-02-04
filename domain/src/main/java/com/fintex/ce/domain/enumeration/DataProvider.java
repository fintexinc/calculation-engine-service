package com.fintex.ce.domain.enumeration;

public enum DataProvider {

  BLACKROCK,
  EAGLE,
  METATAGSEXCEL,
  MORNINGSTAR,
  PHNDB,
  SEISMIC,
  MANAGEMENTFUNDEXCEL,
  UNKNOWN_VALUE,
  ENVESTNET,
  BROADRIDGE,
  PAG;

  public static DataProvider of(final String provider) {
    if (provider == null) {
      return null;
    }
    for (DataProvider value : values()) {
      if (value.name().equalsIgnoreCase(provider)) {
        return value;
      }
    }
    return null;
  }

  public static final DataProvider[] DEFAULT_PROVIDERS = {EAGLE, MORNINGSTAR};
}
