package com.fintex.ce.model.dto.command;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Command for period-based calculations. Supports metrics: trailing-total-return, excess-returns, standard-deviation, mean, sharpe-ratio, sortino-ratio, max-drawdown, downside-deviation, mar-ratio, treynor-ratio, information-ratio, tracking-error, alpha, beta, rsquared, upside-capture, downside-capture")
public class PeriodCommand extends PortfolioCommand implements CustomPedProvider {
  @Schema(description = "Custom interval performance start date")
  @JsonProperty("customIntervalPerformanceStartDate")
  private LocalDate customIntervalPsd;
  @Schema(description = "Custom performance end date")
  @JsonProperty("customPerformanceEndDate")
  private LocalDate customPed;
  @Schema(description = "Time interval periods in months", example = "[\"1\", \"3\", \"12\", \"36\", \"60\"]")
  @JsonProperty("timeIntervalPeriods")
  private Set<String> periods;
}
