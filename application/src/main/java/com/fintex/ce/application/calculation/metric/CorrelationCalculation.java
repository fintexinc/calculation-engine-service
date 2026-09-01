package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.correlation.CorrelationPeriodResult;
import com.fintex.ce.model.domain.result.correlation.CorrelationResult;
import com.fintex.ce.model.domain.result.correlation.HoldingsKeyResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.pow;
import static com.fintex.ce.application.util.DecimalUtils.squareRoot;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.application.util.PortfolioUtils.createKey;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;

@EqualsAndHashCode(callSuper = true)
public class CorrelationCalculation
    extends
      PeriodCalculationAbstract<CorrelationResult, List<CorrelationPeriodResult>> {

  private final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> portfolioBaseTotalReturn;

  public CorrelationCalculation(final PeriodCalculationInput context,
      final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> portfolioBaseTotalReturn,
      final Set<TimePeriod> defaultPeriods) {
    super(context, defaultPeriods);
    this.portfolioBaseTotalReturn = portfolioBaseTotalReturn;
  }

  @Override
  public List<CorrelationPeriodResult> calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    Map<PortfolioHolding, Map<LocalDate, BigDecimal>> returns = portfolioBaseTotalReturn.entrySet().stream()
        .filter(holdingReturns -> hasEnoughReturns(numberOfMonths, holdingReturns))
        .collect(Collectors.toMap(Map.Entry::getKey, e -> calculatePortfolioBaseTotalReturnValuesByPeriod(
            numberOfMonths, e.getValue())));
    return returns.keySet().stream()
        .map(localDateBigDecimalMap -> getCorrelationPeriod(localDateBigDecimalMap, returns, numberOfMonths))
        .toList();
  }

  public boolean hasEnoughReturns(int numberOfMonths,
      final Map.Entry<PortfolioHolding, Map<LocalDate, BigDecimal>> holdingReturns) {
    return holdingReturns.getValue().size() >= numberOfMonths;
  }

  @Override
  public List<CorrelationPeriodResult> toUserFormat(final List<CorrelationPeriodResult> correlationPeriodDtoS) {
    if (correlationPeriodDtoS == null) {
      return null;
    }

    return correlationPeriodDtoS.stream()
        .map(dto -> CollectionUtils.isEmpty(dto.correlations())
            ? dto
            : new CorrelationPeriodResult(dto.period(), dto.key(),
                dto.correlations().entrySet().stream()
                    .collect(LinkedHashMap::new, (map, e) -> map.put(e.getKey(), toUserScale(e.getValue())),
                        Map::putAll)))
        .toList();
  }

  @Override
  public CorrelationResult defineResponseType(final Map<String, List<CorrelationPeriodResult>> periodValues) {
    List<CorrelationPeriodResult> correlationPeriods = periodValues.entrySet().stream()
        .filter(e -> Objects.nonNull(e.getValue()))
        .flatMap(e -> setPeriod(e.getKey(), e.getValue()).stream())
        .toList();
    List<HoldingsKeyResult> holdingsKeys = portfolioBaseTotalReturn.keySet().stream()
        .map(HoldingsKeyResult::buildHoldingsKeyResult)
        .toList();
    return CorrelationResult.builder()
        .correlationPeriods(correlationPeriods)
        .holdingsKey(holdingsKeys)
        .build();
  }

  /**
   * Method is used to set a period. E.g the period for CIPSD is 20 but in response should be
   * "SINCE_CUSTOM_PERFORMANCE_START_DATE" instead of 20.
   *
   * @param period
   *          period to set.
   * @param periods
   *          list of all periods.
   * @return mapped period.
   */
  public List<CorrelationPeriodResult> setPeriod(final String period, final List<CorrelationPeriodResult> periods) {
    return periods.stream()
        .map(e -> new CorrelationPeriodResult(period, e.key(), e.correlations()))
        .toList();
  }

  /**
   * calculates portfolio base total return values (x - average). x is the return of holding, average is average value
   * by period.
   *
   * @param numberOfMonths
   *          number of month in period
   * @param portfolioTotalReturnValue
   *          portfolioBaseTotalReturn (Calculated off local and FX Rates)
   * @return calculated portfolio base total return values
   */
  public Map<LocalDate, BigDecimal> calculatePortfolioBaseTotalReturnValuesByPeriod(final int numberOfMonths,
      final Map<LocalDate, BigDecimal> portfolioTotalReturnValue) {
    TreeMap<LocalDate, BigDecimal> portfolioTotalReturnByHolding = new TreeMap<>(portfolioTotalReturnValue);
    LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, portfolioTotalReturnByHolding);
    SortedMap<LocalDate, BigDecimal> portfolioBaseTotalReturnByPeriodStartDate = getSubMapByPeriodStartDate(
        periodStartDate, portfolioTotalReturnByHolding);
    BigDecimal average = CalculationUtils.average(portfolioBaseTotalReturnByPeriodStartDate);
    return portfolioBaseTotalReturnByPeriodStartDate.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().subtract(average)));
  }

  /**
   * calculates correlation and maps to CorrelationPeriodResult
   *
   * @param keyHolding
   *          key holding in current correlation
   * @param returns
   *          calculated portfolio base total return values by period
   * @param numberOfMonths
   * @return mapped CorrelationPeriodResult
   */
  public CorrelationPeriodResult getCorrelationPeriod(final PortfolioHolding keyHolding,
      final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> returns,
      final int numberOfMonths) {
    Map<LocalDate, BigDecimal> keyHoldingValues = returns.get(keyHolding);
    Map<PortfolioHolding, BigDecimal> correlations = returns.entrySet().stream()
        .filter(e -> !e.getKey().equals(keyHolding))
        .collect(Collectors.toMap(Map.Entry::getKey, e -> calculateCorrelation(keyHoldingValues, e.getValue())));
    return mapToCorrelationPeriodResult(keyHolding, numberOfMonths, correlations);
  }

  /**
   * @param keyHolding
   *          key holding in current correlation
   * @param numberOfMonths
   *          number of month in period
   * @param correlations
   *          calculated correlations
   * @return CorrelationPeriodResult
   */
  public CorrelationPeriodResult mapToCorrelationPeriodResult(final PortfolioHolding keyHolding,
      final int numberOfMonths,
      final Map<PortfolioHolding, BigDecimal> correlations) {
    return new CorrelationPeriodResult(
        String.valueOf(numberOfMonths),
        createKey(keyHolding),
        correlations.entrySet().stream()
            .collect(LinkedHashMap::new, (map, c) -> map.put(createKey(c.getKey()), c.getValue()), Map::putAll));
  }

  /**
   * calculates correlation by formula
   *
   * @param keyHoldingValues
   *          portfolio base total return values by period of key holding
   * @param holdingValues
   *          portfolio base total return values by period of another holding needed for correlation
   * @return correlation
   */
  public BigDecimal calculateCorrelation(final Map<LocalDate, BigDecimal> keyHoldingValues,
      final Map<LocalDate, BigDecimal> holdingValues) {
    BigDecimal denominator = calculateDenominator(keyHoldingValues, holdingValues);
    if (BigDecimal.ZERO.compareTo(denominator) == 0) {
      return null;
    }
    return divide(calculateNumerator(keyHoldingValues, holdingValues), denominator);
  }

  /**
   * calculates numerator for correlation formula
   *
   * @param keyHoldingValues
   *          portfolio base total return values by period of key holding
   * @param holdingValues
   *          portfolio base total return values by period of another holding needed for correlation
   * @return numerator
   */
  public BigDecimal calculateNumerator(final Map<LocalDate, BigDecimal> keyHoldingValues,
      final Map<LocalDate, BigDecimal> holdingValues) {
    return keyHoldingValues.entrySet().stream()
        .map(e -> e.getValue().multiply(holdingValues.get(e.getKey())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * calculates denominator for correlation formula
   *
   * @param keyHoldingValues
   *          portfolio base total return values by period of key holding
   * @param holdingValues
   *          portfolio base total return values by period of another holding needed for correlation
   * @return denominator
   */
  public BigDecimal calculateDenominator(final Map<LocalDate, BigDecimal> keyHoldingValues,
      final Map<LocalDate, BigDecimal> holdingValues) {
    BigDecimal holdingX = getSumOfSquaredValues(keyHoldingValues);
    BigDecimal holdingY = getSumOfSquaredValues(holdingValues);
    return squareRoot(holdingX.multiply(holdingY));
  }

  /**
   * calculates sum of squared values
   *
   * @param holdingValues
   *          portfolio base total return values by period
   * @return sum of squared values
   */
  public BigDecimal getSumOfSquaredValues(final Map<LocalDate, BigDecimal> holdingValues) {
    return holdingValues.values().stream().map(v -> pow(v, TWO)).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

}
