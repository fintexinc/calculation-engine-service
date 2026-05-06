package com.fintex.ce.application.returns.processor;

import com.fintex.ce.application.returns.FxContext;
import com.fintex.ce.application.returns.ProcessingCase;
import com.fintex.ce.application.returns.ProcessingContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class CutByCpsdOrPsdProcessorTest {

  private static final PortfolioHolding HOLDING = new PortfolioHolding(null, null,
      new SecurityIdentifier("A", FiIdentifierType.TICKER));

  private final CutByCpsdOrPsdProcessor processor = new CutByCpsdOrPsdProcessor();

  @Test
  void shouldUseCpsd_whenContextHasCpsd() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshotWithSeries(LocalDate.parse("2020-01-31"),
        LocalDate.parse("2024-12-31"));
    ProcessingContext context = ProcessingContext.of(LocalDate.parse("2022-06-30"), null, FxContext.empty());

    ReturnsSnapshot<HoldingMonthlyReturns> result = processor.process(snapshot, context);

    assertThat(result.returnsMap().get(HOLDING).firstKey()).isEqualTo(LocalDate.parse("2022-06-30"));
    assertThat(result.performanceStartDate()).isEqualTo(LocalDate.parse("2022-06-30"));
    assertThat(result.performanceEndDate()).isEqualTo(snapshot.performanceEndDate());
  }

  @Test
  void shouldFallBackToPsd_whenCpsdIsNull() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = snapshotWithSeries(LocalDate.parse("2020-01-31"),
        LocalDate.parse("2024-12-31"));
    ProcessingContext context = ProcessingContext.of(null, null, FxContext.empty());

    ReturnsSnapshot<HoldingMonthlyReturns> result = processor.process(snapshot, context);

    assertThat(result.returnsMap().get(HOLDING).firstKey()).isEqualTo(LocalDate.parse("2020-01-31"));
    assertThat(result.performanceStartDate()).isEqualTo(LocalDate.parse("2020-01-31"));
  }

  @Test
  void shouldReturnSameSnapshot_whenBothCpsdAndPsdAreNull() {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.empty();
    ProcessingContext context = ProcessingContext.of(null, null, FxContext.empty());

    ReturnsSnapshot<HoldingMonthlyReturns> result = processor.process(snapshot, context);

    assertThat(result).isSameAs(snapshot);
  }

  @Test
  void shouldNotApply_whenProcessingCaseIsPrePsdTrim() {
    assertThat(processor.isApplicable(ProcessingCase.PORTFOLIO_PRE_PSD_TRIM)).isFalse();
    assertThat(processor.isApplicable(ProcessingCase.BENCHMARK_PRE_PSD_TRIM)).isFalse();
  }

  @Test
  void shouldApply_whenProcessingCaseIsAnyWeightedAverageCase() {
    assertThat(processor.isApplicable(ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED)).isTrue();
    assertThat(processor.isApplicable(ProcessingCase.PORTFOLIO_WEIGHTED_AVERAGE_WITH_CPED_ONLY)).isTrue();
    assertThat(processor.isApplicable(ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPSD_AND_CPED)).isTrue();
    assertThat(processor.isApplicable(ProcessingCase.BENCHMARK_WEIGHTED_AVERAGE_WITH_CPED_ONLY)).isTrue();
  }

  private static ReturnsSnapshot<HoldingMonthlyReturns> snapshotWithSeries(LocalDate start, LocalDate end) {
    TreeMap<LocalDate, BigDecimal> series = new TreeMap<>();
    LocalDate cursor = start.with(TemporalAdjusters.lastDayOfMonth());
    LocalDate stop = end.with(TemporalAdjusters.lastDayOfMonth());
    int i = 1;
    while (!cursor.isAfter(stop)) {
      series.put(cursor, BigDecimal.valueOf(i++));
      cursor = cursor.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    }
    return new ReturnsSnapshot<>(Map.of(HOLDING, Currency.USD), Map.of(HOLDING, series), start, end, List.of());
  }
}
