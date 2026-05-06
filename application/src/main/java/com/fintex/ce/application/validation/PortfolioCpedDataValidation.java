package com.fintex.ce.application.validation;

import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.fintex.ce.application.returns.ProcessingCase.PORTFOLIO_PRE_PSD_TRIM;
import static com.fintex.ce.application.returns.ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPED_ONLY;
import static com.fintex.ce.application.returns.ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED;
import static com.fintex.ce.model.error.ErrorCode.CPED_AFTER_PORTFOLIO_PED;
import static com.fintex.ce.model.error.ErrorCode.CPED_BEFORE_PORTFOLIO_PSD;

@Component
@Order(100)
public class PortfolioCpedDataValidation extends CpedDataValidation {

  @Override
  protected ErrorCode getCpedIsAfterPedExceptionCode() {
    return CPED_AFTER_PORTFOLIO_PED;
  }

  @Override
  protected ErrorCode getCpedIsBeforePsdExceptionCode() {
    return CPED_BEFORE_PORTFOLIO_PSD;
  }

  @Override
  public boolean isApplicable(ProcessingCase processingCase) {
    return processingCase == PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED
        || processingCase == PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPED_ONLY
        || processingCase == PORTFOLIO_PRE_PSD_TRIM;
  }
}
