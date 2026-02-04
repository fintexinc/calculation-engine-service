package com.fintex.ce.domain.model;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.core.ProviderAware;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MonthlyReturns implements ReturnsData, ProviderAware {

  private String currency;
  private HoldingType holdingType;
  private TreeMap<LocalDate, BigDecimal> returns;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

  public void addError(ValidationError error) {
    if (errors == null) {
      errors = new ArrayList<>();
    }
    errors.add(error);
  }

}
