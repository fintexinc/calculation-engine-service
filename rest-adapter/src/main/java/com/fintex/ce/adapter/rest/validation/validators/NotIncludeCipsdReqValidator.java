package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(220)
public class NotIncludeCipsdReqValidator
    extends
      AbstractNotIncludePropertyReqValidator<PeriodCommand, LocalDate> {

  public NotIncludeCipsdReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getCustomIntervalPsd, ErrorCode.ERR_RRC_TIP_005);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.ROLLING_AND_LEADING_METRICS;
  }
}
