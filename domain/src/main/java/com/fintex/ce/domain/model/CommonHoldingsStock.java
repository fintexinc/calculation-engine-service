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
public class CommonHoldingsStock implements ProviderAware {

  private String companyName;
  private String ticker;
  private String exchangeCode;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public CommonHoldingsStock(String companyName, String ticker, String exchangeCode) {
    this.companyName = companyName;
    this.ticker = ticker;
    this.exchangeCode = exchangeCode;
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

}
