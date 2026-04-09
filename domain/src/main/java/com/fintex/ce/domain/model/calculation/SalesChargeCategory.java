package com.fintex.ce.domain.model.calculation;

import com.fintex.sm.model.domain.enumeration.SalesChargeType;

import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Categorizes raw SalesChargeType values from Security Master into broader groups for calculation purposes.
 */
@Slf4j
@Getter
public enum SalesChargeCategory {

  NO_LOAD_INITIAL_SALES_CHARGE(Set.of(SalesChargeType.FRONT_END_CHARGE, SalesChargeType.VOLUME_SALES_CHARGE,
      SalesChargeType.FORMULA_ONE, SalesChargeType.NO_SALES_OR_REDEMPTION_CHARGE, SalesChargeType.NO_LOAD,
      SalesChargeType.INITIAL_SALES_CHARGE)),
  LOW_LOAD_SALES_CHARGE(Set.of(SalesChargeType.LOW_SALES_CHARGE, SalesChargeType.LOW_LOAD_SALES_CHARGE)),
  DEFERRED_SALES_CHARGE(Set.of(SalesChargeType.DEFERRED_SALES_CHARGE_ON_MARKET_VALUE,
      SalesChargeType.DEFERRED_CHARGE_ON_ORIGINAL_AMOUNT, SalesChargeType.DEFERRED_SALES_CHARGE,
      SalesChargeType.GROUP_SALES_CHARGE, SalesChargeType.REDEMPTION_CHARGE));

  private final Set<SalesChargeType> types;

  SalesChargeCategory(final Set<SalesChargeType> types) {
    this.types = types;
  }

  public static SalesChargeCategory fromValue(final SalesChargeType type) {
    if (type == null) {
      return null;
    }
    for (final SalesChargeCategory value : values()) {
      if (value.getTypes().contains(type)) {
        return value;
      }
    }
    log.debug("No SalesChargeCategory match for type: {}", type);
    return null;
  }

}
