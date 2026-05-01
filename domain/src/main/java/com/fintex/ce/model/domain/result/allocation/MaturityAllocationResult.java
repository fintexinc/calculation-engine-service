package com.fintex.ce.model.domain.result.allocation;

import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocationType;
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
@Schema(description = "Response for maturity-allocation metric. Contains bond maturity allocation breakdown.")
public class MaturityAllocationResult extends BaseCalculationResult {

  @Schema(description = "Bond maturity allocation percentages")
  private Map<MaturityAllocationType, BigDecimal> maturityAllocation;
}
