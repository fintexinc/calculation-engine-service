package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;

@Component
@Order(221)
public class NotIncludeCpedReqValidator
    extends
      AbstractNotIncludePropertyReqValidator<PeriodCommand, LocalDate> {

  public NotIncludeCpedReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getCustomPed, ErrorCode.REQUEST_CONTAINS_CUSTOM_PED);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(LEADING_TOTAL_RETURNS);
  }
}
