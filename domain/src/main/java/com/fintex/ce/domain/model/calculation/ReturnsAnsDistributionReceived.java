package com.fintex.ce.domain.model.calculation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnsAnsDistributionReceived {
  private TreeMap<LocalDate, BigDecimal> returns = new TreeMap<>();
  private BigDecimal distributionReceived;
  private BigDecimal totalContribution;
  private BigDecimal totalWithdrawal;
  private BigDecimal subsequentContribution;
}
