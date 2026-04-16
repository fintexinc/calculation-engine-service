package com.fintex.ce.model.domain.calculation.holding;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class CommonHoldings extends BaseCalculationData<CommonHoldings> {

  private List<CommonHolding> holdings;

  // For compatibility with R* serialization format
  private String holdingsJson;

  public CommonHoldings(String holdingsJson) {
    this.holdingsJson = holdingsJson;
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
