package com.fintex.ce.port.input.result;

import com.fintex.ce.domain.enumeration.calculation.SalesCharge;
import com.fintex.ce.port.input.result.ErrorResult;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class SalesChargeResult extends ErrorResult {

  private Map<SalesCharge, SalesChargeEntry> salesCharges = new EnumMap<>(SalesCharge.class);

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SalesChargeEntry {

    private BigDecimal allocation;
    private BigDecimal value;
    private Set<SalesChargeHoldingEntry> holdings;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SalesChargeHoldingEntry {

    private String fundServCode;
    private BigDecimal mutualFundAllocation;
  }
}
