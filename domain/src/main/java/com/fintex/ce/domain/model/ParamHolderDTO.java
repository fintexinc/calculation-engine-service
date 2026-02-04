package com.fintex.ce.domain.model;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Used to transfer objects between services
 */
@Data
public class ParamHolderDTO {

  // not mandatory
  private Currency currency;

  private Map<Holding, BigDecimal> allocations;

  public ParamHolderDTO() {
  }

  public ParamHolderDTO(final Currency currency) {
    this.currency = currency;
  }

  public ParamHolderDTO(final Map<Holding, BigDecimal> allocations) {
    this.allocations = allocations;
  }
}
