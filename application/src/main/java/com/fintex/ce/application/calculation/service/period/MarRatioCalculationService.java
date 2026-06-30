package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.Growth10KHelper;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.MarRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.Notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.application.util.DecimalUtils.abs;
import static com.fintex.ce.model.domain.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static com.fintex.ce.model.domain.enumeration.Period.SINCE_PERFORMANCE_START_DATE;
import static com.fintex.ce.model.domain.enumeration.Period.YEAR_TO_DATE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DateTimeUtils.getMonthsBetweenDates;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Service
public class MarRatioCalculationService
    extends
      WeightedAverageWithCpedAbstractService<PeriodCommand, MarRatioResult> {

  private final MaxDrawdownService maxDrawdownService;

  public MarRatioCalculationService(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods,
      MaxDrawdownService maxDrawdownService) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, defaultPeriods);
    this.maxDrawdownService = maxDrawdownService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MAR_RATIO;
  }

  @Override
  public MarRatioResult perform(final PeriodCommand command) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO);
    final NavigableMap<LocalDate, BigDecimal> portfolioReturns = context.getWeightedAveragePortfolioReturns();
    final LocalDate cipsd = context.getCipsd();
    final var ttr = new TrailingTotalReturnsCalculation(context, defaultPeriods);
    // portfolioReturns is already in factor form, pass AS_IS to avoid double-scaling
    final NavigableMap<LocalDate, BigDecimal> growth10K = Growth10KHelper.compoundGrowth10K(
        portfolioReturns, ReturnFactorScale.AS_IS);

    final Set<String> initialPeriods = CollectionUtils.isEmpty(command.getPeriods())
        ? defaultPeriods
        : command.getPeriods();
    final Set<Pair<String, BigDecimal>> rawResults = new HashSet<>();

    initialPeriods.stream()
        .filter(p -> !SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name().equalsIgnoreCase(p))
        .forEach(p -> {
          final int months = getNumberOfMonthsFor(portfolioReturns, p.trim());
          rawResults.add(Pair.of(p, calculateMarRatioPeriod(months, portfolioReturns, growth10K, ttr)));
        });

    if (isCipsdValid(cipsd, portfolioReturns)) {
      final int months = getMonthsBetweenDates(cipsd, portfolioReturns.lastKey(), firstDayOfMonth());
      rawResults.add(Pair.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(),
          calculateMarRatioPeriod(months, portfolioReturns, growth10K, ttr)));
    } else if (cipsd != null || initialPeriods.contains(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name())) {
      rawResults.add(Pair.of(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), null));
    }

    final Set<Pair<String, BigDecimal>> periodsResult = rawResults.stream()
        .map(p -> Pair.of(p.getKey(), p.getValue() != null ? DecimalUtils.toUserScale(p.getValue()) : null))
        .collect(Collectors.toSet());

    final MarRatioResult result = buildResult(periodsResult);
    result.setCustomIntervalPerformanceStartDate(cipsd);
    result.setPerformanceEndDate(portfolioReturns.lastKey());
    result.setPerformanceStartDate(portfolioReturns.firstKey());
    addWarnings(result, periodsResult, portfolioReturns, cipsd);
    return result;
  }

  // Package-private for unit testing.

  BigDecimal calculateMarRatioPeriod(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final NavigableMap<LocalDate, BigDecimal> growth10K,
      final TrailingTotalReturnsCalculation ttr) {
    if (numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final BigDecimal trailingTRValue = ttr.calculatePeriodForNumberOfMonths(numberOfMonths);
    final MaxDrawdownEntry maxDrawdown = maxDrawdownService.calculateEntry(numberOfMonths, portfolioReturns, growth10K);
    if (Objects.isNull(trailingTRValue) || Objects.isNull(maxDrawdown) || Objects.isNull(maxDrawdown.value())
        || maxDrawdown.value().compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }
    return DecimalUtils.divide(trailingTRValue, abs(maxDrawdown.value()));
  }

  MarRatioResult buildResult(final Set<Pair<String, BigDecimal>> periodsResult) {
    final MarRatioResult result = new MarRatioResult();
    final Set<TimeIntervalResult> timeIntervals = periodsResult.stream()
        .map(e -> new TimeIntervalResult(e.getKey(), e.getValue()))
        .collect(Collectors.toSet());
    result.setMarRatio(timeIntervals);
    return result;
  }

  // Private helpers

  private void addWarnings(final MarRatioResult result,
      final Set<Pair<String, BigDecimal>> periodsResult,
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final LocalDate cipsd) {
    final int availableMonths = portfolioReturns.size();
    final List<Notification> warnings = new ArrayList<>(result.getWarnings());

    periodsResult.stream()
        .filter(pair -> pair.getValue() == null)
        .filter(pair -> !SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name().equalsIgnoreCase(pair.getKey().trim()))
        .filter(pair -> getNumberOfMonthsFor(portfolioReturns, pair.getKey().trim()) > availableMonths)
        .map(pair -> ErrorCode.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD.asNotification(pair.getKey().trim(),
            availableMonths))
        .forEach(warnings::add);

    final boolean sinceCipsdRequestedAndNull = periodsResult.stream()
        .anyMatch(pair -> SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name()
            .equalsIgnoreCase(pair.getKey().trim()) && pair.getValue() == null);
    if (cipsd != null && !portfolioReturns.isEmpty() && !isCipsdValid(cipsd, portfolioReturns)
        && sinceCipsdRequestedAndNull) {
      warnings.add(ErrorCode.CIPSD_OUTSIDE_DATA_RANGE.asNotification(
          cipsd, portfolioReturns.firstKey(), portfolioReturns.lastKey()));
    }

    result.setWarnings(warnings);
  }

  private int getNumberOfMonthsFor(final NavigableMap<LocalDate, BigDecimal> returns, final String period) {
    if (isNumeric(period)) {
      return Integer.parseInt(period);
    } else if (YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
      final LocalDate endDate = returns.keySet().stream().max(LocalDate::compareTo).orElseThrow();
      return getMonthsBetweenDates(endDate, endDate, firstDayOfYear());
    } else if (SINCE_PERFORMANCE_START_DATE.name().equalsIgnoreCase(period)) {
      return getMonthsBetweenDates(returns.firstKey(), returns.lastKey(), firstDayOfMonth());
    }
    throw ErrorCode.TIME_INTERVAL_PERIOD_NOT_ALLOWED.toException(period);
  }

  private boolean isCipsdValid(final LocalDate cipsd, final NavigableMap<LocalDate, BigDecimal> returns) {
    return cipsd != null
        && returns.firstKey().compareTo(cipsd) <= 0
        && returns.lastKey().compareTo(cipsd) >= 0;
  }
}
