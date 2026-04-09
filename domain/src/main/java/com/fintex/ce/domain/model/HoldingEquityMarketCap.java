package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.core.ProviderAware;
import com.fintex.sm.model.domain.enumeration.EquityMarketCapitalizationType;
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
public class HoldingEquityMarketCap implements ProviderAware {

  private FinancialInstrumentType holdingType;
  private Map<EquityMarketCapitalizationType, BigDecimal> ratings;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public HoldingEquityMarketCap(Map<EquityMarketCapitalizationType, BigDecimal> ratings) {
    this.ratings = ratings;
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

}
