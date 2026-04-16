package com.fintex.ce.model.domain.result.allocation;

import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocationType;
import com.fintex.ce.model.domain.result.WarningResult;

import java.math.BigDecimal;
import java.util.Map;
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
public class MaturityAllocationResult extends WarningResult {

  private Map<MaturityAllocationType, BigDecimal> maturityAllocation;
}
