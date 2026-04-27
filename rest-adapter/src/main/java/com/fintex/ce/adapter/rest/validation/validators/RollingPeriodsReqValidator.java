package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Component
@Order(330)
public class RollingPeriodsReqValidator implements RequestValidator {

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.ROLLING_METRICS;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!(command instanceof RollingCalculationCommand rc)) return;
    Set<String> periods = rc.getRollingPeriods();
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }
    for (final var period : periods) {
      if (!isNumeric(period) && !CalculationUtils.isNegativeNumeric(period)) {
        throw ErrorCode.TIME_INTERVAL_PERIOD_NOT_ALLOWED.toValidationException(period);
      }
      if (Long.parseLong(period) <= 0) {
        throw ErrorCode.ROLLING_TIME_INTERVAL_NOT_POSITIVE.toValidationException();
      }
    }
  }
}
