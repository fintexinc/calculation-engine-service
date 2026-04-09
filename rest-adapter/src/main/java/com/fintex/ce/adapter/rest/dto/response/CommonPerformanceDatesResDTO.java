package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for common-performance-dates metric. Contains common performance start and end dates across portfolios.")
public class CommonPerformanceDatesResDTO extends ErrorDTO {

  @Schema(description = "Common performance start date across portfolios")
  private LocalDate commonPerformanceStartDatePf;
  @Schema(description = "Common performance end date across portfolios")
  private LocalDate commonPerformanceEndDatePf;
  @Schema(description = "Common performance start date across benchmarks")
  private LocalDate commonPerformanceStartDateBm;
  @Schema(description = "Common performance end date across benchmarks")
  private LocalDate commonPerformanceEndDateBm;

}
