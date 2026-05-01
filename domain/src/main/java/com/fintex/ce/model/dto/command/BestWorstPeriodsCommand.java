package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.error.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Command for best and worst performance periods analysis. Supports metric: best-worst-periods")
public class BestWorstPeriodsCommand extends ReturnCommand {
  @Schema(description = "Time interval periods in months for best/worst analysis", example = "[12, 36]")
  private Set<@Min(value = 1, message = ErrorCode.Codes.BEST_WORST_TIME_INTERVAL_NOT_POSITIVE) @Max(value = 300, message = ErrorCode.Codes.BEST_WORST_TIME_INTERVAL_TOO_LARGE) Long> bestWorstTimeIntervalPeriods;
}
