package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.holding.Holding;

import java.util.List;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class HoldingsCouldNotBeEmptyReqValidation extends HoldingsCouldNotBeEmptyAbstractReqValidation {
  public HoldingsCouldNotBeEmptyReqValidation(final List<Holding> holdings) {
    super(holdings);
  }

  @Override
  protected String getMessage() {
    return "Holdings could not be empty";
  }
}
