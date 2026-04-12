package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.exception.code.ErrorCode;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Command for distribution of monthly returns histogram. Supports metric: distribution-of-monthly-return")
public class DistributionOfReturnsCommand extends PeriodCommand implements CustomPsdProvider {
  @Schema(description = "Custom performance start date")
  @JsonProperty("customPerformanceStartDate")
  private LocalDate customPsd;
  @Schema(description = "Number of histogram bins for the distribution")
  @JsonProperty("numberOfBins")
  @Min(value = 5, message = ErrorCode.Names.ERR_RRC_CNOB_001)
  @Max(value = 30, message = ErrorCode.Names.ERR_RRC_CNOB_002)
  private Integer customNumberOfBins;
}
