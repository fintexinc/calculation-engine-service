package com.fintex.ce.model.domain.calculation.distribution;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
