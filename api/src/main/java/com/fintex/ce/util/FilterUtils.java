package com.fintex.ce.util;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.util.List;
import java.util.function.Predicate;

public class FilterUtils {

  public static final Predicate<PortfolioHolding> CANADA_MUTUAL_PREDICATE = h -> FinancialInstrumentType.MUTUAL_FUND_CANADA
      .equals(h
          .getHoldingType()) || FinancialInstrumentType.SEGREGATED_FUND_CANADA.equals(h.getHoldingType());

  public static final Predicate<PortfolioHolding> STOCK_PREDICATE = h -> FinancialInstrumentType.STOCK_CANADA.equals(h
      .getHoldingType())
      || FinancialInstrumentType.STOCK_US.equals(h.getHoldingType());

  public static final Predicate<PortfolioHolding> US_STOCKS_PREDICATE = h -> FinancialInstrumentType.STOCK_US.equals(h
      .getHoldingType());

  public static final Predicate<PortfolioHolding> CANADA_STOCKS_PREDICATE = h -> FinancialInstrumentType.STOCK_CANADA
      .equals(h
          .getHoldingType());

  public static final Predicate<PortfolioHolding> US_ETF_PREDICATE = h -> FinancialInstrumentType.ETF_US.equals(h
      .getHoldingType());

  public static final Predicate<PortfolioHolding> CANADA_ETF_PREDICATE = h -> FinancialInstrumentType.ETF_CANADA.equals(
      h
          .getHoldingType());

  public static final Predicate<PortfolioHolding> BENCHMARKS_PREDICATE = h -> FinancialInstrumentType.BENCHMARK_INDEX
      .equals(h
          .getHoldingType());

  public static final Predicate<PortfolioHolding> ETF_PREDICATE = h -> FinancialInstrumentType.ETF_CANADA.equals(h
      .getHoldingType())
      || FinancialInstrumentType.ETF_US.equals(h.getHoldingType());

  public static final Predicate<PortfolioHolding> CASH_PREDICATE = h -> FinancialInstrumentType.CASH.equals(h
      .getHoldingType());

  public static final Predicate<PortfolioHolding> GIC_PREDICATE = h -> FinancialInstrumentType.GIC.equals(h
      .getHoldingType());

  public static final Predicate<PortfolioHolding> FIXED_INCOME_PREDICATE = h -> FinancialInstrumentType.FIXED_INCOME
      .equals(h
          .getHoldingType());

  public static final Predicate<PortfolioHolding> SEPARATELY_MANAGED_ACCOUNT_PREDICATE = h -> FinancialInstrumentType.SEPARATELY_MANAGED_ACCOUNT
      .equals(h.getHoldingType());

  // TODO: Ask business requirements for PAG_GUIDED_PORTFOLIO - it is not present in FinancialInstrumentType enum
  // public static final Predicate<PortfolioHolding> PAG_GUIDED_PORTFOLIO_PREDICATE = ...

  private FilterUtils() {
  }

  @SuppressWarnings("unchecked")
  public static <H extends PortfolioHolding> List<H> filterHoldings(final List<? extends PortfolioHolding> holdings,
      final Predicate<? super PortfolioHolding> predicate) {
    return (List<H>) holdings.stream().filter(predicate).toList();
  }

  @SafeVarargs
  public static <T> List<T> getSpecifiedIfEmpty(final List<T> list, final T... specified) {
    if (list == null || list.isEmpty()) {
      return List.of(specified);
    }
    return list;
  }

  public static <T> List<T> getSpecifiedIfEmpty(List<T> list, List<T> defaults) {
    return list == null || list.isEmpty() ? defaults : list;
  }

}
