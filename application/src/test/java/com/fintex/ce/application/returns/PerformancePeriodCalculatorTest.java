package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static org.assertj.core.api.Assertions.assertThat;

class PerformancePeriodCalculatorTest {

  private static final PortfolioHolding HOLDING_A = holdingWithoutCountry(
      new SecurityIdentifier("A", FiIdentifierType.TICKER), null, null);
  private static final PortfolioHolding HOLDING_B = holdingWithoutCountry(
      new SecurityIdentifier("B", FiIdentifierType.TICKER), null, null);
  private static final PortfolioHolding HOLDING_C = holdingWithoutCountry(
      new SecurityIdentifier("C", FiIdentifierType.TICKER), null, null);

  @Test
  void shouldReturnLatestStartingHoldingFirstDate_whenFindPerformanceStartDate() {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap = new HashMap<>();
    returnsMap.put(HOLDING_A, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2024-12-31")));
    returnsMap.put(HOLDING_B, series(LocalDate.parse("2022-06-30"), LocalDate.parse("2024-12-31")));
    returnsMap.put(HOLDING_C, series(LocalDate.parse("2021-03-31"), LocalDate.parse("2024-12-31")));

    LocalDate psd = PerformancePeriodCalculator.findPerformanceStartDate(returnsMap);

    assertThat(psd).isEqualTo(LocalDate.parse("2022-06-30"));
  }

  @Test
  void shouldReturnEarliestEndingHoldingLastDate_whenFindPerformanceEndDate() {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap = new HashMap<>();
    returnsMap.put(HOLDING_A, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2024-12-31")));
    returnsMap.put(HOLDING_B, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2023-09-30")));
    returnsMap.put(HOLDING_C, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2024-03-31")));

    LocalDate ped = PerformancePeriodCalculator.findPerformanceEndDate(returnsMap);

    assertThat(ped).isEqualTo(LocalDate.parse("2023-09-30"));
  }

  @Test
  void shouldReturnNull_whenFindPerformanceStartDateOnEmptyMap() {
    LocalDate psd = PerformancePeriodCalculator.findPerformanceStartDate(Map.of());
    LocalDate ped = PerformancePeriodCalculator.findPerformanceEndDate(Map.of());

    assertThat(psd).isNull();
    assertThat(ped).isNull();
  }

  @Test
  void shouldIgnoreEmptySeries_whenFindPerformanceWindow() {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap = new HashMap<>();
    returnsMap.put(HOLDING_A, new TreeMap<>());
    returnsMap.put(HOLDING_B, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2023-09-30")));

    LocalDate psd = PerformancePeriodCalculator.findPerformanceStartDate(returnsMap);
    LocalDate ped = PerformancePeriodCalculator.findPerformanceEndDate(returnsMap);

    assertThat(psd).isEqualTo(LocalDate.parse("2020-01-31"));
    assertThat(ped).isEqualTo(LocalDate.parse("2023-09-30"));
  }

  @Test
  void shouldKeepEntriesOnOrBeforeEndDate_whenTrimByEndDate() {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source = new HashMap<>();
    source.put(HOLDING_A, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2024-12-31")));

    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByEndDate(source,
        LocalDate.parse("2022-12-31"));

    assertThat(trimmed.get(HOLDING_A).lastKey()).isEqualTo(LocalDate.parse("2022-12-31"));
    assertThat(trimmed.get(HOLDING_A).firstKey()).isEqualTo(LocalDate.parse("2020-01-31"));
    assertThat(trimmed.get(HOLDING_A)).hasSize(36);
  }

  @Test
  void shouldKeepEntriesOnOrAfterStartDate_whenTrimByStartDate() {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source = new HashMap<>();
    source.put(HOLDING_A, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2024-12-31")));

    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByStartDate(source,
        LocalDate.parse("2022-01-31"));

    assertThat(trimmed.get(HOLDING_A).firstKey()).isEqualTo(LocalDate.parse("2022-01-31"));
    assertThat(trimmed.get(HOLDING_A).lastKey()).isEqualTo(LocalDate.parse("2024-12-31"));
    assertThat(trimmed.get(HOLDING_A)).hasSize(36);
  }

  @Test
  void shouldReturnSameMap_whenTrimByEndDateWithNullDate() {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source = new HashMap<>();
    source.put(HOLDING_A, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2024-12-31")));

    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> result = PerformancePeriodCalculator.trimByEndDate(source,
        null);

    assertThat(result).isSameAs(source);
  }

  @Test
  void shouldReturnSameMap_whenTrimByStartDateWithNullDate() {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source = new HashMap<>();
    source.put(HOLDING_A, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2024-12-31")));

    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> result = PerformancePeriodCalculator.trimByStartDate(source,
        null);

    assertThat(result).isSameAs(source);
  }

  @Test
  void shouldReturnNewMapWithFreshTreeMaps_whenTrimByEndDate() {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> source = new HashMap<>();
    source.put(HOLDING_A, series(LocalDate.parse("2020-01-31"), LocalDate.parse("2024-12-31")));

    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByEndDate(source,
        LocalDate.parse("2022-12-31"));

    assertThat(trimmed).isNotSameAs(source);
    assertThat(trimmed.get(HOLDING_A)).isNotSameAs(source.get(HOLDING_A));
  }

  private static TreeMap<LocalDate, BigDecimal> series(LocalDate start, LocalDate end) {
    TreeMap<LocalDate, BigDecimal> map = new TreeMap<>();
    LocalDate cursor = start.with(TemporalAdjusters.lastDayOfMonth());
    LocalDate stop = end.with(TemporalAdjusters.lastDayOfMonth());
    int counter = 1;
    while (!cursor.isAfter(stop)) {
      map.put(cursor, BigDecimal.valueOf(counter++));
      cursor = cursor.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    }
    return map;
  }
}
