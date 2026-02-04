package com.fintex.ce.domain.model.calculation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DistributionData {
  private BigDecimal marketValue;
  private BigDecimal interest = BigDecimal.ZERO;
  private BigDecimal canadianDividend = BigDecimal.ZERO;
  private BigDecimal foreignDividend = BigDecimal.ZERO;
  private BigDecimal capitalGains = BigDecimal.ZERO;
  private BigDecimal returnOfCapital = BigDecimal.ZERO;
  private BigDecimal totalDistribution;

  @JsonIgnore
  private BigDecimal fundValue;
  @JsonIgnore
  private BigDecimal fundDistribution;

}
