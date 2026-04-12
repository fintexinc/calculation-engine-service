package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(200)
public class CipsdLastDayOfMonthReqValidator extends AbstractLastDayOfMonthReqValidator<PeriodCommand> {

  public CipsdLastDayOfMonthReqValidator() {
    super(PeriodCommand.class, PeriodCommand::getCustomIntervalPsd, ErrorCode.ERR_RRC_CIPSD_001);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.CIPSD_SUPPORTED_METRICS;
  }
}
