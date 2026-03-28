package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.calculation.CreditQualityRating;
import com.fintex.ce.domain.model.core.ProviderAware;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreditQuality implements ProviderAware {

  private FinancialInstrumentType holdingType;
  private Map<CreditQualityRating, BigDecimal> ratings;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public CreditQuality(FinancialInstrumentType holdingType, Map<CreditQualityRating, BigDecimal> ratings) {
    this.holdingType = holdingType;
    this.ratings = ratings;
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

}
