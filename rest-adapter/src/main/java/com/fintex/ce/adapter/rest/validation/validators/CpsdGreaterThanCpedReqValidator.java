package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.CustomPedProvider;
import com.fintex.ce.domain.dto.command.CustomPsdProvider;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(211)
public class CpsdGreaterThanCpedReqValidator extends AbstractDateNotAfterReqValidator {

  public CpsdGreaterThanCpedReqValidator() {
    super(
        cmd -> cmd instanceof CustomPsdProvider p ? p.getCustomPsd() : null,
        cmd -> cmd instanceof CustomPedProvider p ? p.getCustomPed() : null,
        ErrorCode.ERR_RRC_CPSD_004);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        ROLLING_TOTAL_RETURNS, ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO,
        ROLLING_CORRELATION, ANNUAL_RETURNS, GROWTH_OF_10K, BEST_WORST_PERIODS,
        DISTRIBUTION_OF_MONTHLY_RETURNS);
  }
}
