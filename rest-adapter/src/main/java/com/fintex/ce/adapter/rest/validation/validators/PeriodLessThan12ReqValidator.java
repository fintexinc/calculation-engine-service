package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(310)
public class PeriodLessThan12ReqValidator extends AbstractPeriodsLessThan12ReqValidator<PeriodCommand> {

  public PeriodLessThan12ReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getPeriods, ErrorCode.ERR_RRC_TIP_001);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.TWELVE_MONTH_MINIMUM_METRICS;
  }
}
