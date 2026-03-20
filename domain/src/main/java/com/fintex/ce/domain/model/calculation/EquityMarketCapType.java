package com.fintex.ce.domain.model.calculation;

import java.util.Set;

public enum EquityMarketCapType {

  GIANT(Set.of("GIANT")),
  LARGE(Set.of("LARGE", "LARGE VALUE", "LARGE CORE", "LARGE GROWTH")),
  MEDIUM(Set.of("MEDIUM", "MID VALUE", "MID CORE", "MID GROWTH")),
  SMALL(Set.of("SMALL", "SMALL VALUE", "SMALL CORE", "SMALL GROWTH")),
  MICRO(Set.of("MICRO"));

  private Set<String> names;

  EquityMarketCapType(Set<String> names) {
    this.names = names;
  }

  public static EquityMarketCapType of(final String typeStr) {
    for (EquityMarketCapType value : values()) {
      if (value.getNames().contains(typeStr.toUpperCase())) {
        return value;
      }
    }
    return null;
  }

  public Set<String> getNames() {
    return names;
  }

  public boolean contains(final String type) {
    return this.names.contains(type);
  }

}
