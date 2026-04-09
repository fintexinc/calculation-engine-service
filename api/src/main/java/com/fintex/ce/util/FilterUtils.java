package com.fintex.ce.util;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import java.util.List;
import java.util.function.Predicate;

public class FilterUtils {

  public static final Predicate<Holding> CANADA_MUTUAL_PREDICATE = h -> FinancialInstrumentType.MUTUAL_FUND_CANADA
      .equals(h
          .getHoldingType()) || FinancialInstrumentType.SEGREGATED_FUND_CANADA.equals(h.getHoldingType());

  public static final Predicate<Holding> STOCK_PREDICATE = h -> FinancialInstrumentType.STOCK_CANADA.equals(h
      .getHoldingType())
      || FinancialInstrumentType.STOCK_US.equals(h.getHoldingType());

  public static final Predicate<Holding> US_STOCKS_PREDICATE = h -> FinancialInstrumentType.STOCK_US.equals(h
      .getHoldingType());

  public static final Predicate<Holding> CANADA_STOCKS_PREDICATE = h -> FinancialInstrumentType.STOCK_CANADA.equals(h
      .getHoldingType());

  public static final Predicate<Holding> US_ETF_PREDICATE = h -> FinancialInstrumentType.ETF_US.equals(h
      .getHoldingType());

  public static final Predicate<Holding> CANADA_ETF_PREDICATE = h -> FinancialInstrumentType.ETF_CANADA.equals(h
      .getHoldingType());

  public static final Predicate<Holding> BENCHMARKS_PREDICATE = h -> FinancialInstrumentType.BENCHMARK_INDEX.equals(h
      .getHoldingType());

  public static final Predicate<Holding> ETF_PREDICATE = h -> FinancialInstrumentType.ETF_CANADA.equals(h
      .getHoldingType())
      || FinancialInstrumentType.ETF_US.equals(h.getHoldingType());

  public static final Predicate<Holding> CASH_PREDICATE = h -> FinancialInstrumentType.CASH.equals(h.getHoldingType());

  public static final Predicate<Holding> GIC_PREDICATE = h -> FinancialInstrumentType.GIC.equals(h.getHoldingType());

  public static final Predicate<Holding> FIXED_INCOME_PREDICATE = h -> FinancialInstrumentType.FIXED_INCOME.equals(h
      .getHoldingType());

  public static final Predicate<Holding> SEPARATELY_MANAGED_ACCOUNT_PREDICATE = h -> FinancialInstrumentType.SEPARATELY_MANAGED_ACCOUNT
      .equals(h.getHoldingType());

  // TODO: Ask business requirements for PAG_GUIDED_PORTFOLIO - it is not present in FinancialInstrumentType enum
  // public static final Predicate<Holding> PAG_GUIDED_PORTFOLIO_PREDICATE = ...

  private FilterUtils() {
  }

  @SuppressWarnings("unchecked")
  public static <H extends Holding> List<H> filterHoldings(final List<? extends Holding> holdings,
      final Predicate<? super Holding> predicate) {
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
