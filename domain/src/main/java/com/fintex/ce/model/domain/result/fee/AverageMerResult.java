package com.fintex.ce.model.domain.result.fee;

import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Schema(description = "Response for mer metric. Contains weighted average management expense ratio (MER) by parameter type.")
public class AverageMerResult extends BaseCalculationResult {

  @Schema(description = "Management expense ratio by parameter type (scaled/absolute)")
  @Builder.Default
  private Map<ParameterType, BigDecimal> managementExpenseRatio = new HashMap<>();
}
