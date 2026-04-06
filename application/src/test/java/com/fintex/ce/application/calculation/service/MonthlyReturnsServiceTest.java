package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.FxRatesConversionComponent;
import com.fintex.ce.application.returns.MonthlyReturnsGenerator;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.application.returns.ReturnsCutComponent;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.port.webclient.FxRatesFetcher;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
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
import static com.fintex.sm.model.domain.enumeration.CurrencyType.CAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
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
    final var sut = mock(MonthlyReturnsService.class);
    final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .validateCpsd(eq(LOCAL_DATE_NOW))
        .validateReturns()
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .cutByCpsdIfCpsdEmptyCutByPsd(eq(LOCAL_DATE_NOW))
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(sut).getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any());

    sut.getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(3));
  }

  @Test
  void shouldGetWeightedAverageWithCpsdAndCpedValidation_whenCheckResult() {
    final var sut = mock(MonthlyReturnsService.class);
    final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .validateCpsd(eq(LOCAL_DATE_NOW))
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW.plusMonths(3)))
        .cutByCpsdIfCpsdEmptyCutByPsd(eq(LOCAL_DATE_NOW))
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(sut).getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any());

    final NavigableMap<LocalDate, BigDecimal> actual = sut.getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns,
        LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(3));

    assertSame(portfolioBaseTotalReturns, actual);
  }

  @Test
  void shouldGetWeightedAverageWithCpedValidation_whenVerifyGetWeightedAverage() {
    final var sut = mock(MonthlyReturnsService.class);
    final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW))
        .validateReturns()
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW))
        .cutByPsd()
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(sut).getWeightedAverageWithCpedValidation(any(), any());

    sut.getWeightedAverageWithCpedValidation(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldGetWeightedAverageWithCpedValidation_whenCheckResult() {
    final var sut = mock(MonthlyReturnsService.class);
    final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturns
        .validateCped(eq(LOCAL_DATE_NOW))
        .cutByCpedIfCpedEmptyCutByPed(eq(LOCAL_DATE_NOW))
        .cutByPsd()
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(sut).getWeightedAverageWithCpedValidation(any(), any());

    final NavigableMap<LocalDate, BigDecimal> actual = sut.getWeightedAverageWithCpedValidation(monthlyReturns,
        LOCAL_DATE_NOW);

    assertSame(portfolioBaseTotalReturns, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyLoad() {
    try (MockedConstruction<Returns> mocked = Mockito.mockConstruction(Returns.class)) {
      final var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
      final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
      final var sut = mock(MonthlyReturnsService.class, withSettings()
          .useConstructor(monthlyReturnsFetcher, mock(FxRatesFetcher.class), gicMonthlyReturnsGenerator));

      final var holdings = mock(List.class);

      when(monthlyReturnsFetcher.fetch(any(), any())).thenReturn(new HashMap<>());
      doCallRealMethod().when(sut).getMonthlyReturns(anyList(), any());

      sut.getMonthlyReturns(holdings, CAD);

      verify(monthlyReturnsFetcher).fetch(holdings, List.of());
    }
  }

  @Test
  void shouldGetMonthlyReturns_whenCheckResult() {
    final var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
    final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    final var sut = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(monthlyReturnsFetcher, mock(FxRatesFetcher.class), gicMonthlyReturnsGenerator));

    final var originalMonthlyReturns = mock(Map.class);
    when(monthlyReturnsFetcher.fetch(any(), any())).thenReturn(originalMonthlyReturns);
    final Returns expected = mock(Returns.class);
    when(sut.getMonthlyReturns(originalMonthlyReturns)).thenReturn(expected);

    doCallRealMethod().when(sut).getMonthlyReturns(anyList(), any());

    final var actual = sut.getMonthlyReturns(mock(List.class), CurrencyType.CAD);

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyGicWasGenerated() {
    final var monthlyReturnsFetcher = mock(SecurityDataFetcher.class);
    final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    final var sut = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(monthlyReturnsFetcher, mock(FxRatesFetcher.class), gicMonthlyReturnsGenerator));

    final var originalMonthlyReturns = mock(Map.class);
    when(monthlyReturnsFetcher.fetch(any(), any())).thenReturn(originalMonthlyReturns);
    final Map gicOriginalMonthlyReturns = mock(Map.class);
    when(gicMonthlyReturnsGenerator.generateGicMonthlyReturns(anyList())).thenReturn(gicOriginalMonthlyReturns);

    doCallRealMethod().when(sut).getMonthlyReturns(anyList(), any());

    sut.getMonthlyReturns(mock(List.class), CurrencyType.CAD);

    verify(originalMonthlyReturns).putAll(gicOriginalMonthlyReturns);
  }

  @Test
  void shouldGetPortfolioMonthlyReturns_whenVerifyGetMonthlyReturns() {
    final var sut = mock(MonthlyReturnsService.class);

    final var holdings = mock(List.class);
    final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList(), any(), any());

    sut.getPortfolioMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    verify(sut).getMonthlyReturns(holdings, CAD);
  }

  @Test
  void shouldGetPortfolioMonthlyReturns_whenVerifyInit() {
    final var sut = mock(MonthlyReturnsService.class);

    final var monthlyReturns = mock(Returns.class, RETURNS_SELF);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    final var fxRates = mock(Map.class);
    when(sut.getFxRates()).thenReturn(fxRates);

    doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList(), any(), any());

    sut.getPortfolioMonthlyReturns(mock(List.class), CAD, ReturnFactorScale.SCALE_OF_TWO);

    final var inOrder = inOrder(monthlyReturns);

    then(monthlyReturns).should(inOrder).setFxRatesConversionComponent(eq(new FxRatesConversionComponent(fxRates,
        CAD)));
    then(monthlyReturns).should(inOrder).setMonthlyReturnsCutComponent(eq(new ReturnsCutComponent()));
    then(monthlyReturns).should(inOrder).setWeightedAverageComponent(eq(new WeightedAverageComponent(
        ReturnFactorScale.SCALE_OF_TWO)));
    then(monthlyReturns).should(inOrder).setCpsdDataValidation(eq(new PortfolioCpsdDataValidation()));
    then(monthlyReturns).should(inOrder).setCpedDataValidation(eq(new PortfolioCpedDataValidation()));

    then(monthlyReturns).shouldHaveNoMoreInteractions();
  }

  @Test
  void shouldGetPortfolioMonthlyReturns_whenCheckResult() {
    final var sut = mock(MonthlyReturnsService.class);

    final var holdings = mock(List.class);
    final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList(), any(), any());

    final var actual = sut.getPortfolioMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    assertSame(monthlyReturns, actual);
  }

  @Test
  void shouldGetBenchmarkMonthlyReturns_whenVerifyGetMonthlyReturns() {
    final var sut = mock(MonthlyReturnsService.class);

    final var holdings = mock(List.class);
    final var benchmarkMonthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(sut).getBenchmarkMonthlyReturns(anyList(), any(), any());

    sut.getBenchmarkMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    verify(sut).getMonthlyReturns(holdings, CAD);
  }

  @Test
  void shouldGetBenchmarkMonthlyReturns_whenVerifyInit() {
    final var sut = mock(MonthlyReturnsService.class);

    final var monthlyReturns = mock(Returns.class, RETURNS_SELF);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    final var fxRates = mock(Map.class);
    when(sut.getFxRates()).thenReturn(fxRates);

    doCallRealMethod().when(sut).getBenchmarkMonthlyReturns(anyList(), any(), any());

    sut.getBenchmarkMonthlyReturns(mock(List.class), CAD, ReturnFactorScale.SCALE_OF_TWO);

    final var inOrder = inOrder(monthlyReturns);

    then(monthlyReturns).should(inOrder).setFxRatesConversionComponent(eq(new FxRatesConversionComponent(fxRates,
        CAD)));
    then(monthlyReturns).should(inOrder).setMonthlyReturnsCutComponent(eq(new ReturnsCutComponent()));
    then(monthlyReturns).should(inOrder).setWeightedAverageComponent(eq(new WeightedAverageComponent(
        ReturnFactorScale.SCALE_OF_TWO)));
    then(monthlyReturns).should(inOrder).setCpsdDataValidation(eq(new BenchmarkCpsdDataValidation()));
    then(monthlyReturns).should(inOrder).setCpedDataValidation(eq(new BenchmarkCpedDataValidation()));

    then(monthlyReturns).shouldHaveNoMoreInteractions();
  }

  @Test
  void shouldGetBenchmarkMonthlyReturns_whenCheckResult() {
    final var sut = mock(MonthlyReturnsService.class);

    final var holdings = mock(List.class);
    final var benchmarkMonthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(sut).getBenchmarkMonthlyReturns(anyList(), any(), any());

    final var actual = sut.getBenchmarkMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    assertSame(benchmarkMonthlyReturns, actual);
  }

  @Test
  void shouldGetFxRates_whenCheckResult() {
    final var fxRatesFetcher = mock(FxRatesFetcher.class);
    final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    final var sut = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(SecurityDataFetcher.class), fxRatesFetcher, gicMonthlyReturnsGenerator));

    final var fxRates = mock(Map.class);
    when(fxRatesFetcher.fetch()).thenReturn(fxRates);
    doCallRealMethod().when(sut).getFxRates();


    final var actual = sut.getFxRates();

    assertSame(fxRates, actual);
  }

}
