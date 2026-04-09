package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.calculation.ClassificationAllocationType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class ClassificationAllocation extends BaseCalculationData<ClassificationAllocation> {

  private FinancialInstrumentType holdingType;
  private Map<ClassificationAllocationType, BigDecimal> securityClassificationValues;

}
