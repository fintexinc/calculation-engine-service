package com.fintex.ce.util;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.Holding;
import java.util.List;
import java.util.function.Predicate;

public class FilterUtils {

  public static final Predicate<Holding> CANADA_MUTUAL_PREDICATE = h -> HoldingType.CANADA_MUTUAL_FUNDS.equals(h
      .getType()) || HoldingType.SEGREGATED_FUND_CANADA.equals(h.getType());

  public static final Predicate<Holding> STOCK_PREDICATE = h -> HoldingType.CANADA_STOCKS.equals(h.getType())
      || HoldingType.US_STOCKS.equals(h.getType());

  public static final Predicate<Holding> US_STOCKS_PREDICATE = h -> HoldingType.US_STOCKS.equals(h.getType());

  public static final Predicate<Holding> CANADA_STOCKS_PREDICATE = h -> HoldingType.CANADA_STOCKS.equals(h.getType());

  public static final Predicate<Holding> US_ETF_PREDICATE = h -> HoldingType.US_ETF.equals(h.getType());

  public static final Predicate<Holding> CANADA_ETF_PREDICATE = h -> HoldingType.CANADA_ETF.equals(h.getType());

  public static final Predicate<Holding> BENCHMARKS_PREDICATE = h -> HoldingType.BENCHMARK_INDEX.equals(h.getType());

  public static final Predicate<Holding> ETF_PREDICATE = h -> HoldingType.CANADA_ETF.equals(h.getType())
      || HoldingType.US_ETF.equals(h.getType());

  public static final Predicate<Holding> CASH_PREDICATE = h -> HoldingType.CASH.equals(h.getType());

  public static final Predicate<Holding> GIC_PREDICATE = h -> HoldingType.GIC.equals(h.getType());

  public static final Predicate<Holding> US_MUTUAL_FUND_PREDICATE = h -> HoldingType.US_MUTUAL_FUNDS.equals(h
      .getType());

  public static final Predicate<Holding> CANADA_POOLED_FUND_PREDICATE = h -> HoldingType.CANADA_POOLED_FUNDS.equals(h
      .getType());

  public static final Predicate<Holding> CANADA_HEDGE_FUND_PREDICATE = h -> HoldingType.CANADA_HEDGE_FUNDS.equals(h
      .getType());

  public static final Predicate<Holding> FIXED_INCOME_PREDICATE = h -> HoldingType.FIXED_INCOME.equals(h.getType());

  public static final Predicate<Holding> SEPARATELY_MANAGED_ACCOUNT_PREDICATE = h -> HoldingType.SEPARATELY_MANAGED_ACCOUNT
      .equals(h.getType());

  public static final Predicate<Holding> PAG_GUIDED_PORTFOLIO_PREDICATE = h -> HoldingType.PAG_GUIDED_PORTFOLIO.equals(h
      .getType());

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

}
