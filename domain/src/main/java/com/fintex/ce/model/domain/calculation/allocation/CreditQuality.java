package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.rating.CreditQualityRatingType;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CreditQuality extends BaseCalculationData<CreditQuality> {

  private FinancialInstrumentType holdingType;
  private Map<CreditQualityRatingType, BigDecimal> ratings;

}
