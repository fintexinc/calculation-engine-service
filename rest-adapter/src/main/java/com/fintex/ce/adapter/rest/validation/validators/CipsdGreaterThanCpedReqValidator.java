package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(210)
public class CipsdGreaterThanCpedReqValidator extends AbstractDateNotAfterReqValidator {

  public CipsdGreaterThanCpedReqValidator() {
    super(
        cmd -> cmd instanceof PeriodCommand pc ? pc.getCustomIntervalPsd() : null,
        cmd -> cmd instanceof PeriodCommand pc ? pc.getCustomPed() : null,
        ErrorCode.CIPSD_AFTER_CPED);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.CIPSD_SUPPORTED_METRICS;
  }
}
