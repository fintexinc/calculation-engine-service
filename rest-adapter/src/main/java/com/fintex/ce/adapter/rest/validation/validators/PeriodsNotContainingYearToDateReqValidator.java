package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.enumeration.Period;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(320)
public class PeriodsNotContainingYearToDateReqValidator extends AbstractPeriodsNotContainingReqValidator {

  public PeriodsNotContainingYearToDateReqValidator() {
    super(Period.YEAR_TO_DATE, ErrorCode.ERR_RRC_TIP_002);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        MAR_RATIO, ROLLING_TOTAL_RETURNS, ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO,
        ROLLING_CORRELATION, LEADING_TOTAL_RETURNS);
  }
}
