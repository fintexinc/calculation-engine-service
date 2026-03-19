package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.SecurityIdentifier;
import java.util.List;
import lombok.EqualsAndHashCode;

import static java.math.BigDecimal.ZERO;

@EqualsAndHashCode(callSuper = true)
public class HoldingValueReqValidator extends ReqValidation {

  private final List<Holding> holdings;

  public HoldingValueReqValidator(final List<Holding> holdings) {
    this.holdings = holdings;
  }

  @Override
  public void check() {
    for (Holding holding : holdings) {
      if (holding.getValue() == null) {
        SecurityIdentifier secId = holding.getSecurityIdentifier();
        if (secId != null && secId.getId() != null) {
          throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationErrorWithId(secId.getId());
        }
        throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationError();
      } else if (holding.getValue().compareTo(ZERO) < ZERO.intValue()) {
        throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationError();
      }
    }
  }
}
