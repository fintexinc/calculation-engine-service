package com.fintex.ce.adapter.rest.dto.period;

import com.fintex.ce.adapter.rest.dto.WarningDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for best-worst-periods metric. Contains best and worst performance periods analysis.")
public class BestWorstPeriodsResponseDTO extends WarningDTO {

  @Schema(description = "Performance end date")
  @JsonProperty("performanceEndDate")
  protected LocalDate ped;
  @Schema(description = "Performance start date")
  @JsonProperty("performanceStartDate")
  protected LocalDate psd;

  @Schema(description = "Best and worst performance periods analysis")
  private BestWorstPeriodDTO bestWorstPeriods;

}
