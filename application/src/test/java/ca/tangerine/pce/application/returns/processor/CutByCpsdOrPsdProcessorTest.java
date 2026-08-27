package ca.tangerine.pce.application.returns.processor;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.application.returns.FxContext;
import ca.tangerine.pce.application.returns.ProcessingCase;
import ca.tangerine.pce.application.returns.ProcessingContext;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

class CutByCpsdOrPsdProcessorTest {

  private static final PortfolioHolding HOLDING = holdingWithoutCountry(
      new SecurityIdentifier("A", FiIdentifierType.TICKER), null, null);

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
