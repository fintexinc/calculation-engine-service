package com.fintex.ce.domain.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Command for best and worst performance periods analysis. Supports metric: best-worst-periods")
public class BestWorstPeriodsCommand extends ReturnCommand {
  @Schema(description = "Time interval periods in months for best/worst analysis", example = "[12, 36]")
  private Set<Long> bestWorstTimeIntervalPeriods;
}
