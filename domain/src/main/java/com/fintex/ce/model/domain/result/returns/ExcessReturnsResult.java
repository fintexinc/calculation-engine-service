package com.fintex.ce.model.domain.result.returns;

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
@Schema(description = "Response for excess-returns metric. Contains excess returns relative to benchmark per time interval period.")
public class ExcessReturnsResult extends PeriodResult {

  @Schema(description = "Excess returns vs benchmark per time interval period")
  private Set<TimeIntervalResult> excessReturns;
}
