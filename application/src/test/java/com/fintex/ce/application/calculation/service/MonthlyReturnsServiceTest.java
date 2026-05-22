package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.FxContext;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ReturnsRole;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.processor.ReturnsProcessor;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonthlyReturnsServiceTest {

  private static final PortfolioHolding ETF = new PortfolioHolding(null, FinancialInstrumentType.ETF_US,
      new SecurityIdentifier("ETF-A", FiIdentifierType.TICKER));
  private static final PortfolioHolding STOCK = new PortfolioHolding(null, FinancialInstrumentType.STOCK_US,
      new SecurityIdentifier("STK-B", FiIdentifierType.TICKER));
  private static final PortfolioHolding CASH = new PortfolioHolding(null, FinancialInstrumentType.CASH,
      new SecurityIdentifier("CASH", FiIdentifierType.TICKER));

  @SuppressWarnings("unchecked")
  private final SecurityDataFetcher<HoldingMonthlyReturns> fetcher = mock(SecurityDataFetcher.class);
  private final FxRateService fxRateService = mock(FxRateService.class);
  private final MonthlyReturnsGenerator generator = mock(MonthlyReturnsGenerator.class);
  private final WeightedAverageComponent weightedAverageComponent = mock(WeightedAverageComponent.class);

  @Test
  void shouldThrowSmsNoDataForHolding_whenHoldingMissingFromSecurityMasterResponse() {
    when(fetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());
    MonthlyReturnsService service = service(List.of());

    assertThatThrownBy(() -> service.getMonthlyReturns(List.of(ETF)))
        .isInstanceOf(BasePceException.class)
        .satisfies(thrown -> assertThat(((BasePceException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.NO_SECURITY_DATA_FOR_HOLDING));
  }

  @Test
  void shouldThrowMissingMonthlyReturns_whenHoldingPresentButReturnsAreEmpty() {
    HoldingMonthlyReturns empty = new HoldingMonthlyReturns();
    empty.setCurrency(Currency.USD.name());
    empty.setReturns(new TreeMap<>());
    when(fetcher.fetch(anyList(), anyList())).thenReturn(Map.of(ETF, empty));
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());
    MonthlyReturnsService service = service(List.of());

    assertThatThrownBy(() -> service.getMonthlyReturns(List.of(ETF)))
        .isInstanceOf(BasePceException.class)
        .satisfies(thrown -> assertThat(((BasePceException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.MISSING_MONTHLY_RETURNS));
  }

  @Test
  void shouldSkipCashAndGicTypes_whenValidatingMonthlyReturnsPresence() {
    when(fetcher.fetch(anyList(), anyList())).thenReturn(Map.of());
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());
    MonthlyReturnsService service = service(List.of());

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = service.getMonthlyReturns(List.of(CASH));

    assertThat(snapshot.returnsMap()).isEmpty();
    assertThat(snapshot.errors()).isEmpty();
  }

  @Test
  void shouldMergeFetcherAndGeneratorOutputs_whenGetMonthlyReturns() {
    HoldingMonthlyReturns etfReturns = holdingMonthlyReturns(Currency.USD,
        Map.entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.01)));
    HoldingMonthlyReturns stockReturns = holdingMonthlyReturns(Currency.USD,
        Map.entry(LocalDate.parse("2020-01-31"), BigDecimal.valueOf(0.02)));
    when(fetcher.fetch(anyList(), anyList())).thenReturn(Map.of(ETF, etfReturns));
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of(STOCK, stockReturns));
    MonthlyReturnsService service = service(List.of());

    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = service.getMonthlyReturns(List.of(ETF, STOCK));

    assertThat(snapshot.returnsMap()).containsOnlyKeys(ETF, STOCK);
  }

  @Test
  void shouldQueryFxRatesWithLowerBoundExtendedToFirstOfMonthBeforePsd_whenBuildingPortfolioContext() {
    // Regression: the FX query lower bound must reach back further than PSD - 1 month-end so that
    // floorEntry(PSD - 1 month) succeeds even when PSD - 1 month-end falls on a weekend or holiday.
    // FX_RATES_UNAVAILABLE is now a hard HTTP 400 error, so a spurious missing-rate on the first
    // month would abort the calculation instead of warning.
    LocalDate psd = LocalDate.parse("2020-01-31");
    LocalDate ped = LocalDate.parse("2020-03-31");
    HoldingMonthlyReturns etfReturns = holdingMonthlyReturns(Currency.USD,
        Map.entry(psd, BigDecimal.ONE),
        Map.entry(ped, BigDecimal.ONE));
    when(fetcher.fetch(anyList(), anyList())).thenReturn(Map.of(ETF, etfReturns));
    when(generator.generateGicMonthlyReturns(anyList())).thenReturn(Map.of());
    when(fxRateService.rates(any(), any(), any())).thenReturn(Map.of());
    MonthlyReturnsService service = service(List.of());

    service.getPortfolioMonthlyReturns(List.of(ETF), Currency.CAD);

    ArgumentCaptor<DateRange> rangeCaptor = ArgumentCaptor.forClass(DateRange.class);
    verify(fxRateService).rates(any(), eq(Currency.CAD), rangeCaptor.capture());
    assertThat(rangeCaptor.getValue().start()).isEqualTo(LocalDate.parse("2019-12-01"));
    assertThat(rangeCaptor.getValue().end()).isEqualTo(ped);
  }

  @Test
  void shouldReturnEarlierEndDate_whenCommonPerformanceEndDate() {
    MonthlyReturnsService service = service(List.of());
    MonthlyReturnsContext<HoldingMonthlyReturns> first = contextWithPed(LocalDate.parse("2024-12-31"));
    MonthlyReturnsContext<HoldingMonthlyReturns> second = contextWithPed(LocalDate.parse("2023-06-30"));

    LocalDate result = service.commonPerformanceEndDate(first, second);

    assertThat(result).isEqualTo(LocalDate.parse("2023-06-30"));
  }

  @Test
  void shouldReturnNonNullEnd_whenCommonPerformanceEndDateAndOneSideIsNull() {
    MonthlyReturnsService service = service(List.of());
    MonthlyReturnsContext<HoldingMonthlyReturns> first = contextWithPed(LocalDate.parse("2024-12-31"));
    MonthlyReturnsContext<HoldingMonthlyReturns> second = contextWithPed(null);

    LocalDate result = service.commonPerformanceEndDate(first, second);

    assertThat(result).isEqualTo(LocalDate.parse("2024-12-31"));
  }

  @Test
  void shouldReturnSameContext_whenTrimContextToEndWithMatchingEndDate() {
    MonthlyReturnsService service = service(List.of());
    MonthlyReturnsContext<HoldingMonthlyReturns> context = contextWithPed(LocalDate.parse("2024-12-31"));

    MonthlyReturnsContext<HoldingMonthlyReturns> result = service.trimContextToEnd(context,
        LocalDate.parse("2024-12-31"));

    assertThat(result.snapshot()).isSameAs(context.snapshot());
  }

  @Test
  void shouldRunPipelineForApplicableProcessors_whenApplyValidateCutAndFx() {
    ReturnsProcessor passthrough = new RecordingProcessor(snapshot -> snapshot, true);
    MonthlyReturnsService service = service(List.of(passthrough));
    MonthlyReturnsContext<HoldingMonthlyReturns> context = new MonthlyReturnsContext<>(
        ReturnsSnapshot.empty(), FxContext.empty(), ReturnsRole.PORTFOLIO);

    ReturnsSnapshot<HoldingMonthlyReturns> result = service.applyValidateCutAndFx(context, null);

    assertThat(result).isNotNull();
    assertThat(((RecordingProcessor) passthrough).invocations).isEqualTo(1);
  }

  @Test
  void shouldThrow_whenApplyValidateCutAndFxAndPipelineLeavesFatalError() {
    ReturnsProcessor injectFatal = new RecordingProcessor(
        snapshot -> snapshot.withAddedErrors(List.of(ErrorCode.CPED_AFTER_PORTFOLIO_PED.toException())),
        true);
    MonthlyReturnsService service = service(List.of(injectFatal));
    MonthlyReturnsContext<HoldingMonthlyReturns> context = new MonthlyReturnsContext<>(
        ReturnsSnapshot.empty(), FxContext.empty(), ReturnsRole.PORTFOLIO);

    assertThatThrownBy(() -> service.applyValidateCutAndFx(context, null))
        .isInstanceOf(CalculationsFailedException.class);
  }

  @Test
  void shouldEmitWeightedAverage_whenCalculateWeightedAverageWithCpsdAndCped() {
    ReturnsProcessor passthrough = new RecordingProcessor(snapshot -> snapshot, true);
    NavigableMap<LocalDate, BigDecimal> expected = new TreeMap<>();
    expected.put(LocalDate.parse("2020-01-31"), BigDecimal.ONE);
    when(weightedAverageComponent.calculateWeightedAverage(any(), any())).thenReturn(expected);
    MonthlyReturnsService service = service(List.of(passthrough));
    MonthlyReturnsContext<HoldingMonthlyReturns> context = new MonthlyReturnsContext<>(
        ReturnsSnapshot.empty(), FxContext.empty(), ReturnsRole.PORTFOLIO);

    WeightedAverageResult<HoldingMonthlyReturns> result = service.calculateWeightedAverageWithCpsdAndCped(context,
        LocalDate.parse("2020-01-31"), LocalDate.parse("2024-12-31"), ReturnFactorScale.SCALE_OF_TWO);

    assertThat(result.weightedAverage()).isEqualTo(expected);
    assertThat(result.snapshot()).isNotNull();
    verify(weightedAverageComponent, times(1)).calculateWeightedAverage(any(), any());
  }

  private MonthlyReturnsService service(List<ReturnsProcessor> processors) {
    return new MonthlyReturnsService(fetcher, fxRateService, generator, weightedAverageComponent, processors);
  }

  private static MonthlyReturnsContext<HoldingMonthlyReturns> contextWithPed(LocalDate ped) {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = new HashMap<>();
    if (ped != null) {
      TreeMap<LocalDate, BigDecimal> series = new TreeMap<>();
      series.put(LocalDate.parse("2020-01-31"), BigDecimal.ONE);
      series.put(ped, BigDecimal.TEN);
      returns.put(ETF, series);
    }
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = new ReturnsSnapshot<>(Map.of(), returns,
        ped == null ? null : LocalDate.parse("2020-01-31"), ped, List.of());
    return new MonthlyReturnsContext<>(snapshot, FxContext.empty(), ReturnsRole.PORTFOLIO);
  }

  @SafeVarargs
  private static HoldingMonthlyReturns holdingMonthlyReturns(Currency currency,
      Map.Entry<LocalDate, BigDecimal>... entries) {
    HoldingMonthlyReturns data = new HoldingMonthlyReturns();
    data.setCurrency(currency.name());
    TreeMap<LocalDate, BigDecimal> map = new TreeMap<>();
    for (Map.Entry<LocalDate, BigDecimal> entry : entries) {
      map.put(entry.getKey(), entry.getValue());
    }
    data.setReturns(map);
    return data;
  }

  private static final class RecordingProcessor implements ReturnsProcessor {
    private final java.util.function.UnaryOperator<ReturnsSnapshot<?>> transform;
    private final boolean applicable;
    private int invocations;

    RecordingProcessor(java.util.function.UnaryOperator<ReturnsSnapshot<?>> transform, boolean applicable) {
      this.transform = transform;
      this.applicable = applicable;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends com.fintex.ce.model.domain.calculation.returns.ReturnsData> ReturnsSnapshot<T> process(
        ReturnsSnapshot<T> snapshot, ProcessingContext context) {
      invocations++;
      return (ReturnsSnapshot<T>) transform.apply((ReturnsSnapshot) snapshot);
    }

    @Override
    public boolean isApplicable(ProcessingCase processingCase) {
      return applicable;
    }
  }
}
