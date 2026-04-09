package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.calculation.SalesChargeCategory;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.SalesChargeResult;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

public class SalesChargeCalculation {

  private final Map<Holding, SalesCharge> salesCharges;

  public static final SalesChargeResult.SalesChargeEntry DEFAULT_SALES_CHARGE_DTO = new SalesChargeResult.SalesChargeEntry(
      ZERO, ZERO, Set.of());
  protected static final Map<SalesChargeCategory, SalesChargeResult.SalesChargeEntry> DEFAULT_MAP = new EnumMap<>(
      SalesChargeCategory.class);

  static {
    Stream.of(SalesChargeCategory.values()).forEach(type -> DEFAULT_MAP.put(type, DEFAULT_SALES_CHARGE_DTO));
  }

  public SalesChargeCalculation(final Map<Holding, SalesCharge> salesCharges) {
    this.salesCharges = salesCharges;
  }

  public SalesChargeResult calculate() {
    if (CollectionUtils.isEmpty(salesCharges)) {
      return new SalesChargeResult().setSalesCharges(DEFAULT_MAP);
    }
    return calculateSalesCharge();
  }

  /**
   * calculates allocation, value and holdings for each sales charge type. divide sum of mutual funds of particular
   * sales charge type to sum of all mutual funds in portfolio results in 'allocation' of sales charge type sum of
   * mutual funds of particular sales charge type results in 'value' of sales charge type
   *
   * @return sales charges with allocation, value and holdings with respective allocations
   */
  private SalesChargeResult calculateSalesCharge() {
    final Map<SalesChargeCategory, SalesChargeResult.SalesChargeEntry> result = new EnumMap<>(DEFAULT_MAP);

    final Map<SalesChargeCategory, Set<Holding>> groupedHoldingsBySalesCharge = groupBySalesChargeCategories(
        salesCharges);

    final BigDecimal categorizedMarketValues = groupedHoldingsBySalesCharge.values().stream()
        .flatMap(Set::stream)
        .map(Holding::getValue)
        .reduce(ZERO, BigDecimal::add);

    groupedHoldingsBySalesCharge.forEach((salesCharge, holdingSet) -> {
      final BigDecimal allocation = calculateAllocation(holdingSet, categorizedMarketValues);
      final BigDecimal value = getSumOfMarketValues(holdingSet);
      final Set<SalesChargeResult.SalesChargeHoldingEntry> salesChargeHoldingResDtos = getSalesChargeHoldingResDtos(
          holdingSet, categorizedMarketValues);

      result.put(salesCharge, new SalesChargeResult.SalesChargeEntry(allocation, value, salesChargeHoldingResDtos));
    });

    return new SalesChargeResult().setSalesCharges(result);
  }

  private BigDecimal calculateAllocation(final Set<Holding> holdingSet, final BigDecimal totalMarketValues) {
    return toUserScale(divide(getSumOfMarketValues(holdingSet), totalMarketValues));
  }

  private Set<SalesChargeResult.SalesChargeHoldingEntry> getSalesChargeHoldingResDtos(final Set<Holding> holdingSet,
      final BigDecimal totalMarketValues) {
    return holdingSet.stream()
        .map(holding -> new SalesChargeResult.SalesChargeHoldingEntry(holding.generateUserIdentifier(),
            getMutualFundAllocation(holding, totalMarketValues)))
        .collect(Collectors.toSet());
  }

  private BigDecimal getMutualFundAllocation(final Holding holding, final BigDecimal totalMarketValues) {
    return toUserScale(divide(holding.getValue(), totalMarketValues));
  }

  /**
   * groups holdings by sales charge types.
   *
   * @param salesCharges
   *          map of holdings as key and sales charge type as value.
   * @return grouped holdings.
   */
  private Map<SalesChargeCategory, Set<Holding>> groupBySalesChargeCategories(
      final Map<Holding, SalesCharge> salesCharges) {
    return salesCharges.entrySet().stream()
        .filter(e -> e.getValue().getType() != null)
        .filter(e -> SalesChargeCategory.fromValue(e.getValue().getType()) != null)
        .collect(groupingBy(e -> SalesChargeCategory.fromValue(e.getValue().getType()),
            mapping(Map.Entry::getKey, toSet())));
  }

  /**
   * calculates sum of market values, uses holding values from provided set of holdings.
   */
  private BigDecimal getSumOfMarketValues(final Set<Holding> holdings) {
    return holdings.stream().map(Holding::getValue).reduce(ZERO, BigDecimal::add);
  }
}
