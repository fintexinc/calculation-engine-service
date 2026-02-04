package com.fintex.ce.domain.model;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.model.core.ProviderAware;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TreasuryBills implements ProviderAware {

  private Currency currency;
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
