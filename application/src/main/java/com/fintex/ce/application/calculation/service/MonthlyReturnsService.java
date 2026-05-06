package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.FxContext;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.PerformancePeriodCalculator;
import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ReturnsErrorPolicy;
import com.fintex.ce.application.returns.ReturnsRole;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.processor.ReturnsProcessor;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

import static com.fintex.ce.application.returns.ProcessingCase.BENCHMARK_PRE_PSD_TRIM;
import static com.fintex.ce.application.returns.ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPED_ONLY;
import static com.fintex.ce.application.returns.ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED;
import static com.fintex.ce.application.returns.ProcessingCase.PORTFOLIO_PRE_PSD_TRIM;
import static com.fintex.ce.application.returns.ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPED_ONLY;
import static com.fintex.ce.application.returns.ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED;

/**
 * Application-layer entry point for monthly-returns retrieval and weighted-average calculation.
 *
 * <p>
 * Delegates construction to {@link ReturnsSnapshot}'s static factories, applies per-case processor pipelines composed
 * from the injected {@code List<ReturnsProcessor>}, and emits a {@link WeightedAverageResult} bundling the time-series
 * output with the post-pipeline snapshot. Stateless helpers ({@link PerformancePeriodCalculator},
 * {@link ReturnsErrorPolicy}) are used as utility classes — no extra beans.
 * </p>
 */
@Slf4j
@Service
public class MonthlyReturnsService {

  private final SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher;
  private final FxRateService fxRateService;
  private final MonthlyReturnsGenerator monthlyReturnsGenerator;
  private final WeightedAverageComponent weightedAverageComponent;
  private final Map<ProcessingCase, List<ReturnsProcessor>> pipelinesByCase;

  public MonthlyReturnsService(SecurityDataFetcher<HoldingMonthlyReturns> monthlyReturnsSecurityDataFetcher,
      FxRateService fxRateService,
      MonthlyReturnsGenerator monthlyReturnsGenerator,
      WeightedAverageComponent weightedAverageComponent,
      List<ReturnsProcessor> processors) {
    this.monthlyReturnsSecurityDataFetcher = monthlyReturnsSecurityDataFetcher;
    this.fxRateService = fxRateService;
    this.monthlyReturnsGenerator = monthlyReturnsGenerator;
    this.weightedAverageComponent = weightedAverageComponent;
    this.pipelinesByCase = buildPipelines(processors);
  }

  public ReturnsSnapshot<HoldingMonthlyReturns> getMonthlyReturns(List<PortfolioHolding> holdings) {
    Map<PortfolioHolding, HoldingMonthlyReturns> sourceData = new HashMap<>(
        monthlyReturnsSecurityDataFetcher.fetch(holdings, List.of()));
    sourceData.putAll(monthlyReturnsGenerator.generateGicMonthlyReturns(holdings));
    validateMonthlyReturnsPresent(holdings, sourceData);
    return ReturnsSnapshot.forMonthlyReturns(sourceData);
  }

  public ReturnsSnapshot<HoldingMonthlyReturns> getMonthlyReturns(
      Map<PortfolioHolding, HoldingMonthlyReturns> sourceData) {
    return ReturnsSnapshot.forMonthlyReturns(sourceData);
  }

  public ReturnsSnapshot<HoldingMonthlyReturns> getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(
      List<PortfolioHolding> holdings) {
    Map<PortfolioHolding, HoldingMonthlyReturns> sourceData = monthlyReturnsSecurityDataFetcher.fetch(holdings,
        List.of());
    return ReturnsSnapshot.validateOnly(sourceData);
  }

  public MonthlyReturnsContext<HoldingMonthlyReturns> getPortfolioMonthlyReturns(List<PortfolioHolding> holdings,
      Currency currency) {
    return buildContext(holdings, currency, ReturnsRole.PORTFOLIO);
  }

  public MonthlyReturnsContext<HoldingMonthlyReturns> getBenchmarkMonthlyReturns(List<PortfolioHolding> holdings,
      Currency currency) {
    return buildContext(holdings, currency, ReturnsRole.BENCHMARK);
  }

  public WeightedAverageResult<HoldingMonthlyReturns> calculateWeightedAverageWithCpsdAndCped(
      MonthlyReturnsContext<HoldingMonthlyReturns> context, LocalDate cpsd, LocalDate cped,
      ReturnFactorScale returnFactorScale) {
    ProcessingCase processingCase = context.role() == ReturnsRole.PORTFOLIO
        ? PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED
        : BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED;
    return runWeightedAveragePipeline(context, ProcessingContext.of(cpsd, cped, context.fxContext()),
        processingCase, returnFactorScale);
  }

  public WeightedAverageResult<HoldingMonthlyReturns> calculateWeightedAverageWithCped(
      MonthlyReturnsContext<HoldingMonthlyReturns> context, LocalDate cped, ReturnFactorScale returnFactorScale) {
    ProcessingCase processingCase = context.role() == ReturnsRole.PORTFOLIO
        ? PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPED_ONLY
        : BENCHMARK_WEIGHTED_AVERAGE_WITH_CPED_ONLY;
    return runWeightedAveragePipeline(context, ProcessingContext.of(null, cped, context.fxContext()),
        processingCase, returnFactorScale);
  }

  public <T extends ReturnsData> ReturnsSnapshot<T> applyValidateCutAndFx(MonthlyReturnsContext<T> context,
      LocalDate cped) {
    ProcessingCase processingCase = context.role() == ReturnsRole.PORTFOLIO
        ? PORTFOLIO_PRE_PSD_TRIM
        : BENCHMARK_PRE_PSD_TRIM;
    ReturnsSnapshot<T> processed = applyPipeline(context.snapshot(),
        ProcessingContext.of(null, cped, context.fxContext()), processingCase);
    return ReturnsErrorPolicy.throwIfFatal(processed);
  }

  public <T extends ReturnsData> NavigableMap<LocalDate, BigDecimal> calculateWeightedAverageAfterPsdTrim(
      ReturnsSnapshot<T> snapshot, ReturnFactorScale returnFactorScale) {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByStartDate(
        snapshot.returnsMap(), snapshot.performanceStartDate());
    return weightedAverageComponent.calculateWeightedAverage(trimmed, returnFactorScale);
  }

  public <T extends ReturnsData> LocalDate commonPerformanceEndDate(MonthlyReturnsContext<T> first,
      MonthlyReturnsContext<T> second) {
    return earlierOf(first.snapshot().performanceEndDate(), second.snapshot().performanceEndDate());
  }

  public <T extends ReturnsData> MonthlyReturnsContext<T> trimContextToEnd(MonthlyReturnsContext<T> context,
      LocalDate endDate) {
    return context.withSnapshot(trimSnapshotToEnd(context.snapshot(), endDate));
  }

  /**
   * Per spec, every holding must have monthly returns for the calculation to proceed. Distinguishes two failure modes:
   * <ul>
   * <li>holding entirely absent from the Security Master response → {@link ErrorCode#SECURITY_NOT_FOUND_IN_SM} (the
   * security identifier is unknown to the data provider)</li>
   * <li>holding present but with no monthly returns → {@link ErrorCode#MISSING_MONTHLY_RETURNS} (security exists but
   * its return history is empty)</li>
   * </ul>
   * Holdings of types in {@link FilterUtils#NOT_SENT_TO_SM_TYPES} (CASH, GIC) are excluded from both checks: they are
   * intentionally never sent to Security Master, so finding them missing from the response is expected. GIC entries
   * arrive via {@code MonthlyReturnsGenerator}; CASH carries no returns and gets zero weight downstream. Throws on the
   * first offending holding so the response carries its identifier.
   */
  private void validateMonthlyReturnsPresent(List<PortfolioHolding> holdings,
      Map<PortfolioHolding, HoldingMonthlyReturns> sourceData) {
    List<PortfolioHolding> sentToSm = holdings.stream()
        .filter(holding -> {
          FinancialInstrumentType type = holding.getHoldingType();
          return type == null || !FilterUtils.NOT_SENT_TO_SM_TYPES.contains(type);
        })
        .toList();
    sentToSm.stream()
        .filter(holding -> !sourceData.containsKey(holding))
        .findFirst()
        .ifPresent(holding -> {
          throw ErrorCode.SECURITY_NOT_FOUND_IN_SM.toExceptionForHolding(holding, holding.getIdsString());
        });
    sentToSm.stream()
        .filter(holding -> isReturnsEmpty(sourceData.get(holding)))
        .findFirst()
        .ifPresent(holding -> {
          throw ErrorCode.MISSING_MONTHLY_RETURNS.toExceptionForHolding(holding);
        });
  }

  private boolean isReturnsEmpty(HoldingMonthlyReturns holdingReturns) {
    return CollectionUtils.isEmpty(holdingReturns.getReturns());
  }

  private MonthlyReturnsContext<HoldingMonthlyReturns> buildContext(List<PortfolioHolding> holdings, Currency currency,
      ReturnsRole role) {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = getMonthlyReturns(holdings);
    FxContext fxContext = buildFxContext(snapshot, currency);
    return new MonthlyReturnsContext<>(snapshot, fxContext, role);
  }

  private FxContext buildFxContext(ReturnsSnapshot<HoldingMonthlyReturns> snapshot, Currency targetCurrency) {
    if (targetCurrency == null) {
      return FxContext.empty();
    }
    log.debug("PortfolioHolding currencies: {}, target: {}", snapshot.holdingCurrencyMap().values(), targetCurrency);
    Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> rates = fxRateService.rates(
        snapshot.holdingCurrencyMap(), targetCurrency,
        new DateRange(snapshot.performanceStartDate(), snapshot.performanceEndDate()));
    return new FxContext(rates, targetCurrency);
  }

  private <T extends ReturnsData> WeightedAverageResult<T> runWeightedAveragePipeline(
      MonthlyReturnsContext<T> context, ProcessingContext processingContext, ProcessingCase processingCase,
      ReturnFactorScale returnFactorScale) {
    ReturnsSnapshot<T> processed = applyPipeline(context.snapshot(), processingContext, processingCase);
    ReturnsErrorPolicy.throwIfFatal(processed);
    NavigableMap<LocalDate, BigDecimal> weightedAverage = weightedAverageComponent.calculateWeightedAverage(
        processed.returnsMap(), returnFactorScale);
    return new WeightedAverageResult<>(weightedAverage, processed);
  }

  private <T extends ReturnsData> ReturnsSnapshot<T> applyPipeline(ReturnsSnapshot<T> initial,
      ProcessingContext processingContext, ProcessingCase processingCase) {
    List<ReturnsProcessor> pipeline = pipelinesByCase.get(processingCase);
    ReturnsSnapshot<T> current = initial;
    for (ReturnsProcessor processor : pipeline) {
      current = processor.process(current, processingContext);
    }
    return current;
  }

  private <T extends ReturnsData> ReturnsSnapshot<T> trimSnapshotToEnd(ReturnsSnapshot<T> snapshot,
      LocalDate endDate) {
    if (endDate == null || endDate.equals(snapshot.performanceEndDate())) {
      return snapshot;
    }
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByEndDate(
        snapshot.returnsMap(), endDate);
    return snapshot
        .withReturnsMap(trimmed)
        .withPeriod(PerformancePeriodCalculator.findPerformanceStartDate(trimmed),
            PerformancePeriodCalculator.findPerformanceEndDate(trimmed));
  }

  private static LocalDate earlierOf(LocalDate first, LocalDate second) {
    if (first == null) {
      return second;
    }
    if (second == null) {
      return first;
    }
    return first.isBefore(second) ? first : second;
  }

  private static Map<ProcessingCase, List<ReturnsProcessor>> buildPipelines(List<ReturnsProcessor> processors) {
    Map<ProcessingCase, List<ReturnsProcessor>> byCase = new EnumMap<>(ProcessingCase.class);
    for (ProcessingCase processingCase : ProcessingCase.values()) {
      byCase.put(processingCase, processors.stream()
          .filter(processor -> processor.isApplicable(processingCase))
          .toList());
    }
    return Map.copyOf(byCase);
  }
}
