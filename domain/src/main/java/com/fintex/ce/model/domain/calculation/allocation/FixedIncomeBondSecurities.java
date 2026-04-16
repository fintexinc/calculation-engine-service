package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

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
public class FixedIncomeBondSecurities extends BaseCalculationData<FixedIncomeBondSecurities> {

  private FinancialInstrumentType holdingType;
  private Map<FixedIncomeSecuritiesAllocationType, BigDecimal> fixedIncomeBondSectors;

}
