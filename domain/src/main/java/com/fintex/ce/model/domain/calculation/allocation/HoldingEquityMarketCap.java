package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType;
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
public class HoldingEquityMarketCap extends BaseCalculationData<HoldingEquityMarketCap> {

  private FinancialInstrumentType holdingType;
  private Map<EquityMarketCapitalizationType, BigDecimal> ratings;

  public HoldingEquityMarketCap(Map<EquityMarketCapitalizationType, BigDecimal> ratings) {
    this.ratings = ratings;
  }

}
