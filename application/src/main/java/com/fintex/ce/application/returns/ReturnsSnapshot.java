package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;
import com.fintex.wm.commons.error.Severity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.application.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.model.error.ErrorCode.CPED_AFTER_PORTFOLIO_PED;
import static com.fintex.ce.model.error.ErrorCode.HOLDING_MISSING_CURRENCY_FROM_MIC;
import static com.fintex.ce.model.error.ErrorCode.HOLDING_PSD_OUT_OF_RANGE;
import static com.fintex.ce.model.error.ErrorParams.HOLDING_ID;

/**
 * Immutable snapshot of returns data at a single point in a calculation pipeline.
 *
 * <p>
 * Carries the holdings-to-currency mapping, the per-holding returns time series, the inferred performance window
 * (PSD/PED), the errors of severity ERROR accumulated by validators or processors so far, and the warnings (non-fatal
 * {@link Notification}s) emitted by FX conversion. Every transformation in the pipeline (cuts, FX conversion,
 * validations) returns a new {@code ReturnsSnapshot} instead of mutating an existing one — there is no setter and there
 * is no way to mutate the snapshot from outside.
 * </p>
 *
 * <p>
 * Construction defensively copies all input collections. The {@code returnsMap} keeps {@link TreeMap} as the value type
 * because downstream calculation components rely on its sorted contract; copies are made entry by entry so the snapshot
 * never shares mutable inner maps with its caller.
 * </p>
 *
 * <p>
 * Static factories {@link #forMonthlyReturns(Map)} and {@link #validateOnly(Map)} encapsulate the build pipeline (error
 * translation, currency extraction, performance-period inference, out-of-range trimming, fatal-error throw). Both
 * return a fully validated, immutable snapshot or throw a {@code CalculationsFailedException} on a non-allowed error.
 * </p>
 */
public record ReturnsSnapshot<T extends ReturnsData>(
    Map<PortfolioHolding, Currency> holdingCurrencyMap,
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap,
    LocalDate performanceStartDate,
    LocalDate performanceEndDate,
    List<BasePceException> errors,
    List<Notification> warnings) implements PipelineResult<T> {

  public ReturnsSnapshot {
    holdingCurrencyMap = holdingCurrencyMap == null
        ? Map.of()
        : Collections.unmodifiableMap(new LinkedHashMap<>(holdingCurrencyMap));
    returnsMap = deepCopyReturns(returnsMap);
    errors = errors == null ? List.of() : List.copyOf(errors);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }

  public ReturnsSnapshot(
      Map<PortfolioHolding, Currency> holdingCurrencyMap,
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap,
      LocalDate performanceStartDate,
      LocalDate performanceEndDate,
      List<BasePceException> errors) {
    this(holdingCurrencyMap, returnsMap, performanceStartDate, performanceEndDate, errors, List.of());
  }

  public static <T extends ReturnsData> ReturnsSnapshot<T> empty() {
    return new ReturnsSnapshot<>(Map.of(), Map.of(), null, null, List.of(), List.of());
  }

  public static <T extends ReturnsData> ReturnsSnapshot<T> forMonthlyReturns(Map<PortfolioHolding, T> sourceData) {
    return build(sourceData, ReturnsData::getErrors, true);
  }

  public static <T extends ReturnsData> ReturnsSnapshot<T> validateOnly(Map<PortfolioHolding, T> sourceData) {
    return build(sourceData,
        returns -> returns.hasMonthlyReturnsErrors() ? returns.getOnlyMonthlyReturnsErrors() : List.of(),
        false);
  }

  public ReturnsSnapshot<T> withReturnsMap(Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> updatedReturnsMap) {
    return new ReturnsSnapshot<>(holdingCurrencyMap, updatedReturnsMap, performanceStartDate, performanceEndDate,
        errors, warnings);
  }

  public ReturnsSnapshot<T> withHoldingCurrencyMap(Map<PortfolioHolding, Currency> updatedHoldingCurrencyMap) {
    return new ReturnsSnapshot<>(updatedHoldingCurrencyMap, returnsMap, performanceStartDate, performanceEndDate,
        errors, warnings);
  }

  public ReturnsSnapshot<T> withPeriod(LocalDate updatedPerformanceStartDate, LocalDate updatedPerformanceEndDate) {
    return new ReturnsSnapshot<>(holdingCurrencyMap, returnsMap, updatedPerformanceStartDate,
        updatedPerformanceEndDate, errors, warnings);
  }

  public ReturnsSnapshot<T> withErrors(List<BasePceException> updatedErrors) {
    return new ReturnsSnapshot<>(holdingCurrencyMap, returnsMap, performanceStartDate, performanceEndDate,
        updatedErrors, warnings);
  }

  public ReturnsSnapshot<T> withAddedErrors(List<? extends BasePceException> additionalErrors) {
    if (additionalErrors == null || additionalErrors.isEmpty()) {
      return this;
    }
    var combined = new ArrayList<BasePceException>(errors.size() + additionalErrors.size());
    combined.addAll(errors);
    combined.addAll(additionalErrors);
    return withErrors(combined);
  }

  public ReturnsSnapshot<T> withAddedWarnings(List<Notification> additionalWarnings) {
    if (additionalWarnings == null || additionalWarnings.isEmpty()) {
      return this;
    }
    var combined = new ArrayList<Notification>(warnings.size() + additionalWarnings.size());
    combined.addAll(warnings);
    combined.addAll(additionalWarnings);
    return new ReturnsSnapshot<>(holdingCurrencyMap, returnsMap, performanceStartDate, performanceEndDate, errors,
        combined);
  }

  /**
   * Returns a snapshot whose returns map is trimmed at {@code endDate} and whose performance window has been recomputed
   * accordingly. Returns this snapshot unchanged when {@code endDate} is null or already matches the current PED.
   */
  public ReturnsSnapshot<T> trimToEnd(LocalDate endDate) {
    if (endDate == null || endDate.equals(performanceEndDate)) {
      return this;
    }
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByEndDate(
        returnsMap, endDate);
    return withReturnsMap(trimmed)
        .withPeriod(PerformancePeriodCalculator.findPerformanceStartDate(trimmed),
            PerformancePeriodCalculator.findPerformanceEndDate(trimmed));
  }

  /**
   * Returns a snapshot whose returns map is trimmed at {@code startDate} and whose performance window has been
   * recomputed accordingly. Returns this snapshot unchanged when {@code startDate} is null.
   */
  public ReturnsSnapshot<T> trimToStart(LocalDate startDate) {
    if (startDate == null) {
      return this;
    }
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByStartDate(
        returnsMap, startDate);
    return withReturnsMap(trimmed)
        .withPeriod(PerformancePeriodCalculator.findPerformanceStartDate(trimmed),
            PerformancePeriodCalculator.findPerformanceEndDate(trimmed));
  }

  public ReturnsSnapshot<T> trimToRange(LocalDate startDate, LocalDate endDate) {
    return trimToEnd(endDate).trimToStart(startDate);
  }

  public List<Notification> getErrorsAsWarnings() {
    Stream<Notification> errorsAsNotifications = errors.stream()
        .map(ReturnsSnapshot::toWarningNotification);
    return Stream.concat(warnings.stream(), errorsAsNotifications).toList();
  }

  private static Notification toWarningNotification(BasePceException error) {
    Notification notification = error.getErrorCode()
        .toNotification(error.getId(), error.getFieldName(), error.getMessage(), error.getMetadata());
    if (error.getErrorCode() != CPED_AFTER_PORTFOLIO_PED) {
      return notification;
    }
    return Notification.builder()
        .category(notification.getCategory())
        .code(notification.getCode())
        .message(notification.getMessage())
        .description(notification.getDescription())
        .action(notification.getAction())
        .metadata(notification.getMetadata())
        .uuid(notification.getUuid())
        .timestamp(notification.getTimestamp())
        .severity(Severity.WARNING)
        .fieldName(notification.getFieldName())
        .build();
  }

  private static <T extends ReturnsData> ReturnsSnapshot<T> build(Map<PortfolioHolding, T> sourceData,
      Function<ReturnsData, List<Notification>> errorSelector, boolean populateCurrencyMap) {
    var errors = new ArrayList<BasePceException>();
    sourceData.values().forEach(returns -> errors.addAll(translate(errorSelector.apply(returns))));

    Map<PortfolioHolding, Currency> holdingCurrencyMap = populateCurrencyMap
        ? extractCurrencies(sourceData, errors)
        : Map.of();
    var returnsMap = new HashMap<>(extractReturnsMap(sourceData));
    LocalDate psd = PerformancePeriodCalculator.findPerformanceStartDate(returnsMap);
    LocalDate ped = PerformancePeriodCalculator.findPerformanceEndDate(returnsMap);

    TrimmedWindow window = trimHoldingsOutsideWindow(returnsMap, psd, ped, errors);
    var snapshot = new ReturnsSnapshot<T>(holdingCurrencyMap, window.returnsMap(), window.psd(), window.ped(), errors);
    return ReturnsErrorPolicy.throwIfFatal(snapshot);
  }

  private static <T extends ReturnsData> Map<PortfolioHolding, Currency> extractCurrencies(
      Map<PortfolioHolding, T> sourceData, List<BasePceException> errorsSink) {
    sourceData.entrySet().stream()
        .filter(entry -> Currency.fromValueOrNull(entry.getValue().getCurrency()) == null)
        .forEach(entry -> errorsSink.add(HOLDING_MISSING_CURRENCY_FROM_MIC.toExceptionForHolding(entry.getKey())));
    return new LinkedHashMap<>(extractHoldingCurrencyMap(sourceData));
  }

  private static TrimmedWindow trimHoldingsOutsideWindow(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap, LocalDate psd, LocalDate ped,
      List<BasePceException> errorsSink) {
    while (psd != null && ped != null && psd.isAfter(ped)) {
      LocalDate psdSnapshot = psd;
      List<PortfolioHolding> holdingsAtPsd = returnsMap.entrySet().stream()
          .filter(entry -> psdSnapshot.equals(entry.getValue().firstKey()))
          .map(Map.Entry::getKey)
          .toList();
      if (holdingsAtPsd.isEmpty()) {
        break;
      }
      holdingsAtPsd.forEach(holding -> {
        errorsSink.add(HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(holding));
        returnsMap.remove(holding);
      });
      psd = PerformancePeriodCalculator.findPerformanceStartDate(returnsMap);
      ped = PerformancePeriodCalculator.findPerformanceEndDate(returnsMap);
    }
    return new TrimmedWindow(returnsMap, psd, ped);
  }

  private static List<? extends BasePceException> translate(Collection<Notification> notifications) {
    if (notifications == null || notifications.isEmpty()) {
      return List.of();
    }
    return notifications.stream()
        .map(notification -> ErrorCode.fromCode(notification.getCode()).toExceptionForId(holdingIdOf(notification)))
        .toList();
  }

  private static String holdingIdOf(Notification notification) {
    Map<String, Object> metadata = notification.getMetadata();
    Object holdingId = metadata == null ? null : metadata.get(HOLDING_ID);
    return holdingId != null ? holdingId.toString() : notification.getUuid();
  }

  private static Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> deepCopyReturns(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source) {
    if (source == null || source.isEmpty()) {
      return Map.of();
    }
    var copy = new HashMap<PortfolioHolding, TreeMap<LocalDate, BigDecimal>>(source.size());
    source.forEach((holding, series) -> copy.put(holding, series == null ? new TreeMap<>() : new TreeMap<>(series)));
    return Collections.unmodifiableMap(copy);
  }

  private static Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> extractReturnsMap(
      Map<PortfolioHolding, ? extends ReturnsData> sourceData) {
    return sourceData.entrySet()
        .stream()
        .filter(entry -> entry.getValue() != null && entry.getValue().getReturns() != null)
        .collect(toMap(Map.Entry::getKey,
            entry -> entry.getValue().getReturns().entrySet().stream().collect(toTreeMap(Map.Entry::getKey,
                Map.Entry::getValue))));
  }

  private static Map<PortfolioHolding, Currency> extractHoldingCurrencyMap(
      Map<PortfolioHolding, ? extends ReturnsData> sourceData) {
    return sourceData.entrySet()
        .stream()
        .filter(entry -> Currency.fromValueOrNull(entry.getValue().getCurrency()) != null)
        .collect(toMap(Map.Entry::getKey, entry -> Currency.fromValueOrNull(entry.getValue().getCurrency())));
  }

  private record TrimmedWindow(
      Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap,
      LocalDate psd,
      LocalDate ped) {
  }
}
