package com.fintex.ce.domain.enumeration.calculation;

import lombok.Getter;

import java.util.Set;

@Getter
public enum SalesCharge {

  NO_LOAD_INITIAL_SALES_CHARGE(Set.of("FRONT_END_CHARGE", "VOLUME_SALES_CHARGE", "FORMULA_ONE",
      "NO_SALES_OR_REDEMPTION_CHARGE")),
  LOW_LOAD_SALES_CHARGE(Set.of("LOW_SALES_CHARGE")),
  DEFERRED_SALES_CHARGE(Set.of("DEFERRED_SALES_CHARGE_ON_MARKET_VALUE", "DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT",
      "DEFERRED_SALES_CHARGE",
      "GROUP_SALES_CHARGE", "REDEMPTION_CHARGE"));

  private final Set<String> types;

  SalesCharge(final Set<String> types) {
    this.types = types;
  }

  public static SalesCharge of(final String type) {
    if (type == null) {
      return null;
    }
    for (final SalesCharge value : values()) {
      if (value.getTypes().contains(type.toUpperCase())) {
        return value;
      }
    }
    return null;
  }

}
