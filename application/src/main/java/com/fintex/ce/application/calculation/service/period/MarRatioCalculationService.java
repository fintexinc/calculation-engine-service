package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.Growth10KHelper;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.risk.MarRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.error.Notification;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

import static com.fintex.ce.application.util.DecimalUtils.abs;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DateTimeUtils.getMonthsBetweenDates;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class MarRatioCalculationService
    extends
      WeightedAverageWithCpedAbstractService<PeriodCommand, MarRatioResult> {

  private final MaxDrawdownService maxDrawdownService;

  public MarRatioCalculationService(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      PeriodProperties periods,
      MaxDrawdownService maxDrawdownService) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, periods.getRiskCalculations());
    this.maxDrawdownService = maxDrawdownService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MAR_RATIO;
  }

  @Override
  public MarRatioResult perform(final PeriodCommand command,
      final PortfolioBenchmarkReturns returnsData) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO,
        returnsData);
    final NavigableMap<LocalDate, BigDecimal> portfolioReturns = context.getWeightedAveragePortfolioReturns();
    final LocalDate cipsd = context.getCipsd();
    final var ttr = TrailingTotalReturnsCalculation.mathOnly(context, defaultPeriods);
    // portfolioReturns is already in factor form, pass AS_IS to avoid double-scaling
    NavigableMap<LocalDate, BigDecimal> growth10K = Growth10KHelper.compoundGrowth10K(
        portfolioReturns, ReturnFactorScale.AS_IS);

    Set<TimePeriod> initialPeriods = CollectionUtils.isEmpty(command.getPeriods())
        ? defaultPeriods
        : command.getPeriods();
    Map<String, BigDecimal> rawResults = new LinkedHashMap<>();

    initialPeriods.stream()
        .filter(p -> p != TimePeriod.CIPSD)
        .forEach(p -> {
          int months = getNumberOfMonthsFor(portfolioReturns, p);
          rawResults.put(p.name(), calculateMarRatioPeriod(months, portfolioReturns, growth10K, ttr));
        });

    if (isCipsdValid(cipsd, portfolioReturns)) {
      int months = getMonthsBetweenDates(cipsd, portfolioReturns.lastKey(), firstDayOfMonth());
      rawResults.put(TimePeriod.CIPSD.name(),
          calculateMarRatioPeriod(months, portfolioReturns, growth10K, ttr));
    } else if (cipsd != null || initialPeriods.contains(TimePeriod.CIPSD)) {
      rawResults.put(TimePeriod.CIPSD.name(), null);
    }

    Map<String, BigDecimal> periodsResult = new LinkedHashMap<>();
    rawResults.forEach((period, value) -> periodsResult.put(period, value != null
        ? DecimalUtils.toUserScale(value)
        : null));

    MarRatioResult result = buildResult(periodsResult);
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
    BigDecimal trailingTRValue = ttr.calculatePeriodForNumberOfMonths(numberOfMonths);
    MaxDrawdownEntry maxDrawdown = maxDrawdownService.calculateEntry(numberOfMonths, portfolioReturns, growth10K);
    if (Objects.isNull(trailingTRValue) || Objects.isNull(maxDrawdown) || Objects.isNull(maxDrawdown.value())
        || maxDrawdown.value().compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }
    return DecimalUtils.divide(trailingTRValue, abs(maxDrawdown.value()));
  }

  MarRatioResult buildResult(final Map<String, BigDecimal> periodsResult) {
    MarRatioResult result = new MarRatioResult();
    result.setMarRatio(periodsResult);
    return result;
  }

  // Private helpers

  private void addWarnings(final MarRatioResult result,
      final Map<String, BigDecimal> periodsResult,
      final NavigableMap<LocalDate, BigDecimal> portfolioReturns,
      final LocalDate cipsd) {
    int availableMonths = portfolioReturns.size();
    List<Notification> warnings = new ArrayList<>(result.getWarnings());

    periodsResult.entrySet().stream()
        .filter(entry -> entry.getValue() == null)
        .filter(entry -> !TimePeriod.CIPSD.name().equals(entry.getKey()))
        .filter(entry -> getNumberOfMonthsFor(portfolioReturns, TimePeriod.valueOf(entry.getKey())) > availableMonths)
        .map(entry -> ErrorCode.INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD.asNotification(
            getNumberOfMonthsFor(portfolioReturns, TimePeriod.valueOf(entry.getKey())), availableMonths))
        .forEach(warnings::add);

    boolean sinceCipsdRequestedAndNull = periodsResult.entrySet().stream()
        .anyMatch(entry -> TimePeriod.CIPSD.name()
            .equalsIgnoreCase(entry.getKey()) && entry.getValue() == null);
    if (cipsd != null && !portfolioReturns.isEmpty() && !isCipsdValid(cipsd, portfolioReturns)
        && sinceCipsdRequestedAndNull) {
      warnings.add(ErrorCode.CIPSD_OUTSIDE_DATA_RANGE.asNotification(
          cipsd, portfolioReturns.firstKey(), portfolioReturns.lastKey()));
    }

    result.setWarnings(warnings);
  }

  private int getNumberOfMonthsFor(final NavigableMap<LocalDate, BigDecimal> returns, final TimePeriod period) {
    if (period.isFixedLength()) {
      return period.getMonths();
    } else if (period == TimePeriod.YTD) {
      LocalDate endDate = returns.keySet().stream().max(LocalDate::compareTo).orElseThrow();
      return getMonthsBetweenDates(endDate, endDate, firstDayOfYear());
    } else if (period == TimePeriod.SI) {
      return getMonthsBetweenDates(returns.firstKey(), returns.lastKey(), firstDayOfMonth());
    }
    throw ErrorCode.TIME_INTERVAL_PERIOD_NOT_ALLOWED.toException(period.name());
  }

  private boolean isCipsdValid(final LocalDate cipsd, final NavigableMap<LocalDate, BigDecimal> returns) {
    return cipsd != null
        && returns.firstKey().compareTo(cipsd) <= 0
        && returns.lastKey().compareTo(cipsd) >= 0;
  }
}
