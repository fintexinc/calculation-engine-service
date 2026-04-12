package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

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
        ErrorCode.ERR_RRC_CIPSD_002);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return CalculationMetric.CIPSD_SUPPORTED_METRICS;
  }
}
