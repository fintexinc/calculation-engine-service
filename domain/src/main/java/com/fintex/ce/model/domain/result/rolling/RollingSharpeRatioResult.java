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
@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for rolling-sharpe-ratio metric. Contains rolling Sharpe ratio over moving time windows.")
public class RollingSharpeRatioResult extends PeriodResult {

  @Schema(description = "Rolling Sharpe ratio per time window")
  private Set<RollingIntervalResult> rollingSharpeRatio;
}
