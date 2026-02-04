package com.fintex.ce.application.calculation.core;

import com.fintex.ce.application.result.core.IntervalResult;
import com.fintex.ce.application.result.core.RollingIntervalResult;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.port.input.result.PeriodResult;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.util.DecimalUtils.toUserScale;

public abstract class RollingAbstractCalculation<T extends PeriodResult>
    extends
      PeriodCalculationAbstract<T, NavigableMap<LocalDate, BigDecimal>> {

  protected RollingAbstractCalculation(final CalculationDTO input,
      final Set<String> defaultPeriods) {
    super(input, defaultPeriods);
  }

  @Override
  public NavigableMap<LocalDate, BigDecimal> calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    return calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
  }

  public NavigableMap<LocalDate, BigDecimal> calculatePeriodForNumberOfMonths(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns) {
    if (numberOfMonths > portfolioReturns.size()) {
      return null;
    }
    final LocalDate performanceStartDate = portfolioReturns.firstEntry().getKey();
    final LocalDate performanceEndDate = portfolioReturns.lastEntry().getKey();

    final var firstAvailableDateForRollingReturn = performanceStartDate.plusMonths(numberOfMonths - 1L);

    final var result = new TreeMap<LocalDate, BigDecimal>();
    for (final var portfolioReturn : portfolioReturns.entrySet()) {
      if (isInRange(portfolioReturn, firstAvailableDateForRollingReturn, performanceEndDate)) {
        final NavigableMap<LocalDate, BigDecimal> returns = portfolioReturns.headMap(portfolioReturn.getKey(), true);
        final BigDecimal calculatedRollingValue = calculateRollingValue(numberOfMonths, returns);
        result.put(portfolioReturn.getKey(), calculatedRollingValue);
      }
    }
    return result;
  }

  /**
   * Calculates Rolling Value for Rolling calculations.
   * <p>
   * Should to be overridden in all Rolling calculations: (Rolling Total Returns, Rolling Standard Deviation, Rolling
   * Sharpe Ratio, Rolling Correlation) and implement its custom logic.
   *
   * @param numberOfMonths
   *          period to be calculated.
   * @param returns
   *          portfolio total returns.
   * @return calculated Rolling value.
   */
  public abstract BigDecimal calculateRollingValue(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> returns);

  /**
   * Checks if the portfolio entry if in needed range. It should be greater than startDateOfRollingReturn and less than
   * portfolio End Date.
   *
   * @param returnEntry
   *          portfolio entry.
   * @param startDateOfRollingReturn
   *          start date of rollin returns.
   * @param ped
   *          portfolio performance end date.
   * @return true - if in range, otherwise false.
   */
  public boolean isInRange(final Map.Entry<LocalDate, BigDecimal> returnEntry,
      final LocalDate startDateOfRollingReturn, final LocalDate ped) {
    return returnEntry.getKey().compareTo(startDateOfRollingReturn) >= 0 && returnEntry.getKey().compareTo(ped) <= 0;
  }

  public Set<RollingIntervalResult> getRollingIntervalResults(
      final Set<Pair<String, NavigableMap<LocalDate, BigDecimal>>> result) {
    return result
        .stream()
        .map(e -> new RollingIntervalResult(e.getKey(), mapRollingReturn(e)))
        .collect(Collectors.toSet());
  }

  public Set<IntervalResult> mapRollingReturn(final Pair<String, NavigableMap<LocalDate, BigDecimal>> result) {
    if (Objects.isNull(result.getValue())) {
      return null;
    }
    return result.getValue().entrySet().stream()
        .map(e -> new IntervalResult(e.getKey(), e.getValue()))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public NavigableMap<LocalDate, BigDecimal> toUserFormat(final NavigableMap<LocalDate, BigDecimal> rollingReturn) {
    if (Objects.isNull(rollingReturn)) {
      return null;
    }
    final var result = new TreeMap<LocalDate, BigDecimal>();
    rollingReturn.forEach((k, v) -> result.put(k, toUserScale(v)));
    return result;
  }
}
