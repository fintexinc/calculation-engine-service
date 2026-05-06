package com.fintex.ce.application.validation;

import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.model.error.ErrorCode;

import java.util.List;

class PortfolioCpsdDataValidationTest extends AbstractCpsdDataValidationTest {

  private final PortfolioCpsdDataValidation validation = new PortfolioCpsdDataValidation();

  @Override
  protected CpsdDataValidation validator() {
    return validation;
  }

  @Override
  protected ErrorCode expectedAfterPedCode() {
    return ErrorCode.CPSD_AFTER_PORTFOLIO_PED;
  }

  @Override
  protected ErrorCode expectedBeforePsdCode() {
    return ErrorCode.CPSD_BEFORE_PORTFOLIO_PSD;
  }

  @Override
  protected List<ProcessingCase> expectedApplicableCases() {
    return List.of(ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED);
  }
}
