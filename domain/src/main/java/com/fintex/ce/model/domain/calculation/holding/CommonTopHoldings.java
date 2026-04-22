package com.fintex.ce.model.domain.calculation.holding;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.util.List;
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
public class CommonTopHoldings extends BaseCalculationData<CommonTopHoldings> {

  private List<CommonTopHolding> holdings;

  // For compatibility with R* serialization format
  private String holdingsJson;

  public CommonTopHoldings(String holdingsJson) {
    this.holdingsJson = holdingsJson;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CommonTopHolding {
    private String name;
    private String companyName;
    private String type;
    private BigDecimal value;
    private List<CommonTopHolding> underlyingHoldings;
    private List<SecurityIdentifier> identifiers;
    private BigDecimal weight;
  }

}
