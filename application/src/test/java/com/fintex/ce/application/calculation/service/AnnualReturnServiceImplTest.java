package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.calculation.service.AnnualReturnServiceImpl.buildAnnualReturnResult;
import static com.fintex.ce.application.calculation.service.AnnualReturnServiceImpl.calculateAnnualReturns;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnnualReturnServiceImplTest {

  @Test
  void shouldCalculateAnnualReturn_whenYearHasTwelveMonthlyReturns() {
    LocalDate december = LocalDate.of(2020, 12, 1);
    TreeMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    for (int monthsBack = 0; monthsBack < 12; monthsBack++) {
      portfolioReturns.put(toLastDayOfMonth(december.minusMonths(monthsBack)), TEN);
    }

    TreeMap<Integer, BigDecimal> actual = calculateAnnualReturns(portfolioReturns, Set.of(december.getYear()));

    assertThat(actual).hasSize(1);
    assertThat(actual.get(december.getYear())).isEqualByComparingTo(new BigDecimal("999999999999"));
  }

  @Test
  void shouldReturnEmptyAnnualReturns_whenDecemberMissing() {
    LocalDate november = LocalDate.of(2020, 11, 1);
    TreeMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    for (int monthsBack = 0; monthsBack < 11; monthsBack++) {
      portfolioReturns.put(toLastDayOfMonth(november.minusMonths(monthsBack)), TEN);
    }

    TreeMap<Integer, BigDecimal> actual = calculateAnnualReturns(portfolioReturns, Set.of(november.getYear()));

    assertThat(actual).isEmpty();
  }

  @Test
  void shouldReturnEmptyAnnualReturns_whenJanuaryMissing() {
    LocalDate december = LocalDate.of(2020, 12, 1);
    Map<LocalDate, BigDecimal> map = new HashMap<>();
    for (int monthsBack = 0; monthsBack < 11; monthsBack++) {
      map.put(toLastDayOfMonth(december.minusMonths(monthsBack)), TEN);
    }

    TreeMap<Integer, BigDecimal> actual = calculateAnnualReturns(new TreeMap<>(map), Set.of(december.getYear()));

    assertThat(actual).isEmpty();
  }

  @Test
  void shouldThrowIncompleteYearSkipped_whenYearContainsGapInMonthlyReturns() {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    for (int month = 1; month <= 12; month++) {
      if (month == 3) {
        continue;
      }
      portfolioReturns.put(toLastDayOfMonth(LocalDate.of(2020, month, 1)), TEN);
    }

    assertThatThrownBy(() -> calculateAnnualReturns(portfolioReturns, Set.of(2020)))
        .isInstanceOf(CalculationException.class)
        .satisfies(thrown -> assertThat(((CalculationException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.INCOMPLETE_YEAR_SKIPPED))
        .hasMessageContaining("2020");
  }

  @Test
  void shouldThrowNoCompleteCalendarYear_whenDataWindowDoesNotCoverAnyFullJanDecYear() {
    // DIV-shape regression: data spans Oct 2024 → Sep 2025 — neither year is a complete Jan-Dec calendar year,
    // so per-year INCOMPLETE_YEAR_SKIPPED never fires (Jan or Dec is entirely absent) and the calculation aborts
    // with NO_COMPLETE_CALENDAR_YEAR instead of returning an empty result.
    TreeMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    for (int monthsBack = 0; monthsBack < 12; monthsBack++) {
      portfolioReturns.put(toLastDayOfMonth(LocalDate.of(2025, 9, 1).minusMonths(monthsBack)), TEN);
    }

    assertThatThrownBy(() -> buildAnnualReturnResult(portfolioReturns, List.of()))
        .isInstanceOf(CalculationException.class)
        .satisfies(thrown -> assertThat(((CalculationException) thrown).getErrorCode())
            .isEqualTo(ErrorCode.NO_COMPLETE_CALENDAR_YEAR));
  }

  @Test
  void shouldPopulateResult_whenAtLeastOneFullYearIsCovered() {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    for (int year = 2020; year <= 2024; year++) {
      for (int month = 1; month <= 12; month++) {
        portfolioReturns.put(toLastDayOfMonth(LocalDate.of(year, month, 1)), TEN);
      }
    }

    AnnualReturnResult<Integer> result = buildAnnualReturnResult(portfolioReturns, List.<Notification>of());

    assertThat(result.getAnnualReturns()).hasSize(5);
    assertThat(result.getPerformanceStartDate()).isEqualTo(toLastDayOfMonth(LocalDate.of(2020, 1, 1)));
    assertThat(result.getPerformanceEndDate()).isEqualTo(toLastDayOfMonth(LocalDate.of(2024, 12, 1)));
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldPropagateWarnings_whenWarningsArePresent() {
    TreeMap<LocalDate, BigDecimal> portfolioReturns = new TreeMap<>();
    for (int month = 1; month <= 12; month++) {
      portfolioReturns.put(toLastDayOfMonth(LocalDate.of(2020, month, 1)), TEN);
    }
    Notification warning = mock(Notification.class);

    AnnualReturnResult<Integer> result = buildAnnualReturnResult(portfolioReturns, List.of(warning));

    assertThat(result.getWarnings()).containsExactly(warning);
  }

  @Test
  void shouldOrchestrateWeightedAverageAndYearMath_whenPerformIsCalled() {
    MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    AnnualReturnServiceImpl service = new AnnualReturnServiceImpl(monthlyReturnsService);

    NavigableMap<LocalDate, BigDecimal> weightedAverage = new TreeMap<>();
    for (int month = 1; month <= 12; month++) {
      weightedAverage.put(toLastDayOfMonth(LocalDate.of(2020, month, 1)), TEN);
    }
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot.empty();
    @SuppressWarnings("unchecked")
    MonthlyReturnsContext<HoldingMonthlyReturns> context = mock(MonthlyReturnsContext.class);
    WeightedAverageResult<HoldingMonthlyReturns> weightedResult = new WeightedAverageResult<>(weightedAverage,
        snapshot);

    ReturnCommand command = new ReturnCommand();
    command.setHoldings(List.of());
    command.setCurrency(Currency.CAD);
    command.setCustomPsd(LocalDate.of(2020, 1, 1));
    command.setCustomPed(LocalDate.of(2020, 12, 31));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(command.getHoldings(), Currency.CAD)).thenReturn(context);
    when(monthlyReturnsService.calculateWeightedAverageWithCpsdAndCped(eq(context), eq(command.getCustomPsd()),
        eq(command.getCustomPed()), any(ReturnFactorScale.class))).thenReturn(weightedResult);

    AnnualReturnResult<Integer> result = service.perform(command);

    assertThat(result.getAnnualReturns()).hasSize(1);
    assertThat(result.getAnnualReturns().get(0).key()).isEqualTo(2020);
  }
}
