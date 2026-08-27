package ca.tangerine.pce.model.domain.result;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for common-performance-dates metric. Contains common performance start and end dates across portfolios.")
public class CommonPerformanceDatesResult extends BaseCalculationResult {

  @Schema(description = "Common performance start date across portfolios")
  private LocalDate commonPerformanceStartDatePf;
  @Schema(description = "Common performance end date across portfolios")
  private LocalDate commonPerformanceEndDatePf;
  @Schema(description = "Common performance start date across benchmarks")
  private LocalDate commonPerformanceStartDateBm;
  @Schema(description = "Common performance end date across benchmarks")
  private LocalDate commonPerformanceEndDateBm;
}
