package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Batch calculation request: one shared portfolio context with a list of metrics to compute. "
    + "Security Master data needed by multiple metrics is fetched only once per distinct SM dataset. "
    + "One failing metric does not suppress others. "
    + "common-performance-dates is not supported in batch mode.")
public class BatchCalculationCommand {

  @NotEmpty(message = ErrorCode.Codes.FIELD_NOT_EMPTY)
  @Schema(description = "Metrics to compute for this portfolio", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<CalculationMetric> metrics;

  // PortfolioCommand / PortfolioHoldingsCommand / AverageMerCommand fields
  @Schema(description = "Portfolio holdings — required by all period, return, and breakdown metrics")
  private List<PortfolioHolding> holdings;

  @Schema(description = "Benchmark holdings — required by benchmark-relative metrics "
      + "(excess-returns, alpha, beta, upside-capture, etc.)")
  private List<PortfolioHolding> benchmarkHoldings;

  @Schema(description = "Portfolio target currency — required by period and return metrics", example = "CAD")
  private Currency currency;

  @Schema(description = "Data providers for Security Master lookups; defaults to MORNINGSTAR if omitted", example = "[\"MORNINGSTAR\"]")
  private List<DataProvider> dataProviders;

  // PeriodCommand / RollingCalculationCommand field
  @JsonProperty("timeIntervalPeriods")
  @Schema(description = "Time interval periods in months for period-based metrics", example = "[\"1\",\"3\",\"12\",\"36\",\"60\"]")
  private Set<String> periods;

  // PeriodCommand field
  @JsonProperty("customIntervalPerformanceStartDate")
  @Schema(description = "Custom interval performance start date (CIPSD) for CIPSD-supporting metrics")
  private LocalDate customIntervalPsd;

  // PeriodCommand / ReturnCommand field
  @JsonProperty("customPerformanceEndDate")
  @Schema(description = "Custom performance end date (CPED)")
  private LocalDate customPed;

  // LeadingTotalReturnCommand / RollingCalculationCommand / ReturnCommand / DistributionOfReturnsCommand field
  @JsonProperty("customPerformanceStartDate")
  @Schema(description = "Custom performance start date (CPSD) — for rolling, leading, return, and distribution metrics")
  private LocalDate customPsd;

  // RollingCalculationCommand field
  @JsonProperty("rollingTimeIntervalPeriod")
  @Schema(description = "Rolling time interval periods in months for rolling metrics", example = "[\"12\",\"36\",\"60\"]")
  private Set<String> rollingPeriods;

  // AverageMerCommand field
  @Schema(description = "Fee aggregation modes for mer / management-fee / fees metrics", example = "[\"scaled\",\"absolute\"]")
  private List<FeeAggregationMode> parameterTypes;

  // IncomeForecastCommand / YieldCommand field — named differently from the Set<String>
  // timeIntervalPeriods used by period metrics to avoid a JSON type conflict.
  @JsonProperty("forecastTimeIntervalPeriods")
  @Schema(description = "Forecast / yield time period in months for income-forecast and yield metrics "
      + "(separate from the array-valued timeIntervalPeriods used by period-based metrics)", example = "12")
  private Integer forecastTimeIntervalPeriods;

  // BestWorstPeriodsCommand field
  @Schema(description = "Best / worst analysis time interval periods in months", example = "[12,36]")
  private Set<Long> bestWorstTimeIntervalPeriods;

  // DistributionOfReturnsCommand field
  @JsonProperty("numberOfBins")
  @Schema(description = "Number of histogram bins for distribution-of-monthly-returns (5–30)")
  private Integer customNumberOfBins;

  // TopCommonHoldingsCommand fields
  @Schema(description = "Maximum number of top common holdings to return")
  private Integer numOfTopCommonHoldings;

  @Schema(description = "Holding types to accumulate for top-common-holdings")
  private Set<String> accumulateHoldingTypes;
}
