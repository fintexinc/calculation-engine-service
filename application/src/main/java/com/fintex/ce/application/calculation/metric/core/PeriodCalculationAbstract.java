package com.fintex.ce.application.calculation.metric.core;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.error.Notification;

import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import lombok.Getter;

import static com.fintex.ce.application.util.CalculationUtils.product;
import static com.fintex.ce.application.util.CalculationUtils.sum;
import static com.fintex.ce.application.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DateTimeUtils.getMonthsBetweenDates;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.CIPSD;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.SI;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.YTD;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

/**
 * V - type of calculated value. e.g If calculation returns BigDecimal value for period, then V -> BigDecimal.
 */
@Getter
public abstract class PeriodCalculationAbstract<T extends PeriodResult, V> {

  private static final long ONE_MONTH = 1;

  public final Set<TimePeriod> defaultPeriods;
  public NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns;
  public LocalDate cipsd;

  protected PeriodCalculationAbstract(PeriodCalculationInput input,
      Set<TimePeriod> defaultPeriods) {
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
  public abstract V calculatePeriodForNumberOfMonths(int numberOfMonths);

  /**
   * Calculates periods
   *
   * @param periods
   *          entered periods
   * @return calculated periods
   */
  public Set<Pair<String, V>> calculatePeriods(Set<TimePeriod> periods) {
    Set<TimePeriod> initialPeriods = getInitialPeriods(periods);
    Set<Pair<String, V>> result = initialPeriods.stream()
        .filter(period -> period != CIPSD)
        .map(this::calculateForPeriod).collect(Collectors.toSet());
    addSinceCustomIntervalPerformanceStartDate(result, initialPeriods);
    return result;
  }

  /**
   * Calculates a single period.
   *
   * <p>
   * The returned pair keeps the period as its name rather than as the enum: this is the one point where the typed
   * vocabulary meets the response DTOs, which are string-keyed throughout ({@link TimeIntervalResult}). The name is
   * what the enum serializes as anyway, so the wire format is the same either way.
   *
   * @param period
   *          period
   * @return result for a single period
   */
  public Pair<String, V> calculateForPeriod(TimePeriod period) {
    int months = getNumberOfMonthsFor(portfolioTotalReturns, Objects.requireNonNull(period));
    V result = calculatePeriodForNumberOfMonths(months);
    return Pair.of(period.name(), toUserFormat(result));
  }

  /**
   * returns scaled value when V is instance of BigDecimal, else returns input value
   *
   * @param value
   *          calculated value
   * @return scaled BigDecimal value
   */
  @SuppressWarnings(value = "unchecked")
  public V toUserFormat(V value) {
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
  public void addSinceCustomIntervalPerformanceStartDate(Set<Pair<String, V>> resultSet,
      Set<TimePeriod> periods) {
    if (isSinceCustomIntervalPerformanceStartDateValid()) {
      V periodValue = calculatePeriodForCustomIntervalStartDate();
      resultSet.add(Pair.of(CIPSD.name(), toUserFormat(periodValue)));
    } else if (cipsd != null || periods.contains(CIPSD)) {
      resultSet.add(Pair.of(CIPSD.name(), null));
    }
  }

  /**
   * Calculates CIPSD
   *
   * @return result of CIPSD
   */
  public V calculatePeriodForCustomIntervalStartDate() {
    int months = getMonthsBetweenDates(cipsd, portfolioTotalReturns.lastKey(), firstDayOfMonth());
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
  public int getNumberOfMonthsFor(NavigableMap<LocalDate, BigDecimal> returns, TimePeriod period) {
    if (period.isFixedLength()) {
      return period.getMonths();
    } else if (period == YTD) {
      return getNumberOfMonthsForYearToDate(returns);
    } else if (period == SI) {
      return getNumberOfMonthsForSinceInception(returns);
    }
    throw ErrorCode.TIME_INTERVAL_PERIOD_NOT_ALLOWED.toException(period.name());
  }

  /**
   * Calculates number of months since the beginning of the year based on entered returns
   *
   * @param returns
   *          entered returns
   * @return number of months since the beginning of the year
   */
  public int getNumberOfMonthsForYearToDate(Map<LocalDate, BigDecimal> returns) {
    LocalDate endDate = returns.keySet().stream().max(LocalDate::compareTo).orElseThrow();
    return getMonthsBetweenDates(endDate, endDate, firstDayOfYear());
  }

  /**
   * Calculates number of months for SINCE_INCEPTION date
   *
   * @param returns
   *          given returns
   * @return number of months
   */
  public int getNumberOfMonthsForSinceInception(NavigableMap<LocalDate, BigDecimal> returns) {
    LocalDate endDate = returns.lastKey();
    LocalDate startDate = returns.firstKey();
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
  public BigDecimal calculateProductForPeriod(int numberOfMonths,
      NavigableMap<LocalDate, BigDecimal> returns) {
    NavigableMap<LocalDate, BigDecimal> monthsList = filterRequiredMonthsForPeriod(numberOfMonths, returns);
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
  public List<BigDecimal> getBenchmarkValues(int numberOfMonths,
      NavigableMap<LocalDate, BigDecimal> returns) {
    NavigableMap<LocalDate, BigDecimal> monthsList = filterRequiredMonthsForPeriod(numberOfMonths,
        getPortfolioTotalReturns());
    return monthsList.keySet().stream().filter(returns::containsKey).map(returns::get).toList();
  }

  public NavigableMap<LocalDate, BigDecimal> filterRequiredMonthsForPeriod(long numberOfMonths,
      NavigableMap<LocalDate, BigDecimal> returns) {
    LocalDate startOfPeriod = toLastDayOfMonth(returns.lastKey().minusMonths(numberOfMonths - 1));
    return returns.tailMap(startOfPeriod, true);
  }

  /**
   * Returns user entered periods or uses the default periods
   *
   * @param periods
   *          user entered periods
   * @return periods
   */
  public Set<TimePeriod> getInitialPeriods(Set<TimePeriod> periods) {
    return CollectionUtils.isEmpty(periods) ? this.defaultPeriods : periods;
  }

  /**
   * Entry point to calculate period based calculations
   *
   * @param periods
   *          entered periods
   * @return final result
   */
  public T calculate(Set<TimePeriod> periods) {
    Set<Pair<String, V>> periodsResult = calculatePeriods(periods);
    T result = defineResponseType(periodsResult);
    populateBasicDetails(result);
    addInsufficientDataWarnings(result, periodsResult);
    return result;
  }

  /**
   * Appends a {@link ErrorCode#INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD} warning for every period whose value is
   * {@code null} because the requested number of months exceeds the available monthly returns. Symbolic periods (e.g.
   * YEAR_TO_DATE, SINCE_PERFORMANCE_START_DATE) are resolved via {@link #getNumberOfMonthsFor} so they get the same
   * treatment as numeric ones. {@code SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE} is handled separately — its
   * validity is gated by CIPSD position rather than month count, and it gets a dedicated
   * {@link ErrorCode#CIPSD_OUTSIDE_DATA_RANGE} warning so callers can tell why the value is null. Lets API consumers
   * distinguish "no result, not enough data" from a generic null without overriding the spec's null contract.
   */
  public void addInsufficientDataWarnings(T result, Set<Pair<String, V>> periodsResult) {
    int availableMonths = availableMonths();
    List<Notification> warnings = new ArrayList<>(result.getWarnings());
    periodsResult.stream()
        .filter(pair -> pair.getValue() == null)
        .filter(pair -> !CIPSD.name().equals(pair.getKey()))
        .filter(pair -> requiresInsufficientDataWarning(pair.getKey(), availableMonths))
        .map(pair -> ErrorCode.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD.asNotification(pair.getKey(),
            availableMonths))
        .forEach(warnings::add);

    // CIPSD lies outside [firstKey, lastKey] → SINCE_CIPSD is silently null. Without this warning the caller
    // has no signal whether the cause was an out-of-range CIPSD vs missing data vs anything else.
    boolean sinceCipsdRequestedAndNull = periodsResult.stream().anyMatch(
        pair -> CIPSD.name().equals(pair.getKey()) && pair.getValue() == null);
    if (cipsd != null && !portfolioTotalReturns.isEmpty()
        && !isSinceCustomIntervalPerformanceStartDateValid()
        && sinceCipsdRequestedAndNull) {
      warnings.add(ErrorCode.CIPSD_OUTSIDE_DATA_RANGE.asNotification(
          cipsd, portfolioTotalReturns.firstKey(), portfolioTotalReturns.lastKey()));
    }

    periodsResult.stream()
        .filter(pair -> pair.getValue() == null)
        .filter(pair -> requiresUnavailablePeriodWarning(pair.getKey(), availableMonths))
        .map(pair -> unavailablePeriodWarning(pair.getKey()))
        .forEach(warnings::add);

    result.setWarnings(warnings);
  }

  /**
   * Number of months a metric can actually compute against. Subclasses that depend on additional series (benchmark,
   * excess returns, T-Bills, etc.) must override this so the warning fires whenever the calculation will return
   * {@code null} due to insufficient data on any input — not just the portfolio side.
   */
  public int availableMonths() {
    return portfolioTotalReturns.size();
  }

  /**
   * Populates basic fields that are common for all of the period based calculations
   *
   * @param result
   *          period-based result object to populate
   */
  public void populateBasicDetails(T result) {
    result.setCustomIntervalPerformanceStartDate(cipsd);
    result.setPerformanceEndDate(portfolioTotalReturns.lastKey());
    result.setPerformanceStartDate(portfolioTotalReturns.firstKey());
  }

  /**
   * Builds the metric-specific result from the per-period calculated values.
   *
   * @param periodValues
   *          calculated value per requested period
   * @return populated period-based result object
   */
  public abstract T defineResponseType(Set<Pair<String, V>> periodValues);

  /**
   * returns period start date by number of months. Last date in returns map minus (numberOfMonths - 1)
   *
   * @param numberOfMonths
   *          number of month in period
   * @param returns
   *          portfolio or benchmark returns
   * @return period start date by number of months
   */
  public LocalDate getPeriodStartDate(int numberOfMonths, SortedMap<LocalDate, BigDecimal> returns) {
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
  public SortedMap<LocalDate, BigDecimal> getSubMapByPeriodStartDate(LocalDate periodStartDate,
      NavigableMap<LocalDate, BigDecimal> returns) {
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
      SortedMap<LocalDate, BigDecimal> totalReturns,
      NavigableMap<LocalDate, BigDecimal> tBills) {
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
   *          map of total return values to transform
   * @return new map with transformed values
   */
  public NavigableMap<LocalDate, BigDecimal> overrideTotalReturns(
      NavigableMap<LocalDate, BigDecimal> totalReturns) {
    return totalReturns.entrySet()
        .stream().collect(toTreeMap(Map.Entry::getKey, e -> DecimalUtils.divide(e.getValue().multiply(HUNDRED).subtract(
            HUNDRED), HUNDRED)));
  }

  /**
   * restricts TBills to be within portfolioTotalReturns start and end date
   */
  public NavigableMap<LocalDate, BigDecimal> restrictTBillsRange(NavigableMap<LocalDate, BigDecimal> tBills) {
    return restrictTBillsRange(tBills, this.portfolioTotalReturns);
  }

  public NavigableMap<LocalDate, BigDecimal> restrictTBillsRange(NavigableMap<LocalDate, BigDecimal> tBills,
      NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns) {
    if (CollectionUtils.isEmpty(portfolioTotalReturns)) {
      return Collections.emptyNavigableMap();
    }
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
  public BigDecimal calculateAverageArithmeticAnnualizedReturn(NavigableMap<LocalDate, BigDecimal> returns,
      LocalDate periodStartDate,
      int numberOfMonths) {
    SortedMap<LocalDate, BigDecimal> returnsInPeriod = getSubMapByPeriodStartDate(periodStartDate, returns);
    return divide(sum(returnsInPeriod), numberOfMonths).multiply(TWELVE);
  }

  /**
   * Returns true when a null period value should trigger an {@link ErrorCode#INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD}
   * warning. The default checks whether the requested month count exceeds available months. Subclasses that have
   * additional null-return paths (e.g. degenerate input data) should override and call {@code super} first.
   */
  protected boolean requiresInsufficientDataWarning(final String period, final int availableMonths) {
    return getNumberOfMonthsFor(portfolioTotalReturns, TimePeriod.fromJson(period)) > availableMonths;
  }

  /**
   * Returns true when a null period value is not explained by the standard insufficient-data or invalid-CIPSD warnings.
   * This covers metric-specific undefined cases such as zero denominators, zero variance, empty qualifying windows, or
   * alignment gaps handled internally by a metric.
   */
  protected boolean requiresUnavailablePeriodWarning(final String period, final int availableMonths) {
    if (CIPSD.name().equals(period)) {
      return cipsd != null && isSinceCustomIntervalPerformanceStartDateValid();
    }
    return !requiresInsufficientDataWarning(period, availableMonths);
  }

  protected Notification unavailablePeriodWarning(final String period) {
    return ErrorCode.PERIOD_RESULT_NOT_AVAILABLE.asNotification(period, getClass().getSimpleName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PeriodCalculationAbstract<?, ?> that = (PeriodCalculationAbstract<?, ?>) o;
    return Objects.equals(defaultPeriods, that.defaultPeriods) &&
        Objects.equals(portfolioTotalReturns, that.portfolioTotalReturns) &&
        Objects.equals(cipsd, that.cipsd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultPeriods, portfolioTotalReturns, cipsd);
  }
}
