package ca.tangerine.pce.application.returns;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import lombok.EqualsAndHashCode;

import static ca.tangerine.pce.application.util.CollectorUtils.toMap;
import static ca.tangerine.pce.application.util.CollectorUtils.toTreeMap;
import static ca.tangerine.pce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ZERO;

import ca.tangerine.pce.application.calculation.metric.formula.SumProduct;
import ca.tangerine.pce.application.util.PortfolioUtils;
import ca.tangerine.pce.application.util.ReturnFactorScale;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

/**
 * Stateless component that computes the portfolio-level weighted-average return time series for a per-holding returns
 * map. The {@link ReturnFactorScale} is supplied per call so the bean carries no request-time configuration.
 */
@Component
@EqualsAndHashCode
public class WeightedAverageComponent {

  public NavigableMap<LocalDate, BigDecimal> calculateWeightedAverage(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns,
      ReturnFactorScale returnFactorScale) {
    if (CollectionUtils.isEmpty(returns)) {
      return new TreeMap<>();
    }
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> endingPortfolioWeight = calculateEndingPortfolioWeight(
        returns);
    return new TreeMap<>(calculateTotalPortfolioReturnFactor(returns, endingPortfolioWeight, returnFactorScale));
  }

  public Map<LocalDate, BigDecimal> calculateTotalPortfolioReturnFactor(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> portfolioBaseTotalReturn,
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> endingPortfolioWeight,
      ReturnFactorScale returnFactorScale) {
    NavigableMap<LocalDate, BigDecimal> calculate = new SumProduct<>(portfolioBaseTotalReturn, endingPortfolioWeight)
        .setMap2KeyFinder(date -> toLastDayOfMonth(date.minusMonths(1)))
        .calculate();
    return calculate.entrySet().stream().collect(toTreeMap(Map.Entry::getKey, returnFactorScale.getFormula()));
  }

  public Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> calculateEndingPortfolioWeight(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> portfolioBaseTotalReturn) {
    Map<PortfolioHolding, BigDecimal> initialWeights = PortfolioUtils.calculateInitialPortfolioWeight(
        portfolioBaseTotalReturn.keySet());
    return portfolioBaseTotalReturn.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, collectMonthlyWeightEntries(initialWeights)));
  }

  /**
   * Works for monthly rebalance.
   *
   * @param initialWeights
   *          initial holdings weights of portfolio
   * @return function to collect starting portfolio holdings weights for each time period for single holding
   */
  public Function<Map.Entry<PortfolioHolding, TreeMap<LocalDate, BigDecimal>>, TreeMap<LocalDate, BigDecimal>> collectMonthlyWeightEntries(
      Map<PortfolioHolding, BigDecimal> initialWeights) {
    return topEntry -> {
      Map<LocalDate, BigDecimal> hBaseTotalReturns = new HashMap<>(topEntry.getValue());
      LocalDate initDate = topEntry.getValue().keySet().stream().min(LocalDate::compareTo).orElseThrow();
      hBaseTotalReturns.put(toLastDayOfMonth(initDate.minusMonths(1)), ZERO);
      return hBaseTotalReturns.entrySet().stream().collect(toTreeMap(Map.Entry::getKey,
          entry -> initialWeights.get(topEntry.getKey())));
    };
  }
}
