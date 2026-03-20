package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.util.List;

import static java.util.Objects.isNull;

@EqualsAndHashCode(callSuper = true)
public class NotEmptyGicInterestRateReqValidator extends ReqValidation {

  private final List<Holding> holdings;

  public NotEmptyGicInterestRateReqValidator(final List<Holding> holdings) {
    this.holdings = holdings;
  }

  @Override
  protected void check() {
    for (final Holding holding : holdings) {
      if (holding instanceof GicHolding) {
        final var gic = (GicHolding) holding;
        if (isNull(gic.getClientIntRate())) {
          throw ExceptionCode.ERR_GIC_MC_001.reqValidationError();
        }
      }
    }
  }
}
