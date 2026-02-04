package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.core.ProviderAware;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class EquityMarketCapitalizationStock implements ProviderAware {

  private String styleBox;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public EquityMarketCapitalizationStock(String styleBox) {
    this.styleBox = styleBox;
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

}
