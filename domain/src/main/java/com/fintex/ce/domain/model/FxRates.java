package com.fintex.ce.domain.model;

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
public class FxRates implements ProviderAware {

  private Map<LocalDate, FxRate> fxRates;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public FxRates(Map<LocalDate, FxRate> fxRates) {
    this.fxRates = fxRates;
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FxRate {
    private BigDecimal usdCad;
    private BigDecimal cadUsd;
  }

}
