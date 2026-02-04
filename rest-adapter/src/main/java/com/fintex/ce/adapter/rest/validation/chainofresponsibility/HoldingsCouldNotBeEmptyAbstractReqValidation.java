package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.exception.ReqValidationException;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
public abstract class HoldingsCouldNotBeEmptyAbstractReqValidation extends ReqValidation {

  private final List<Holding> holdings;

  public HoldingsCouldNotBeEmptyAbstractReqValidation(final List<Holding> holdings) {
    this.holdings = holdings;
  }

  @Override
  public void check() {
    if (CollectionUtils.isEmpty(holdings)) {
      throw new ReqValidationException(getMessage());
    }
  }

  protected abstract String getMessage();
}
