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
public class EquitySectorStock implements ProviderAware {

  private String sectorName;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public EquitySectorStock(String sectorName) {
    this.sectorName = sectorName;
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

}
