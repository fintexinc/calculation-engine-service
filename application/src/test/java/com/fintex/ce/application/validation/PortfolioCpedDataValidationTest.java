package com.fintex.ce.application.validation;

import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.model.error.ErrorCode;

import java.util.List;

class PortfolioCpedDataValidationTest extends AbstractCpedDataValidationTest {

  private final PortfolioCpedDataValidation validation = new PortfolioCpedDataValidation();

  @Override
  protected CpedDataValidation validator() {
    return validation;
  }

  @Override
  protected ErrorCode expectedAfterPedCode() {
    return ErrorCode.CPED_AFTER_PORTFOLIO_PED;
  }

  @Override
  protected ErrorCode expectedBeforePsdCode() {
    return ErrorCode.CPED_BEFORE_PORTFOLIO_PSD;
  }

  @Override
  protected List<ProcessingCase> expectedApplicableCases() {
    return List.of(
        ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED,
        ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPED_ONLY,
        ProcessingCase.PORTFOLIO_PRE_PSD_TRIM);
  }
}
