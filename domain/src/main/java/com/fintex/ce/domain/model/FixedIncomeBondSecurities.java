package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.calculation.FixedIncomeSectorType;
import com.fintex.ce.domain.model.core.ProviderAware;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FixedIncomeBondSecurities implements ProviderAware {

  private FinancialInstrumentType holdingType;
  private Map<FixedIncomeSectorType, BigDecimal> fixedIncomeBondSectors;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public FixedIncomeBondSecurities(FinancialInstrumentType holdingType, Map<FixedIncomeSectorType, BigDecimal> fixedIncomeBondSectors) {
    this.holdingType = holdingType;
    this.fixedIncomeBondSectors = fixedIncomeBondSectors;
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

}
