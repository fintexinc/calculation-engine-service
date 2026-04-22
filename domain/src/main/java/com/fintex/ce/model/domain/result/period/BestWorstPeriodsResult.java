package com.fintex.ce.model.domain.result.period;

import com.fintex.ce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response for best-worst-periods metric. Contains best and worst performance periods analysis.")
public class BestWorstPeriodsResult extends BaseCalculationResult {

  @Schema(description = "Performance end date")
  private LocalDate performanceEndDate;
  @Schema(description = "Performance start date")
  private LocalDate performanceStartDate;
  @Schema(description = "Best and worst performance periods analysis")
  private BestWorstPeriodData bestWorstPeriods;
}
