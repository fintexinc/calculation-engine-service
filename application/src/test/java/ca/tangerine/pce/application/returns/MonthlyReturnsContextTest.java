package ca.tangerine.pce.application.returns;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

class MonthlyReturnsContextTest {

  private static final PortfolioHolding SPY = holding(
      new SecurityIdentifier("SPY", FiIdentifierType.TICKER), FinancialInstrumentType.ETF, Country.USA,
      (BigDecimal) null);

  @Test
  void shouldReplaceSnapshot_whenWithSnapshot() {
    ReturnsSnapshot<HoldingMonthlyReturns> firstSnapshot = ReturnsSnapshot.empty();
    ReturnsSnapshot<HoldingMonthlyReturns> secondSnapshot = ReturnsSnapshot.empty();
    FxContext fxContext = FxContext.empty();
    MonthlyReturnsContext<HoldingMonthlyReturns> context = new MonthlyReturnsContext<>(firstSnapshot, fxContext,
        ReturnsRole.PORTFOLIO);

    MonthlyReturnsContext<HoldingMonthlyReturns> next = context.withSnapshot(secondSnapshot);

    assertThat(next.snapshot()).isSameAs(secondSnapshot);
    assertThat(next.fxContext()).isSameAs(fxContext);
    assertThat(next.role()).isEqualTo(ReturnsRole.PORTFOLIO);
  }

  @Test
  void shouldReturnLaterStartDate_whenCommonPerformanceStartDate() {
    MonthlyReturnsContext<HoldingMonthlyReturns> first = contextWithRange(LocalDate.parse("2020-01-31"),
        LocalDate.parse("2024-12-31"));
    MonthlyReturnsContext<HoldingMonthlyReturns> second = contextWithRange(LocalDate.parse("2021-06-30"),
        LocalDate.parse("2023-06-30"));

    LocalDate result = first.commonPerformanceStartDate(second);

    assertThat(result).isEqualTo(LocalDate.parse("2021-06-30"));
  }

  @Test
  void shouldReturnEarlierEndDate_whenCommonPerformanceEndDate() {
    MonthlyReturnsContext<HoldingMonthlyReturns> first = contextWithPed(LocalDate.parse("2024-12-31"));
    MonthlyReturnsContext<HoldingMonthlyReturns> second = contextWithPed(LocalDate.parse("2023-06-30"));

    LocalDate result = first.commonPerformanceEndDate(second);

    assertThat(result).isEqualTo(LocalDate.parse("2023-06-30"));
  }

  @Test
  void shouldReturnNonNullEnd_whenCommonPerformanceEndDateAndOneSideIsNull() {
    MonthlyReturnsContext<HoldingMonthlyReturns> first = contextWithPed(LocalDate.parse("2024-12-31"));
    MonthlyReturnsContext<HoldingMonthlyReturns> second = contextWithPed(null);

    LocalDate result = first.commonPerformanceEndDate(second);

    assertThat(result).isEqualTo(LocalDate.parse("2024-12-31"));
  }

  @Test
  void shouldReturnSameSnapshot_whenTrimToEndWithMatchingEndDate() {
    MonthlyReturnsContext<HoldingMonthlyReturns> context = contextWithPed(LocalDate.parse("2024-12-31"));

    MonthlyReturnsContext<HoldingMonthlyReturns> result = context.trimToEnd(LocalDate.parse("2024-12-31"));

    assertThat(result.snapshot()).isSameAs(context.snapshot());
  }

  @Test
  void shouldTrimSnapshot_whenTrimToRange() {
    MonthlyReturnsContext<HoldingMonthlyReturns> context = contextWithRange(LocalDate.parse("2020-01-31"),
        LocalDate.parse("2020-04-30"));

    MonthlyReturnsContext<HoldingMonthlyReturns> result = context.trimToRange(LocalDate.parse("2020-02-29"),
        LocalDate.parse("2020-03-31"));

    assertThat(result.snapshot().performanceStartDate()).isEqualTo(LocalDate.parse("2020-02-29"));
    assertThat(result.snapshot().performanceEndDate()).isEqualTo(LocalDate.parse("2020-03-31"));
    assertThat(result.snapshot().returnsMap().get(SPY)).containsOnlyKeys(LocalDate.parse("2020-02-29"),
        LocalDate.parse("2020-03-31"));
  }

  private static MonthlyReturnsContext<HoldingMonthlyReturns> contextWithPed(LocalDate ped) {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = new HashMap<>();
    if (ped != null) {
      TreeMap<LocalDate, BigDecimal> series = new TreeMap<>();
      series.put(LocalDate.parse("2020-01-31"), BigDecimal.ONE);
      series.put(ped, BigDecimal.TEN);
      returns.put(SPY, series);
    }
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = new ReturnsSnapshot<>(Map.of(), returns,
        ped == null ? null : LocalDate.parse("2020-01-31"), ped, List.of());
    return new MonthlyReturnsContext<>(snapshot, FxContext.empty(), ReturnsRole.PORTFOLIO);
  }

  private static MonthlyReturnsContext<HoldingMonthlyReturns> contextWithRange(LocalDate psd, LocalDate ped) {
    TreeMap<LocalDate, BigDecimal> series = new TreeMap<>();
    series.put(psd, BigDecimal.ONE);
    series.put(psd.plusMonths(1).with(lastDayOfMonth()), BigDecimal.valueOf(2));
    series.put(ped.minusMonths(1).with(lastDayOfMonth()), BigDecimal.valueOf(3));
    series.put(ped, BigDecimal.TEN);
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returns = Map.of(SPY, series);
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = new ReturnsSnapshot<>(Map.of(), returns, psd, ped, List.of());
    return new MonthlyReturnsContext<>(snapshot, FxContext.empty(), ReturnsRole.PORTFOLIO);
  }
}
