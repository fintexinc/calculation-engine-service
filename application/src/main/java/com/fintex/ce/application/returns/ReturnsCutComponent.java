package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ReturnsCutComponent {

  public Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> cutReturnsByEndDate(
      final Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns,
      final LocalDate endDate) {
    return filterReturnsDates(returns, (dateOfReturn) -> dateOfReturn.isAfter(endDate));
  }

  public Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> cutReturnsByStartDate(
      final Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns,
      final LocalDate startDate) {
    return filterReturnsDates(returns, (monthlyReturn) -> monthlyReturn.isBefore(startDate));
  }

  private Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> filterReturnsDates(
      final Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns,
      final Predicate<LocalDate> returnDateFilter) {
    final HashMap<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> copyOfReturns = new HashMap<>(returns);
    copyOfReturns.forEach((key, value) -> value.entrySet().removeIf(i -> returnDateFilter.test(i.getKey())));
    return copyOfReturns;
  }
}
