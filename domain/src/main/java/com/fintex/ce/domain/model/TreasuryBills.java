package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.core.ProviderAware;
import com.fintex.sm.model.domain.enumeration.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class TreasuryBills implements ProviderAware {

  private CurrencyType currency;
  private Map<LocalDate, BigDecimal> monthlyReturns;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

}
