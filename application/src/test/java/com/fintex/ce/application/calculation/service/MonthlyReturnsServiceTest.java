package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.returns.ReturnsCutComponent;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.port.webclient.FxRatesFetcher;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.wm.commons.domain.currency.Currency.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MonthlyReturnsServiceTest {

  @Test
  void shouldGetWeightedAverageWithCpsdAndCpedValidation_whenVerifyGetWeightedAverage() {
    final var service = mock(MonthlyReturnsService.class);
    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .validateCpsd(eq(LOCAL_DATE_NOW))
        .validateReturns()
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .cutByCpsdIfCpsdEmptyCutByPsd(eq(LOCAL_DATE_NOW))
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(service).getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any());

    service.getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(3));
  }

  @Test
  void shouldGetWeightedAverageWithCpsdAndCpedValidation_whenCheckResult() {
    final var service = mock(MonthlyReturnsService.class);
    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .validateCpsd(eq(LOCAL_DATE_NOW))
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .cutByCpsdIfCpsdEmptyCutByPsd(eq(LOCAL_DATE_NOW))
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(service).getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any());

    final NavigableMap<LocalDate, BigDecimal> actual = service.getWeightedAverageWithCpsdAndCpedValidation(
        monthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(3));

    assertSame(portfolioBaseTotalReturns, actual);
  }

  @Test
  void shouldGetWeightedAverageWithCpedValidation_whenVerifyGetWeightedAverage() {
    final var service = mock(MonthlyReturnsService.class);
    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW))
        .validateReturns()
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW))
        .cutByPsd()
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(service).getWeightedAverageWithCpedValidation(any(), any());

    service.getWeightedAverageWithCpedValidation(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldGetWeightedAverageWithCpedValidation_whenCheckResult() {
    final var service = mock(MonthlyReturnsService.class);
    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW))
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW))
        .cutByPsd()
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(service).getWeightedAverageWithCpedValidation(any(), any());

    final NavigableMap<LocalDate, BigDecimal> actual = service.getWeightedAverageWithCpedValidation(monthlyReturns,
        LOCAL_DATE_NOW);

    assertSame(portfolioBaseTotalReturns, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyLoad() {
    try (MockedConstruction<ReturnsAggregate> mocked = Mockito.mockConstruction(ReturnsAggregate.class)) {
      final var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
      final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
      final var service = mock(MonthlyReturnsService.class, withSettings()
          .useConstructor(monthlyReturnsFetcher, mock(FxRatesFetcher.class), gicMonthlyReturnsGenerator));

      final var holdings = mock(List.class);

      when(monthlyReturnsFetcher.fetch(any(), any())).thenReturn(new HashMap<>());
      doCallRealMethod().when(service).getMonthlyReturns(anyList(), any());

      service.getMonthlyReturns(holdings, CAD);

      verify(monthlyReturnsFetcher).fetch(holdings, List.of());
    }
  }

  @Test
  void shouldGetMonthlyReturns_whenCheckResult() {
    final var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
    final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    final var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(monthlyReturnsFetcher, mock(FxRatesFetcher.class), gicMonthlyReturnsGenerator));

    final var originalMonthlyReturns = mock(Map.class);
    when(monthlyReturnsFetcher.fetch(any(), any())).thenReturn(originalMonthlyReturns);
    final ReturnsAggregate expected = mock(ReturnsAggregate.class);
    when(service.getMonthlyReturns(originalMonthlyReturns)).thenReturn(expected);

    doCallRealMethod().when(service).getMonthlyReturns(anyList(), any());

    final var actual = service.getMonthlyReturns(mock(List.class), Currency.CAD);

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyGicWasGenerated() {
    final var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
    final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    final var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(monthlyReturnsFetcher, mock(FxRatesFetcher.class), gicMonthlyReturnsGenerator));

    final var originalMonthlyReturns = mock(Map.class);
    when(monthlyReturnsFetcher.fetch(any(), any())).thenReturn(originalMonthlyReturns);
    final Map gicOriginalMonthlyReturns = mock(Map.class);
    when(gicMonthlyReturnsGenerator.generateGicMonthlyReturns(anyList())).thenReturn(gicOriginalMonthlyReturns);

    doCallRealMethod().when(service).getMonthlyReturns(anyList(), any());

    service.getMonthlyReturns(mock(List.class), Currency.CAD);

    verify(originalMonthlyReturns).putAll(gicOriginalMonthlyReturns);
  }

  @Test
  void shouldGetPortfolioMonthlyReturns_whenVerifyGetMonthlyReturns() {
    final var fxRatesFetcher = mock(FxRatesFetcher.class);
    final var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRatesFetcher, mock(MonthlyReturnsGenerator.class)));

    final var holdings = mock(List.class);
    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    monthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(service).getPortfolioMonthlyReturns(anyList(), any(), any());

    service.getPortfolioMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    verify(service).getMonthlyReturns(holdings, CAD);
  }

  @Test
  void shouldGetPortfolioMonthlyReturns_whenVerifyInit() {
    final var fxRatesFetcher = mock(FxRatesFetcher.class);
    final var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRatesFetcher, mock(MonthlyReturnsGenerator.class)));

    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_SELF);
    monthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(service).getPortfolioMonthlyReturns(anyList(), any(), any());

    service.getPortfolioMonthlyReturns(mock(List.class), CAD, ReturnFactorScale.SCALE_OF_TWO);

    final var inOrder = inOrder(monthlyReturns);

    inOrder.verify(monthlyReturns).setFxRatesConversionComponent(any());
    inOrder.verify(monthlyReturns).setMonthlyReturnsCutComponent(eq(new ReturnsCutComponent()));
    inOrder.verify(monthlyReturns).setWeightedAverageComponent(eq(new WeightedAverageComponent(
        ReturnFactorScale.SCALE_OF_TWO)));
    inOrder.verify(monthlyReturns).setCpsdDataValidation(eq(new PortfolioCpsdDataValidation()));
    inOrder.verify(monthlyReturns).setCpedDataValidation(eq(new PortfolioCpedDataValidation()));
  }

  @Test
  void shouldGetPortfolioMonthlyReturns_whenCheckResult() {
    final var fxRatesFetcher = mock(FxRatesFetcher.class);
    final var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRatesFetcher, mock(MonthlyReturnsGenerator.class)));

    final var holdings = mock(List.class);
    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    monthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(service).getPortfolioMonthlyReturns(anyList(), any(), any());

    final var actual = service.getPortfolioMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    assertSame(monthlyReturns, actual);
  }

  @Test
  void shouldGetBenchmarkMonthlyReturns_whenVerifyGetMonthlyReturns() {
    final var fxRatesFetcher = mock(FxRatesFetcher.class);
    final var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRatesFetcher, mock(MonthlyReturnsGenerator.class)));

    final var holdings = mock(List.class);
    final var benchmarkMonthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    benchmarkMonthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(service).getBenchmarkMonthlyReturns(anyList(), any(), any());

    service.getBenchmarkMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    verify(service).getMonthlyReturns(holdings, CAD);
  }

  @Test
  void shouldGetBenchmarkMonthlyReturns_whenVerifyInit() {
    final var fxRatesFetcher = mock(FxRatesFetcher.class);
    final var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRatesFetcher, mock(MonthlyReturnsGenerator.class)));

    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_SELF);
    monthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(service).getBenchmarkMonthlyReturns(anyList(), any(), any());

    service.getBenchmarkMonthlyReturns(mock(List.class), CAD, ReturnFactorScale.SCALE_OF_TWO);

    final var inOrder = inOrder(monthlyReturns);

    inOrder.verify(monthlyReturns).setFxRatesConversionComponent(any());
    inOrder.verify(monthlyReturns).setMonthlyReturnsCutComponent(eq(new ReturnsCutComponent()));
    inOrder.verify(monthlyReturns).setWeightedAverageComponent(eq(new WeightedAverageComponent(
        ReturnFactorScale.SCALE_OF_TWO)));
    inOrder.verify(monthlyReturns).setCpsdDataValidation(eq(new BenchmarkCpsdDataValidation()));
    inOrder.verify(monthlyReturns).setCpedDataValidation(eq(new BenchmarkCpedDataValidation()));
  }

  @Test
  void shouldGetBenchmarkMonthlyReturns_whenCheckResult() {
    final var fxRatesFetcher = mock(FxRatesFetcher.class);
    final var service = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRatesFetcher, mock(MonthlyReturnsGenerator.class)));

    final var holdings = mock(List.class);
    final var benchmarkMonthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    benchmarkMonthlyReturns.holdingCurrencyMap = new HashMap<>();
    when(service.getMonthlyReturns(anyList(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(service).getBenchmarkMonthlyReturns(anyList(), any(), any());

    final var actual = service.getBenchmarkMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    assertSame(benchmarkMonthlyReturns, actual);
  }

}
