package com.fintex.ce.returns;

import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.MonthlyReturnGeneratableHolding;
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
  Map<Holding, MonthlyReturns> generateGicMonthlyReturns(List<Holding> holdings);

  /**
   * Generate returns for a holding with interest rate.
   */
  TreeMap<LocalDate, BigDecimal> generateReturns(MonthlyReturnGeneratableHolding holding);

}
