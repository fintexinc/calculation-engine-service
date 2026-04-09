package com.fintex.ce.domain.model;

import com.fintex.sm.model.domain.enumeration.EquityMarketCapitalizationType;
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
public class HoldingEquityMarketCap extends BaseCalculationData<HoldingEquityMarketCap> {

  private FinancialInstrumentType holdingType;
  private Map<EquityMarketCapitalizationType, BigDecimal> ratings;

  public HoldingEquityMarketCap(Map<EquityMarketCapitalizationType, BigDecimal> ratings) {
    this.ratings = ratings;
  }

}
