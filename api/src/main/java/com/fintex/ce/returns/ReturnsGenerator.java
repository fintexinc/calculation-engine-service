package com.fintex.ce.returns;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.MonthlyReturnGeneratableHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Port for generating monthly returns from holdings.
 */
public interface ReturnsGenerator {

  /**
   * Generate monthly returns for GIC holdings.
   */
  Map<PortfolioHolding, HoldingMonthlyReturns> generateGicMonthlyReturns(List<PortfolioHolding> holdings);

  /**
   * Generate returns for a holding with interest rate.
   */
  TreeMap<LocalDate, BigDecimal> generateReturns(MonthlyReturnGeneratableHolding holding);

}
