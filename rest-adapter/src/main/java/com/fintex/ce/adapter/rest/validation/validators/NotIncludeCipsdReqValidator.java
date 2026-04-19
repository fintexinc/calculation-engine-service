package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Order(220)
public class NotIncludeCipsdReqValidator
    extends
      AbstractNotIncludePropertyReqValidator<PeriodCommand, LocalDate> {

  public NotIncludeCipsdReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getCustomIntervalPsd, ErrorCode.REQUEST_CONTAINS_CUSTOM_INTERVAL_PSD);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.ROLLING_AND_LEADING_METRICS;
  }
}
