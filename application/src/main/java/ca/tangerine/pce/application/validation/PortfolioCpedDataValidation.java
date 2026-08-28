package ca.tangerine.pce.application.validation;

import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static ca.tangerine.pce.application.returns.ProcessingCase.PORTFOLIO_PRE_PSD_TRIM;
import static ca.tangerine.pce.application.returns.ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPED_ONLY;
import static ca.tangerine.pce.application.returns.ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED;
import static ca.tangerine.pce.model.error.ErrorCode.CPED_AFTER_PORTFOLIO_PED;
import static ca.tangerine.pce.model.error.ErrorCode.CPED_BEFORE_PORTFOLIO_PSD;

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
