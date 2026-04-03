package com.fintex.ce.domain.model.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.dto.command.CorrelationCommand;
import com.fintex.ce.domain.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.domain.dto.command.IncomeForecastCommand;
import com.fintex.ce.domain.dto.command.LeadingTotalReturnCommand;
import com.fintex.ce.domain.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.dto.command.ReturnCommand;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.dto.command.RollingCorrelationCommand;
import com.fintex.ce.domain.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.domain.dto.command.YieldCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "Available portfolio calculation metrics")
@Getter
@RequiredArgsConstructor
public enum CalculationMetric {

  @Schema(description = "Trailing total return over specified periods")
  TRAILING_TOTAL_RETURNS("trailing-total-returns", PeriodCommand.class),

  @Schema(description = "Leading (forward-looking) total return over specified periods")
  LEADING_TOTAL_RETURNS("leading-total-returns", LeadingTotalReturnCommand.class),

  @Schema(description = "Rolling total returns over a moving time window")
  ROLLING_TOTAL_RETURNS("rolling-total-returns", RollingCalculationCommand.class),

  @Schema(description = "Excess returns relative to a benchmark")
  EXCESS_RETURNS("excess-returns", PeriodCommand.class),

  @Schema(description = "Calendar-year annual returns")
  ANNUAL_RETURNS("annual-returns", ReturnCommand.class),

  @Schema(description = "Growth of a hypothetical $10,000 investment over time")
  GROWTH_OF_10K("growth-of-10k", ReturnCommand.class),

  @Schema(description = "Best and worst performance periods over specified intervals")
  BEST_WORST_PERIODS("best-worst-periods", BestWorstPeriodsCommand.class),

  @Schema(description = "Distribution (histogram) of monthly returns")
  DISTRIBUTION_OF_MONTHLY_RETURNS("distribution-of-monthly-returns", DistributionOfReturnsCommand.class),

  @Schema(description = "Annualized standard deviation of returns")
  STANDARD_DEVIATION("standard-deviation", PeriodCommand.class),

  @Schema(description = "Rolling standard deviation over a moving time window")
  ROLLING_STANDARD_DEVIATION("rolling-standard-deviation", RollingCalculationCommand.class),

  @Schema(description = "Arithmetic mean of periodic returns")
  MEAN("mean", PeriodCommand.class),

  @Schema(description = "Sharpe ratio — risk-adjusted return relative to risk-free rate")
  SHARPE_RATIO("sharpe-ratio", PeriodCommand.class),

  @Schema(description = "Rolling Sharpe ratio over a moving time window")
  ROLLING_SHARPE_RATIO("rolling-sharpe-ratio", RollingCalculationCommand.class),

  @Schema(description = "Sortino ratio — downside risk-adjusted return")
  SORTINO_RATIO("sortino-ratio", PeriodCommand.class),

  @Schema(description = "Maximum peak-to-trough drawdown")
  MAX_DRAWDOWN("max-drawdown", PeriodCommand.class),

  @Schema(description = "Downside deviation — volatility of negative returns")
  DOWNSIDE_DEVIATION("downside-deviation", PeriodCommand.class),

  @Schema(description = "MAR ratio — annualized return divided by maximum drawdown")
  MAR_RATIO("mar-ratio", PeriodCommand.class),

  @Schema(description = "Treynor ratio — excess return per unit of systematic risk (beta)")
  TREYNOR_RATIO("treynor-ratio", PeriodCommand.class),

  @Schema(description = "Information ratio — active return divided by tracking error")
  INFORMATION_RATIO("information-ratio", PeriodCommand.class),

  @Schema(description = "Tracking error — standard deviation of excess returns vs benchmark")
  TRACKING_ERROR("tracking-error", PeriodCommand.class),

  @Schema(description = "Alpha — excess return attributable to active management")
  ALPHA("alpha", PeriodCommand.class),

  @Schema(description = "Beta — sensitivity of portfolio returns to benchmark returns")
  BETA("beta", PeriodCommand.class),

  @Schema(description = "R-squared — proportion of return variance explained by benchmark")
  R_SQUARED("rsquared", PeriodCommand.class),

  @Schema(description = "Correlation coefficient between portfolio and benchmark returns")
  CORRELATION("correlation", CorrelationCommand.class),

  @Schema(description = "Rolling correlation coefficient over a moving time window")
  ROLLING_CORRELATION("rolling-correlation", RollingCorrelationCommand.class),

  @Schema(description = "Upside capture ratio — performance in rising benchmark periods")
  UPSIDE_CAPTURE("upside-capture", PeriodCommand.class),

  @Schema(description = "Downside capture ratio — performance in falling benchmark periods")
  DOWNSIDE_CAPTURE("downside-capture", PeriodCommand.class),

  @Schema(description = "Portfolio asset allocation breakdown by region")
  ASSET_ALLOCATIONS("asset-allocations", PortfolioHoldingsCommand.class),

  @Schema(description = "Portfolio asset allocation breakdown by emerging markets region")
  ASSET_ALLOCATIONS_EM("asset-allocations-em", PortfolioHoldingsCommand.class),

  @Schema(description = "Equity sector allocation breakdown")
  EQUITY_SECTOR("equity-sector", PortfolioHoldingsCommand.class),

  @Schema(description = "Equity country exposure breakdown")
  EQUITY_COUNTRY_EXPOSURE("equity-country-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Equity style box exposure (value/blend/growth vs large/mid/small)")
  EQUITY_STYLEBOX_EXPOSURE("equity-stylebox-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Equity geographic exposure breakdown by region")
  EQUITY_GEOGRAPHIC_EXPOSURE("equity-geographic-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Equity market capitalization breakdown (large/mid/small cap)")
  EQUITY_MARKET_CAPITALIZATION("equity-market-capitalization", PortfolioHoldingsCommand.class),

  @Schema(description = "Fixed income country exposure breakdown")
  FIXED_INCOME_COUNTRY_EXPOSURE("fixed-income-country-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Fixed income geographic exposure breakdown by region")
  FIXED_INCOME_GEOGRAPHIC_EXPOSURE("fixed-income-geographic-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Fixed income bond sector allocation breakdown")
  FIXED_INCOME_BOND_SECTOR("fixed-income-bond-sector", PortfolioHoldingsCommand.class),

  @Schema(description = "Fixed income style box exposure breakdown")
  FIXED_INCOME_STYLEBOX_EXPOSURE("fixed-income-stylebox-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Bond maturity allocation breakdown")
  MATURITY_ALLOCATION("maturity-allocation", PortfolioHoldingsCommand.class),

  @Schema(description = "Classification-based allocation breakdown")
  CLASSIFICATION_ALLOCATION("classification-allocation", PortfolioHoldingsCommand.class),

  @Schema(description = "Management expense ratio (MER) — weighted average across holdings")
  MER("mer", AverageMerCommand.class),

  @Schema(description = "Management fee — weighted average management fee across holdings")
  MANAGEMENT_FEE("management-fee", AverageMerCommand.class),

  @Schema(description = "Sales charge breakdown by holding")
  SALES_CHARGE("sales-charge", PortfolioHoldingsCommand.class),

  @Schema(description = "Income forecast for specified time period")
  INCOME_FORECAST("income-forecast", IncomeForecastCommand.class),

  @Schema(description = "Yield calculation for portfolio holdings")
  YIELD("yield", YieldCommand.class),

  @Schema(description = "Common performance start dates across multiple portfolios")
  COMMON_PERFORMANCE_DATES("common-performance-dates", MultiplePortfoliosCommand.class),

  @Schema(description = "Top common holdings shared across portfolio holdings")
  TOP_COMMON_HOLDINGS("top-common-holdings", TopCommonHoldingsCommand.class),

  @Schema(description = "Fixed income credit quality breakdown")
  FIXED_INCOME_CREDIT_QUALITY("fixed-income-credit-quality", PortfolioHoldingsCommand.class);

  @JsonValue
  private final String value;
  private final Class<? extends CalculationCommand> commandType;

  private static final Map<String, CalculationMetric> VALUE_MAP = Arrays.stream(values())
      .collect(Collectors.toMap(CalculationMetric::getValue, Function.identity()));

  @JsonCreator
  public static CalculationMetric from(String value) {
    CalculationMetric metric = VALUE_MAP.get(value);
    if (metric == null) {
      throw new IllegalArgumentException("Unknown calculation metric: " + value);
    }
    return metric;
  }
}
