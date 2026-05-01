package com.fintex.ce.model.domain.calculation.holding;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CommonTopHoldings extends BaseCalculationData {

  private List<CommonTopHolding> holdings;

  @Data
  @Builder
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
