package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.Period;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;

@Component
@Order(320)
public class PeriodsNotContainingYearToDateReqValidator extends AbstractPeriodsNotContainingReqValidator {

  public PeriodsNotContainingYearToDateReqValidator() {
    super(Period.YEAR_TO_DATE, ErrorCode.TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    // Rolling and leading metrics only — non-rolling metrics that need the same check (incl. MAR_RATIO)
    // are already covered by PeriodContainYearToDateReqValidator via TWELVE_MONTH_MINIMUM_METRICS.
    return List.of(
        ROLLING_TOTAL_RETURNS, ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO,
        ROLLING_CORRELATION, LEADING_TOTAL_RETURNS);
  }
}
