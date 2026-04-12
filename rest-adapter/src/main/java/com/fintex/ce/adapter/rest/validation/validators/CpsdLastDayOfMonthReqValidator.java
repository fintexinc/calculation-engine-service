package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.CustomPsdProvider;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(202)
public class CpsdLastDayOfMonthReqValidator extends AbstractLastDayOfMonthReqValidator<CustomPsdProvider> {

  public CpsdLastDayOfMonthReqValidator() {
    super(CustomPsdProvider.class, CustomPsdProvider::getCustomPsd, ErrorCode.ERR_RRC_CPSD_001);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        ROLLING_TOTAL_RETURNS, ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO,
        ROLLING_CORRELATION, ANNUAL_RETURNS, GROWTH_OF_10K, BEST_WORST_PERIODS,
        DISTRIBUTION_OF_MONTHLY_RETURNS, LEADING_TOTAL_RETURNS);
  }
}
