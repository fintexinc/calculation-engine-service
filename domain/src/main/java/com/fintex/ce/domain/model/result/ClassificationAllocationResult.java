package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.calculation.ClassificationAllocationType;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class ClassificationAllocationResult extends WarningResult {

  private Map<ClassificationAllocationType, BigDecimal> classificationAllocation;
}
