package com.fintex.ce.application.calculation.core;

import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.model.result.PeriodResult;
import com.fintex.ce.domain.exception.ReqValidationException;
import com.fintex.ce.util.DecimalUtils;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static com.fintex.ce.domain.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.domain.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.domain.model.enumeration.Period.*;
import static com.fintex.ce.util.CalculationUtils.product;
import static com.fintex.ce.util.CalculationUtils.sum;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DateTimeUtils.getMonthsBetweenDates;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * V - type of calculated value. e.g If calculation returns BigDecimal value for period, then V -> BigDecimal.
 */
@Getter
public abstract class PeriodCalculationAbstract<T extends PeriodResult, V> {

  private static final long ONE_MONTH = 1;

  public final Set<String> defaultPeriods;
  public NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns;
  public LocalDate cipsd;

  protected PeriodCalculationAbstract(final CalculationDTO input,
      final Set<String> defaultPeriods) {
    this.cipsd = input.getCipsd();
    this.portfolioTotalReturns = input.getWeightedAveragePortfolioReturns();
    this.defaultPeriods = defaultPeriods;
  }

  /**
   * Calculates period for the entered number of months
   *
   * @param numberOfMonths
   *          number of months
   * @return calculated value for the given period
   */
  public abstract V calculatePeriodForNumberOfMonths(final int numberOfMonths);

  /**
   * Calculates periods
   *
   * @param periods
   *          entered periods
   * @return calculated periods
   */
  public Set<Pair<String, V>> calculatePeriods(final Set<String> periods) {
    final Set<String> initialPeriods = getInitialPeriods(periods);
    final Set<Pair<String, V>> result = initialPeriods.stream()
        .filter(periodStr -> !SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name().equalsIgnoreCase(periodStr))
        .map(this::calculateForPeriod).collect(Collectors.toSet());
    addSinceCustomIntervalPerformanceStartDate(result, initialPeriods);
    return result;
  }

  /**
   * Calculates single period
   *
   * @param period
   *          period
   * @return result for a single period
   */
  public Pair<String, V> calculateForPeriod(final String period) {
    final int months = getNumberOfMonthsFor(portfolioTotalReturns, Objects.requireNonNull(period).trim());
    final V result = calculatePeriodForNumberOfMonths(months);
    return Pair.of(period, toUserFormat(result));
  }

  /**
   * returns scaled value when V is instance of BigDecimal, else returns input value
   *
   * @param value
   *          calculated value
   * @return scaled BigDecimal value
   */
  @SuppressWarnings(value = "unchecked")
  public V toUserFormat(final V value) {
    if (value instanceof BigDecimal) {
      return (V) toUserScale((BigDecimal) value);
    }
    return value;
  }

  /**
   * Checks if CIPSD is valid
   *
   * @return true - if valid
   */
  public boolean isSinceCustomIntervalPerformanceStartDateValid() {
    return cipsd != null
        && portfolioTotalReturns.firstKey().compareTo(cipsd) <= 0
        && portfolioTotalReturns.lastKey().compareTo(cipsd) >= 0;
  }

  /**
   * Appends CIPSD (custom since interval performance start date) (if valid) to the result
   *
   * @param resultSet
   *          result set (pre-calculated the rest of the periods)
   * @param periods
   *          requested periods
   */
  public void addSinceCustomIntervalPerformanceStartDate(final Set<Pair<String, V>> resultSet,
      final Set<String> periods) {
    if (isSinceCustomIntervalPerformanceStartDateValid()) {
      final V periodValue = calculatePeriodForCustomIntervalStartDate();
      resultSet.add(Pair.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), toUserFormat(periodValue)));
    } else if (cipsd != null || periods.contains(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name())) {
      resultSet.add(Pair.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), null));
    }
  }

  /**
   * Calculates CIPSD
   *
   * @return result of CIPSD
   */
  public V calculatePeriodForCustomIntervalStartDate() {
    final int months = getMonthsBetweenDates(cipsd, portfolioTotalReturns.lastKey(), firstDayOfMonth());
    return calculatePeriodForNumberOfMonths(months);
  }

  /**
   * Calculates number of months for a given period based on returns
   *
   * @param returns
   *          returns
   * @param period
   *          user entered period
   * @return number of months
   */
  public int getNumberOfMonthsFor(final NavigableMap<LocalDate, BigDecimal> returns, final String period) {
    if (isNumeric(period)) {
      return Integer.parseInt(period);
    } else if (YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
      return getNumberOfMonthsForYearToDate(returns);
    } else if (SINCE_PERFORMANCE_START_DATE.name().equalsIgnoreCase(period)) {
      return getNumberOfMonthsForSinceInception(returns);
    }
    throw new ReqValidationException(String.format("Period is not supported %s", period));
  }

  /**
   * Calculates number of months since the beginning of the year based on entered returns
   *
   * @param returns
   *          entered returns
   * @return number of months since the beginning of the year
   */
  public int getNumberOfMonthsForYearToDate(final Map<LocalDate, BigDecimal> returns) {
    final LocalDate endDate = returns.keySet().stream().max(LocalDate::compareTo).orElseThrow();
    return getMonthsBetweenDates(endDate, endDate, firstDayOfYear());
  }

  /**
   * Calculates number of months for SINCE_INCEPTION date
   *
   * @param returns
   *          given returns
   * @return number of months
   */
  public int getNumberOfMonthsForSinceInception(final NavigableMap<LocalDate, BigDecimal> returns) {
    final LocalDate endDate = returns.lastKey();
    final LocalDate startDate = returns.firstKey();
    return getMonthsBetweenDates(startDate, endDate, firstDayOfMonth());
  }

  /**
   * Calculates product based on entered period for a given map
   *
   * @param numberOfMonths
   *          number of months
   * @param returns
   *          given map
   * @return product
   */
  public BigDecimal calculateProductForPeriod(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    final NavigableMap<LocalDate, BigDecimal> monthsList = filterRequiredMonthsForPeriod(numberOfMonths, returns);
    return product(monthsList);
  }

  /**
   * Calculates product based on entered period for a given returns and benchmark map (portfolio returns)
   *
   * @param numberOfMonths
   *          number of months
   * @param returns
   *          given map
   * @return product
   */
  public List<BigDecimal> getBenchmarkValues(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    final NavigableMap<LocalDate, BigDecimal> monthsList = filterRequiredMonthsForPeriod(numberOfMonths,
        getPortfolioTotalReturns());
    return monthsList.keySet().stream().filter(returns::containsKey).map(returns::get).toList();
  }

  public NavigableMap<LocalDate, BigDecimal> filterRequiredMonthsForPeriod(final long numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    final LocalDate startOfPeriod = toLastDayOfMonth(returns.lastKey().minusMonths(numberOfMonths - 1));
    return returns.tailMap(startOfPeriod, true);
  }

  /**
   * Returns user entered periods or uses the default periods
   *
   * @param periods
   *          user entered periods
   * @return periods
   */
  public Set<String> getInitialPeriods(final Set<String> periods) {
    return CollectionUtils.isEmpty(periods) ? this.defaultPeriods : periods;
  }

  /**
   * Entry point to calculate period based calculations
   *
   * @param periods
   *          entered periods
   * @return final result
   */
  public T calculate(final Set<String> periods) {
    final Set<Pair<String, V>> periodsResult = calculatePeriods(periods);
    final T responseDTO = defineResponseType(periodsResult);
    populateBasicDetails(responseDTO);
    return responseDTO;
  }

  /**
   * Populates basic DTO fields that are common for all of the period based calculations
   *
   * @param responseDTO
   *          user defined period based response object
   */
  public void populateBasicDetails(final T responseDTO) {
    responseDTO.setCustomIpsd(cipsd);
    responseDTO.setPed(portfolioTotalReturns.lastKey());
    responseDTO.setPsd(portfolioTotalReturns.firstKey());
  }

  /**
   * Defines desired period based object with pre-init result
   *
   * @param result
   *          calculated periods (final result)
   * @return user created period based response object
   */
  public abstract T defineResponseType(final Set<Pair<String, V>> result);

  /**
   * returns period start date by number of months. Last date in returns map minus (numberOfMonths - 1)
   *
   * @param numberOfMonths
   *          number of month in period
   * @param returns
   *          portfolio or benchmark returns
   * @return period start date by number of months
   */
  public LocalDate getPeriodStartDate(final int numberOfMonths, final SortedMap<LocalDate, BigDecimal> returns) {
    return toLastDayOfMonth(returns.lastKey().minusMonths(numberOfMonths - ONE_MONTH));
  }

  /**
   * Returns sub map from startOfPeriodsDate to the end of the returns
   *
   * @param periodStartDate
   *          date when period starts
   * @param returns
   *          portfolio or benchmark returns
   * @return calculated map
   */
  public SortedMap<LocalDate, BigDecimal> getSubMapByPeriodStartDate(final LocalDate periodStartDate,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    return returns.subMap(periodStartDate, true, returns.lastKey(), true);
  }

  /**
   * calculates excess return. (totalReturns value subtract tBills value for that date)
   *
   * @param totalReturns
   *          portfolio or benchmark total returns
   * @return calculated excess returns
   */
  public static NavigableMap<LocalDate, BigDecimal> calculateExcessReturn(
      final SortedMap<LocalDate, BigDecimal> totalReturns,
      final NavigableMap<LocalDate, BigDecimal> tBills) {
    return totalReturns.entrySet()
        .stream()
        .filter(entry -> tBills.containsKey(entry.getKey()))
        .collect(toTreeMap(Map.Entry::getKey, e -> e.getValue().subtract(tBills.get(e.getKey()))));
  }

  /**
   * Returns mapped values from Set<Pair<String, BigDecimal>> to Set<TimeIntervalResult>.
   * <p>
   * TimeIntervalResult is the final view of the result.
   *
   * @param result
   *          final result.
   * @return result as TimeIntervalResult object.
   */
  public Set<TimeIntervalResult> formTimeIntervalResult(Set<Pair<String, BigDecimal>> result) {
    return result.stream().map(e -> new TimeIntervalResult(e.getKey(), e.getValue()))
        .collect(Collectors.toSet());
  }

  /**
   * overrides total return values. (newValue = ((oldValue*100)-100)/100)
   *
   * @param totalReturns
   * @return
   */
  public NavigableMap<LocalDate, BigDecimal> overrideTotalReturns(
      final NavigableMap<LocalDate, BigDecimal> totalReturns) {
    return totalReturns.entrySet()
        .stream().collect(toTreeMap(Map.Entry::getKey, e -> DecimalUtils.divide(e.getValue().multiply(HUNDRED).subtract(
            HUNDRED), HUNDRED)));
  }

  /**
   * restricts TBills to be within portfolioTotalReturns start and end date
   */
  public NavigableMap<LocalDate, BigDecimal> restrictTBillsRange(final NavigableMap<LocalDate, BigDecimal> tBills) {
    return restrictTBillsRange(tBills, this.portfolioTotalReturns);
  }

  public NavigableMap<LocalDate, BigDecimal> restrictTBillsRange(final NavigableMap<LocalDate, BigDecimal> tBills,
      final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns) {
    return tBills.subMap(portfolioTotalReturns.firstKey(), true, portfolioTotalReturns.lastKey(), true);
  }

  /**
   * calculates average arithmetic annualized return
   *
   * @param returns
   *          risk free rate or portfolio return values
   * @param periodStartDate
   *          start date of period
   * @param numberOfMonths
   *          number of month in period
   * @return annualized return
   */
  public BigDecimal calculateAverageArithmeticAnnualizedReturn(final NavigableMap<LocalDate, BigDecimal> returns,
      final LocalDate periodStartDate,
      final int numberOfMonths) {
    final SortedMap<LocalDate, BigDecimal> returnsInPeriod = getSubMapByPeriodStartDate(periodStartDate, returns);
    return divide(sum(returnsInPeriod), numberOfMonths).multiply(TWELVE);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final PeriodCalculationAbstract<?, ?> that = (PeriodCalculationAbstract<?, ?>) o;
    return Objects.equals(defaultPeriods, that.defaultPeriods) &&
        Objects.equals(portfolioTotalReturns, that.portfolioTotalReturns) &&
        Objects.equals(cipsd, that.cipsd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultPeriods, portfolioTotalReturns, cipsd);
  }
}
