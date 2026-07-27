package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.SupportedPeriods;
import com.fintex.ce.model.dto.command.PeriodCommand;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(310)
public class StandardDeviationPeriodsReqValidator extends AbstractSupportedPeriodsReqValidator<PeriodCommand> {

  public StandardDeviationPeriodsReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getPeriods, SupportedPeriods.FIXED_LENGTH);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(CalculationMetric.STANDARD_DEVIATION);
  }
}
