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

import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNumeric;

@Component
@Order(300)
public class PeriodReqValidator implements RequestValidator {

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.PERIOD_METRICS;
  }

  @Override
  public void validate(CalculationCommand command) {
    if (!(command instanceof PeriodCommand pc)) return;
    Set<String> periods = pc.getPeriods();
    if (CollectionUtils.isEmpty(periods)) {
      return;
    }

    final Set<String> allowedSymbols = Stream.of(Period.values()).map(Enum::name).collect(Collectors.toSet());
    for (String period : periods) {
      if (NumberUtils.isNumber(period) && Integer.parseInt(period) <= 0) {
        throw ErrorCode.ERR_RRC_TIP_003.reqValidationError();
      }
      if (!isNumeric(period) && !allowedSymbols.contains(period)) {
        throw ErrorCode.ERR_RRC_TIP_004.reqValidationError(period);
      }
    }
  }
}
