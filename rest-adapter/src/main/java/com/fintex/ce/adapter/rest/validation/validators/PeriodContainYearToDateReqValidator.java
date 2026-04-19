package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.Period;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Component
@Order(311)
public class PeriodContainYearToDateReqValidator implements RequestValidator {

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.TWELVE_MONTH_MINIMUM_METRICS;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!(command instanceof PeriodCommand pc)) return;
    Set<String> periods = pc.getPeriods();
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }
    for (final String period : periods) {
      if (Period.YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
        throw ErrorCode.TIME_INTERVAL_PERIOD_CONTAINS_YEAR_TO_DATE.toValidationException();
      }
    }
  }
}
