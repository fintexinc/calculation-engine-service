package com.fintex.ce.model.domain.calculation.returns;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnsAndDistributionReceived {
  private TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
  private BigDecimal distributionReceived;
  private BigDecimal totalContribution;
  private BigDecimal totalWithdrawal;
  private BigDecimal subsequentContribution;
}
