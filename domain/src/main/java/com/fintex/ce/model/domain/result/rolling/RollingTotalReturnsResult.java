package com.fintex.ce.model.domain.result.rolling;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.RollingIntervalResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
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
@Schema(description = "Response for rolling-total-returns metric. Contains rolling total returns over moving time windows.")
public class RollingTotalReturnsResult extends PeriodResult {

  @Schema(description = "Rolling total returns per time window")
  private Set<RollingIntervalResult> rollingTotalReturns;
}
