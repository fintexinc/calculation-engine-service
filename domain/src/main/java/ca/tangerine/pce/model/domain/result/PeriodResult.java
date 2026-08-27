package ca.tangerine.pce.model.domain.result;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Base response for period-based calculations containing performance dates.")
public class PeriodResult extends BaseCalculationResult {

  @Schema(description = "Performance end date")
  protected LocalDate performanceEndDate;
  @Schema(description = "Performance start date")
  protected LocalDate performanceStartDate;
  @Schema(description = "Custom interval performance start date")
  protected LocalDate customIntervalPerformanceStartDate;
}
