package com.fintex.ce.model.domain.result.rolling;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.RollingIntervalResult;

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
@Schema(description = "Response for rolling-standard-deviation metric. Contains rolling standard deviation over moving time windows.")
public class RollingStandardDeviationResult extends PeriodResult {

  @Schema(description = "Rolling standard deviation per time window")
  private Set<RollingIntervalResult> rollingStandardDeviation;
}
