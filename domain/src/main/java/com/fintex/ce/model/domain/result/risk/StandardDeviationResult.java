package com.fintex.ce.model.domain.result.risk;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.TimeIntervalResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for standard-deviation metric. Contains annualized standard deviation per time interval period.")
public class StandardDeviationResult extends PeriodResult {

  @Schema(description = "Annualized standard deviation per time interval period")
  private Set<TimeIntervalResult> standardDeviation;
}
