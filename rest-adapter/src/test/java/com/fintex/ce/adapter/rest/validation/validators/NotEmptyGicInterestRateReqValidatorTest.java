package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.math.BigDecimal;

class NotEmptyGicInterestRateReqValidatorTest extends AbstractGicFieldReqValidatorTest {

  @Override
  RequestValidator createValidator() {
    return new NotEmptyGicInterestRateReqValidator();
  }

  @Override
  GicHolding createInvalidGicHolding() {
    return GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(null)
        .build();
  }

  @Override
  GicHolding createValidGicHolding() {
    return GicHolding.builder()
        .value(BigDecimal.TEN)
        .holdingType(FinancialInstrumentType.GIC)
        .clientIntRate(BigDecimal.valueOf(0.05))
        .build();
  }

  @Override
  String expectedErrorCode() {
    return "ERR_GIC_MC_001";
  }
}
