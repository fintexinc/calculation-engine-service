package ca.tangerine.pce.model.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import ca.tangerine.pce.model.dto.command.AverageMerCommand;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.MerComparisonCommand;
import ca.tangerine.pce.model.dto.command.MultiplePortfoliosCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.dto.command.ReturnCommand;
import ca.tangerine.pce.model.dto.command.TopCommonHoldingsCommand;
import ca.tangerine.pce.model.error.ErrorCode;

@Schema(description = "Available portfolio calculation metrics")
@Getter
@RequiredArgsConstructor
public enum CalculationMetric {

  @Schema(description = "Trailing total return over specified periods")
  TRAILING_TOTAL_RETURNS("trailing-total-returns", PeriodCommand.class),
  @Schema(description = "Calendar-year annual returns")
  ANNUAL_RETURNS("annual-returns", ReturnCommand.class),

  @Schema(description = "Growth of a hypothetical $10,000 investment over time")
  GROWTH_OF_10K("growth-of-10k", ReturnCommand.class),
  @Schema(description = "Annualized standard deviation of returns")
  STANDARD_DEVIATION("standard-deviation", PeriodCommand.class),
  @Schema(description = "Sharpe ratio — risk-adjusted return relative to risk-free rate")
  SHARPE_RATIO("sharpe-ratio", PeriodCommand.class),
  @Schema(description = "Maximum peak-to-trough drawdown")
  MAX_DRAWDOWN("max-drawdown", PeriodCommand.class),
  @Schema(description = "Portfolio asset allocation breakdown by region")
  ASSET_ALLOCATIONS("asset-allocations", PortfolioHoldingsCommand.class),

  @Schema(description = "Portfolio asset allocation breakdown by emerging markets region")
  ASSET_ALLOCATIONS_EM("asset-allocations-em", PortfolioHoldingsCommand.class),

  @Schema(description = "Equity sector allocation breakdown")
  EQUITY_SECTOR("equity-sector", PortfolioHoldingsCommand.class),

  @Schema(description = "Consolidated sector breakdown across the whole portfolio, equity and fixed income together")
  SECTOR_EXPOSURE("sector-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Equity country exposure breakdown")
  EQUITY_COUNTRY_EXPOSURE("equity-country-exposure", PortfolioHoldingsCommand.class),
  @Schema(description = "Equity geographic exposure breakdown by region")
  EQUITY_GEOGRAPHIC_EXPOSURE("equity-geographic-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Consolidated geographic exposure breakdown by region across the whole portfolio")
  GEOGRAPHIC_EXPOSURE("geographic-exposure", PortfolioHoldingsCommand.class),
  @Schema(description = "Fixed income country exposure breakdown")
  FIXED_INCOME_COUNTRY_EXPOSURE("fixed-income-country-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Fixed income geographic exposure breakdown by region")
  FIXED_INCOME_GEOGRAPHIC_EXPOSURE("fixed-income-geographic-exposure", PortfolioHoldingsCommand.class),

  @Schema(description = "Fixed income bond sector allocation breakdown")
  FIXED_INCOME_BOND_SECTOR("fixed-income-bond-sector", PortfolioHoldingsCommand.class),
  @Schema(description = "Management expense ratio (MER) — weighted average across holdings")
  MER("mer", AverageMerCommand.class),

  @Schema(description = "Management fee — weighted average management fee across holdings")
  MANAGEMENT_FEE("management-fee", AverageMerCommand.class),

  @Schema(description = "Annual and monthly fee dollar amounts for the portfolio (Σ value × MER and that ÷ 12)")
  FEES("fees", AverageMerCommand.class),

  @Schema(description = "MER comparison — portfolio MER vs the benchmark's MER, per view")
  MER_BENCHMARK_COMPARISON("mer-benchmark-comparison", MerComparisonCommand.class),
  @Schema(description = "Common performance start dates across multiple portfolios")
  COMMON_PERFORMANCE_DATES("common-performance-dates", MultiplePortfoliosCommand.class),

  @Schema(description = "Top common holdings shared across portfolio holdings")
  TOP_COMMON_HOLDINGS("top-common-holdings", TopCommonHoldingsCommand.class),

  @Schema(description = "Count of unique underlying holdings across all portfolio securities")
  NUMBER_OF_UNIQUE_HOLDINGS("number-of-unique-holdings", PortfolioHoldingsCommand.class);

  @JsonValue
  private final String value;
  private final Class<? extends CalculationCommand> commandType;

  /** Every metric that requires a benchmark side in the request. */
  public static final List<CalculationMetric> BENCHMARK_METRICS = List.of(MER_BENCHMARK_COMPARISON);

  /**
   * Fee metrics that report projected dollar amounts, and therefore honour a requested horizon set. {@code MER} is
   * deliberately absent — it reports rates, which carry no horizon.
   */
  public static final List<CalculationMetric> FEE_PROJECTION_METRICS = List.of(
      FEES, MER_BENCHMARK_COMPARISON);

  public static final List<CalculationMetric> TWELVE_MONTH_MINIMUM_METRICS = List.of(SHARPE_RATIO);

  public static final List<CalculationMetric> CIPSD_SUPPORTED_METRICS = List.of(
      TRAILING_TOTAL_RETURNS, STANDARD_DEVIATION, SHARPE_RATIO, MAX_DRAWDOWN);

  public static final List<CalculationMetric> PERIOD_METRICS = List.of(
      TRAILING_TOTAL_RETURNS, STANDARD_DEVIATION, SHARPE_RATIO, MAX_DRAWDOWN);

  private static final Map<String, CalculationMetric> VALUE_MAP = Arrays.stream(values())
      .collect(Collectors.toMap(CalculationMetric::getValue, Function.identity()));

  @JsonCreator
  public static CalculationMetric from(String value) {
    CalculationMetric metric = VALUE_MAP.get(value);
    if (metric == null) {
      throw ErrorCode.UNSUPPORTED_METRIC.toException(value);
    }
    return metric;
  }

  /**
   * Human-readable label derived from the kebab-case {@link #value}, e.g. {@code equity-sector} -> {@code Equity
   * Sector}. Used wherever a metric needs to be named in user-facing messages so we do not scatter string literals.
   */
  public String getUserFriendlyName() {
    return Arrays.stream(value.split("-"))
        .filter(word -> !word.isEmpty())
        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
        .collect(Collectors.joining(" "));
  }
}
