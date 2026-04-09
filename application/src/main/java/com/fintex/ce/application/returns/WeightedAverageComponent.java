package com.fintex.ce.application.returns;

import com.fintex.ce.application.calculation.metric.formula.SumProduct;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.ReturnFactorScale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ZERO;

@EqualsAndHashCode
public class WeightedAverageComponent {

  private final ReturnFactorScale returnFactorScale;

  public WeightedAverageComponent(final ReturnFactorScale returnFactorScale) {
    this.returnFactorScale = returnFactorScale;
  }

  public NavigableMap<LocalDate, BigDecimal> calculateWeightedAverage(
      final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns) {
    final Map<Holding, TreeMap<LocalDate, BigDecimal>> endingPortfolioWeight = calculateEndingPortfolioWeight(returns);
    return new TreeMap<>(calculateTotalPortfolioReturnFactor(returns, endingPortfolioWeight));
  }

  public Map<LocalDate, BigDecimal> calculateTotalPortfolioReturnFactor(
      final Map<Holding, TreeMap<LocalDate, BigDecimal>> portfolioBaseTotalReturn,
      final Map<Holding, TreeMap<LocalDate, BigDecimal>> endingPortfolioWeight) {
    final NavigableMap<LocalDate, BigDecimal> calculate = new SumProduct<>(portfolioBaseTotalReturn,
        endingPortfolioWeight)
        .setMap2KeyFinder(date -> toLastDayOfMonth(date.minusMonths(1)))
        .calculate();
    return calculate.entrySet().stream().collect(toTreeMap(Map.Entry::getKey, returnFactorScale.getFormula()));
  }

  public Map<Holding, TreeMap<LocalDate, BigDecimal>> calculateEndingPortfolioWeight(
      final Map<Holding, TreeMap<LocalDate, BigDecimal>> pBaseTotalReturn) {
    final Map<Holding, BigDecimal> initialWeights = PortfolioUtils.calculateInitialPortfolioWeight(pBaseTotalReturn
        .keySet());
    return pBaseTotalReturn.entrySet().stream().collect(toMap(Map.Entry::getKey, collectMonthlyWeightEntries(
        initialWeights)));
  }

  /**
   * Works for monthly rebalance
   *
   * @param initialWeights
   *          initial holdings weights of portfolio
   * @return function to collect starting portfolio holdings weights for each time period for single holding
   */
  public Function<Map.Entry<Holding, TreeMap<LocalDate, BigDecimal>>, TreeMap<LocalDate, BigDecimal>> collectMonthlyWeightEntries(
      Map<Holding, BigDecimal> initialWeights) {
    return topEntry -> {
      final Map<LocalDate, BigDecimal> hBaseTotalReturns = new HashMap<>(topEntry.getValue());
      final LocalDate initDate = topEntry.getValue().keySet().stream().min(LocalDate::compareTo).orElseThrow();
      // add a month before start date to be able to refer to this date from other calculations
      hBaseTotalReturns.put(toLastDayOfMonth(initDate.minusMonths(1)), ZERO);
      return hBaseTotalReturns.entrySet().stream().collect(toTreeMap(Map.Entry::getKey, e -> initialWeights.get(topEntry
          .getKey())));
    };
  }
}
