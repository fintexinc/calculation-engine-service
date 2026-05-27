package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyReturnsContextTest {

  private static final PortfolioHolding SPY = new PortfolioHolding(null, FinancialInstrumentType.ETF_US,
      new SecurityIdentifier("SPY", FiIdentifierType.TICKER));

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
}
