package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.holding.Holding;

import java.util.List;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class BenchmarksCouldNotBeEmptyReqValidation extends HoldingsCouldNotBeEmptyAbstractReqValidation {

  public BenchmarksCouldNotBeEmptyReqValidation(final List<Holding> holdings) {
    super(holdings);
  }

  @Override
  protected String getMessage() {
    return "Benchmarks should not be empty";
  }
}
