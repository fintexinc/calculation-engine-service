package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.CustomPedProvider;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(201)
public class CpedLastDayOfMonthReqValidator extends AbstractLastDayOfMonthReqValidator<CustomPedProvider> {

  public CpedLastDayOfMonthReqValidator() {
    super(CustomPedProvider.class, CustomPedProvider::getCustomPed, ErrorCode.ERR_RRC_CPED_001);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        TRAILING_TOTAL_RETURNS, STANDARD_DEVIATION, MEAN, SHARPE_RATIO, SORTINO_RATIO,
        DOWNSIDE_DEVIATION, EXCESS_RETURNS, TREYNOR_RATIO, INFORMATION_RATIO, TRACKING_ERROR,
        ALPHA, BETA, R_SQUARED, UPSIDE_CAPTURE, DOWNSIDE_CAPTURE, MAX_DRAWDOWN, MAR_RATIO,
        CORRELATION, ROLLING_TOTAL_RETURNS, ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO,
        ROLLING_CORRELATION, ANNUAL_RETURNS, GROWTH_OF_10K, BEST_WORST_PERIODS,
        DISTRIBUTION_OF_MONTHLY_RETURNS);
  }
}
