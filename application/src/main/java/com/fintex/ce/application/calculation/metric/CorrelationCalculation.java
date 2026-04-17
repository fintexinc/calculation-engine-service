package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.correlation.CorrelationKeyValueResult;
import com.fintex.ce.model.domain.result.correlation.CorrelationPeriodResult;
import com.fintex.ce.model.domain.result.correlation.CorrelationResult;
import com.fintex.ce.model.domain.result.correlation.HoldingsKeyResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.util.CalculationUtils;

import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.pow;
import static com.fintex.ce.util.DecimalUtils.squareRoot;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.createKey;

@EqualsAndHashCode(callSuper = true)
public class CorrelationCalculation
    extends
      PeriodCalculationAbstract<CorrelationResult, List<CorrelationPeriodResult>> {

  private final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> portfolioBaseTotalReturn;

  public CorrelationCalculation(final CalculationDTO calculationDTO,
      final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> portfolioBaseTotalReturn,
      final Set<String> defaultPeriods) {
    super(calculationDTO, defaultPeriods);
    this.portfolioBaseTotalReturn = portfolioBaseTotalReturn;
  }

  @Override
  public List<CorrelationPeriodResult> calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final Map<PortfolioHolding, Map<LocalDate, BigDecimal>> returns = portfolioBaseTotalReturn.entrySet().stream()
        .filter(holdingReturns -> hasEnoughReturns(numberOfMonths, holdingReturns))
        .collect(Collectors.toMap(Map.Entry::getKey, e -> calculatePortfolioBaseTotalReturnValuesByPeriod(
            numberOfMonths, e.getValue())));
    return returns.keySet().stream()
        .map(localDateBigDecimalMap -> getCorrelationPeriod(localDateBigDecimalMap, returns, numberOfMonths))
        .collect(Collectors.toList());
  }

  public boolean hasEnoughReturns(int numberOfMonths,
      final Map.Entry<PortfolioHolding, Map<LocalDate, BigDecimal>> holdingReturns) {
    return holdingReturns.getValue().size() >= numberOfMonths;
  }

  @Override
  public List<CorrelationPeriodResult> toUserFormat(final List<CorrelationPeriodResult> correlationPeriodDTOS) {
    if (correlationPeriodDTOS == null) {
      return null;
    }

    for (final var dto : correlationPeriodDTOS) {
      if (!CollectionUtils.isEmpty(dto.getCorrelations())) {
        dto.getCorrelations().forEach(e -> e.setValue(toUserScale(e.getValue())));
      }
    }
    return correlationPeriodDTOS;
  }

  @Override
  public CorrelationResult defineResponseType(final Set<Pair<String, List<CorrelationPeriodResult>>> result) {
    final List<CorrelationPeriodResult> correlationPeriods = result.stream()
        .filter(v -> Objects.nonNull(v.getValue()))
        .flatMap(l -> setPeriod(l.getKey(), l.getValue()).stream())
        .collect(Collectors.toList());
    final List<HoldingsKeyResult> holdingsKeys = portfolioBaseTotalReturn.keySet().stream()
        .map(HoldingsKeyResult::buildHoldingsKeyResult)
        .collect(Collectors.toList());
    return new CorrelationResult()
        .setCorrelationPeriods(correlationPeriods)
        .setHoldingsKey(holdingsKeys);
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
    return periods.stream().map(e -> e.setPeriod(period)).collect(Collectors.toList());
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
    final TreeMap<LocalDate, BigDecimal> portfolioTotalReturnByHolding = new TreeMap<>(portfolioTotalReturnValue);
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, portfolioTotalReturnByHolding);
    final SortedMap<LocalDate, BigDecimal> portfolioBaseTotalReturnByPeriodStartDate = getSubMapByPeriodStartDate(
        periodStartDate, portfolioTotalReturnByHolding);
    final BigDecimal average = CalculationUtils.average(portfolioBaseTotalReturnByPeriodStartDate);
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
    final Map<LocalDate, BigDecimal> keyHoldingValues = returns.get(keyHolding);
    final Map<PortfolioHolding, BigDecimal> correlations = returns.entrySet().stream()
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
    return new CorrelationPeriodResult()
        .setPeriod(String.valueOf(numberOfMonths))
        .setKey(createKey(keyHolding))
        .setCorrelations(correlations.entrySet().stream()
            .map(c -> new CorrelationKeyValueResult(createKey(c.getKey()), c.getValue()))
            .collect(Collectors.toList()));
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
    return divide(calculateNumerator(keyHoldingValues, holdingValues), calculateDenominator(keyHoldingValues,
        holdingValues));
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
    final BigDecimal holdingX = getSumOfSquaredValues(keyHoldingValues);
    final BigDecimal holdingY = getSumOfSquaredValues(holdingValues);
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
