package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.dto.command.contract.CustomPsdProvider;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Command for rolling window calculations. Supports metrics: rolling-total-returns, rolling-standard-deviation, rolling-sharpe-ratio")
public class RollingCalculationCommand extends PeriodCommand implements CustomPsdProvider {
  @Schema(description = "Custom performance start date for rolling window")
  @JsonProperty("customPerformanceStartDate")
  private LocalDate customPsd;
  @Schema(description = "Rolling time interval periods in months", example = "[\"12\", \"36\"]")
  @JsonProperty("rollingTimeIntervalPeriod")
  private Set<String> rollingPeriods;
}
