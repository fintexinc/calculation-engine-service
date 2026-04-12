package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.domain.dto.command.HoldingsProvider;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fintex.ce.domain.model.enumeration.CalculationMetric.*;

@Component
@Order(110)
public class HoldingsCouldNotBeEmptyReqValidator extends AbstractHoldingsNotEmptyReqValidator<HoldingsProvider> {

  public HoldingsCouldNotBeEmptyReqValidator() {
    super(HoldingsProvider.class, HoldingsProvider::getHoldings, "Holdings could not be empty");
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        STANDARD_DEVIATION, MEAN, SHARPE_RATIO, SORTINO_RATIO, DOWNSIDE_DEVIATION,
        EXCESS_RETURNS, TREYNOR_RATIO, INFORMATION_RATIO, TRACKING_ERROR, ALPHA, BETA,
        R_SQUARED, UPSIDE_CAPTURE, DOWNSIDE_CAPTURE, MAX_DRAWDOWN, MAR_RATIO, CORRELATION,
        ROLLING_TOTAL_RETURNS, ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO,
        ROLLING_CORRELATION, LEADING_TOTAL_RETURNS, ASSET_ALLOCATIONS, ASSET_ALLOCATIONS_EM,
        EQUITY_SECTOR, EQUITY_COUNTRY_EXPOSURE, EQUITY_STYLEBOX_EXPOSURE,
        EQUITY_GEOGRAPHIC_EXPOSURE, EQUITY_MARKET_CAPITALIZATION, FIXED_INCOME_COUNTRY_EXPOSURE,
        FIXED_INCOME_GEOGRAPHIC_EXPOSURE, FIXED_INCOME_BOND_SECTOR,
        FIXED_INCOME_STYLEBOX_EXPOSURE, MATURITY_ALLOCATION, CLASSIFICATION_ALLOCATION,
        SALES_CHARGE, FIXED_INCOME_CREDIT_QUALITY, YIELD, ANNUAL_RETURNS, GROWTH_OF_10K, MER,
        MANAGEMENT_FEE, BEST_WORST_PERIODS, TOP_COMMON_HOLDINGS,
        DISTRIBUTION_OF_MONTHLY_RETURNS, INCOME_FORECAST);
  }
}
