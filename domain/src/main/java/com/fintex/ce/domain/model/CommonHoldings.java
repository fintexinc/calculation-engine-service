package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.core.ProviderAware;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CommonHoldings implements ProviderAware {

  private List<CommonHolding> holdings;

  // For compatibility with R* serialization format
  private String holdingsJson;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public CommonHoldings(String holdingsJson) {
    this.holdingsJson = holdingsJson;
  }

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CommonHolding {
    private String name;
    private String companyName;
    private String type;
    private BigDecimal value;
    private List<CommonHolding> underlyingHoldings;
    private String ticker;
    private String exchangeCode;
    private BigDecimal weight;
    private UUID uuid;
  }

}
