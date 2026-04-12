package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;
import static com.fintex.ce.util.CalculationUtils.isNegativeNumeric;
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
      if (!isNumeric(period) && !isNegativeNumeric(period)) {
        throw ErrorCode.ERR_RRC_TIP_004.reqValidationError(period);
      }
      if (Long.parseLong(period) <= 0) {
        throw ErrorCode.ERR_RRC_RTIP_003.reqValidationError();
      }
    }
  }
}
