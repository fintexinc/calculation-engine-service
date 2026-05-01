package com.fintex.ce.model.domain.result.allocation;

import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocationType;
import com.fintex.ce.model.domain.result.BaseCalculationResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
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
@Schema(description = "Response for classification-allocation metric. Contains classification-based allocation breakdown.")
public class ClassificationAllocationResult extends BaseCalculationResult {

  @Schema(description = "Allocation percentages by classification type")
  private Map<ClassificationAllocationType, BigDecimal> classificationAllocation;
}
