package ca.tangerine.pce.util;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilterUtils {

  /**
   * Instrument types whose data is sourced locally rather than fetched from the external security-data provider. Single
   * source of truth shared by the adapter-side request filter ({@code HoldingMappingUtils.isSkipped}) and the
   * application-side validators that must not flag these as "not found" — for these types the calculation either uses
   * internally-generated returns (GIC, via {@code MonthlyReturnsGenerator}) or currency-specific Treasury Bill returns
   * (CASH).
   */
  public static final Set<FinancialInstrumentType> LOCALLY_SOURCED_TYPES = Set.of(
      FinancialInstrumentType.CASH,
      FinancialInstrumentType.GIC);

  public static final Predicate<PortfolioHolding> CANADA_MUTUAL_PREDICATE = h -> Country.CANADA.equals(h.getCountry())
      && (FinancialInstrumentType.MUTUAL_FUND.equals(h.getHoldingType())
          || FinancialInstrumentType.SEGREGATED_FUND.equals(h.getHoldingType()));

  public static final Predicate<PortfolioHolding> STOCK_PREDICATE = h -> isOfType(h.getHoldingType(),
      FinancialInstrumentType.STOCK);

  public static final Predicate<PortfolioHolding> US_STOCKS_PREDICATE = h -> Country.USA.equals(h.getCountry())
      && isOfType(h.getHoldingType(), FinancialInstrumentType.STOCK);

  public static final Predicate<PortfolioHolding> CANADA_STOCKS_PREDICATE = h -> Country.CANADA.equals(h.getCountry())
      && isOfType(h.getHoldingType(), FinancialInstrumentType.STOCK);

  public static final Predicate<PortfolioHolding> US_ETF_PREDICATE = h -> Country.USA.equals(h.getCountry())
      && isOfType(h.getHoldingType(), FinancialInstrumentType.ETF);

  public static final Predicate<PortfolioHolding> CANADA_ETF_PREDICATE = h -> Country.CANADA.equals(h.getCountry())
      && isOfType(h.getHoldingType(), FinancialInstrumentType.ETF);

  public static final Predicate<PortfolioHolding> BENCHMARKS_PREDICATE = h -> FinancialInstrumentType.BENCHMARK_INDEX
      .equals(h
          .getHoldingType());

  public static final Predicate<PortfolioHolding> ETF_PREDICATE = h -> isOfType(h.getHoldingType(),
      FinancialInstrumentType.ETF);

  public static final Predicate<PortfolioHolding> CASH_PREDICATE = h -> FinancialInstrumentType.CASH.equals(h
      .getHoldingType());

  public static final Predicate<PortfolioHolding> GIC_PREDICATE = h -> isOfType(h.getHoldingType(),
      FinancialInstrumentType.GIC);

  public static final Predicate<PortfolioHolding> FIXED_INCOME_PREDICATE = h -> FinancialInstrumentType.FIXED_INCOME
      .equals(h
          .getHoldingType());

  public static final Predicate<PortfolioHolding> SEPARATELY_MANAGED_ACCOUNT_PREDICATE = h -> FinancialInstrumentType.SEPARATELY_MANAGED_ACCOUNT
      .equals(h.getHoldingType());

  // TODO: Ask business requirements for PAG_GUIDED_PORTFOLIO - it is not present in FinancialInstrumentType enum
  // public static final Predicate<PortfolioHolding> PAG_GUIDED_PORTFOLIO_PREDICATE = ...

  private FilterUtils() {
  }

  /**
   * Returns true when {@code type} is either {@code target} or any descendant of {@code target} reachable through the
   * {@link FinancialInstrumentType#getParent()} chain. Used by the family predicates ({@code STOCK_PREDICATE},
   * {@code ETF_PREDICATE}, {@code GIC_PREDICATE}) so a specific type routes through the same branch as its parent
   * category regardless of the holding's country, which is now carried as a separate dimension.
   */
  public static boolean isOfType(FinancialInstrumentType type, FinancialInstrumentType target) {
    FinancialInstrumentType current = type;
    while (current != null) {
      if (current == target) {
        return true;
      }
      current = current.getParent();
    }
    return false;
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

  /**
   * Restricts per-holding attribute data to the given holdings, mirroring what a fetch for exactly that subset would
   * have produced. The returned map is mutable.
   */
  public static <D> Map<PortfolioHolding, D> restrictToHoldings(Map<PortfolioHolding, D> data,
      List<? extends PortfolioHolding> holdings) {
    if (holdings == null) {
      return new HashMap<>();
    }
    return holdings.stream()
        .filter(data::containsKey)
        .collect(Collectors.toMap(Function.identity(), data::get,
            (existing, duplicate) -> existing, HashMap::new));
  }

}
