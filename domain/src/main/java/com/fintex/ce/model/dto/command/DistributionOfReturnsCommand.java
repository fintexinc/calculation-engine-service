package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.error.ErrorCode;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Command for distribution of monthly returns histogram. Supports metric: distribution-of-monthly-return")
public class DistributionOfReturnsCommand extends PeriodCommand implements CustomPsdProvider {
  @Schema(description = "Custom performance start date")
  @JsonProperty("customPerformanceStartDate")
  private LocalDate customPsd;
  @Schema(description = "Number of histogram bins for the distribution")
  @JsonProperty("numberOfBins")
  @Min(value = 5, message = ErrorCode.Codes.CUSTOM_NUMBER_OF_BINS_LESS_THAN_MIN)
  @Max(value = 30, message = ErrorCode.Codes.CUSTOM_NUMBER_OF_BINS_GREATER_THAN_MAX)
  private Integer customNumberOfBins;
}
