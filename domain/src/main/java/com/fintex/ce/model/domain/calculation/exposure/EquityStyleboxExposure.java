package com.fintex.ce.model.domain.calculation.exposure;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.rating.StyleBoxType;

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
public class EquityStyleboxExposure extends BaseCalculationData<EquityStyleboxExposure> {

  private FinancialInstrumentType holdingType;
  private Map<StyleBoxType, BigDecimal> boxValues;

}
