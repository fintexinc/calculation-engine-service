package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.contract.CustomPedProvider;
import com.fintex.ce.model.dto.command.contract.CustomPsdProvider;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;

@Component
@Order(211)
public class CpsdGreaterThanCpedReqValidator extends AbstractDateNotAfterReqValidator {

  public CpsdGreaterThanCpedReqValidator() {
    super(
        cmd -> cmd instanceof CustomPsdProvider p ? p.getCustomPsd() : null,
        cmd -> cmd instanceof CustomPedProvider p ? p.getCustomPed() : null,
        ErrorCode.CPSD_AFTER_CPED);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(ANNUAL_RETURNS, GROWTH_OF_10K);
  }
}
