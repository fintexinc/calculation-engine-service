package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.adapter.rest.validation.RequestValidator;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.enumeration.Period;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

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
        throw ErrorCode.ERR_RRC_TIP_002.reqValidationError();
      }
    }
  }
}
