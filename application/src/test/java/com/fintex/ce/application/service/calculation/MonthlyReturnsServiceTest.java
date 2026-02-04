package com.fintex.ce.application.service.calculation;

import com.fintex.ce.adapter.cache.FxRatesCacheStorage;
import com.fintex.ce.adapter.cache.MonthlyReturnsCacheStorage;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.validation.BenchmarkCpedDataValidation;
import com.fintex.ce.application.validation.BenchmarkCpsdDataValidation;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.monthlyreturns.FxRatesConversionComponent;
import com.fintex.ce.monthlyreturns.MonthlyReturnsGenerator;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.monthlyreturns.ReturnsCutComponent;
import com.fintex.ce.monthlyreturns.WeightedAverageComponent;
import com.fintex.ce.util.ReturnFactorScale;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.domain.enumeration.Currency.CAD;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
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
  void getWeightedAverageWithCpsdAndCpedValidation_verifyGetWeightedAverage() {
    // SETUP
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

    // ACT
    sut.getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(3));
  }

  @Test
  void getWeightedAverageWithCpsdAndCpedValidation_checkResult() {
    // SETUP
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

    // ACT
    final NavigableMap<LocalDate, BigDecimal> actual = sut.getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns,
        LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(3));

    // VERIFY
    assertSame(portfolioBaseTotalReturns, actual);
  }

  @Test
  void getWeightedAverageWithCpedValidation_verifyGetWeightedAverage() {
    // SETUP
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

    // ACT
    sut.getWeightedAverageWithCpedValidation(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void getWeightedAverageWithCpedValidation_checkResult() {
    // SETUP
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

    // ACT
    final NavigableMap<LocalDate, BigDecimal> actual = sut.getWeightedAverageWithCpedValidation(monthlyReturns,
        LOCAL_DATE_NOW);

    // VERIFY
    assertSame(portfolioBaseTotalReturns, actual);
  }

  @Test
  void getMonthlyReturns_verifyLoad() {
    // SETUP
    try (MockedConstruction<Returns> mocked = Mockito.mockConstruction(Returns.class)) {
      final var monthlyReturnsCacheStorage = mock(MonthlyReturnsCacheStorage.class);
      final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
      final var sut = mock(MonthlyReturnsService.class, withSettings()
          .useConstructor(monthlyReturnsCacheStorage, mock(FxRatesCacheStorage.class), gicMonthlyReturnsGenerator));

      final var holdings = mock(List.class);

      doCallRealMethod().when(sut).getMonthlyReturns(anyList(), any());

      // ACT
      sut.getMonthlyReturns(holdings, CAD);

      // VERIFY
      verify(monthlyReturnsCacheStorage).load(holdings, List.of(), List.of(), new ParamHolderDTO(CAD));
    }
  }

  @Test
  void getMonthlyReturns_checkResult() {
    // SETUP
    final var monthlyReturnsCacheStorage = mock(MonthlyReturnsCacheStorage.class);
    final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    final var sut = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(monthlyReturnsCacheStorage, mock(FxRatesCacheStorage.class), gicMonthlyReturnsGenerator));

    final var originalMonthlyReturns = mock(Map.class);
    when(monthlyReturnsCacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(originalMonthlyReturns);
    final Returns expected = mock(Returns.class);
    when(sut.getMonthlyReturns(originalMonthlyReturns)).thenReturn(expected);

    doCallRealMethod().when(sut).getMonthlyReturns(anyList(), any());

    // ACT
    final var actual = sut.getMonthlyReturns(mock(List.class), Currency.CAD);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void getMonthlyReturns_verifyGicWasGenerated() {
    // SETUP
    final var monthlyReturnsCacheStorage = mock(MonthlyReturnsCacheStorage.class);
    final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    final var sut = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(monthlyReturnsCacheStorage, mock(FxRatesCacheStorage.class), gicMonthlyReturnsGenerator));

    final var originalMonthlyReturns = mock(Map.class);
    when(monthlyReturnsCacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(originalMonthlyReturns);
    final Map gicOriginalMonthlyReturns = mock(Map.class);
    when(gicMonthlyReturnsGenerator.generateGicMonthlyReturns(anyList())).thenReturn(gicOriginalMonthlyReturns);

    doCallRealMethod().when(sut).getMonthlyReturns(anyList(), any());

    // ACT
    sut.getMonthlyReturns(mock(List.class), Currency.CAD);

    // VERIFY
    verify(originalMonthlyReturns).putAll(gicOriginalMonthlyReturns);
  }

  @Test
  void getPortfolioMonthlyReturns_verifyGetMonthlyReturns() {
    // SETUP
    final var sut = mock(MonthlyReturnsService.class);

    final var holdings = mock(List.class);
    final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList(), any(), any());

    // ACT
    sut.getPortfolioMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    // VERIFY
    verify(sut).getMonthlyReturns(holdings, CAD);
  }

  @Test
  void getPortfolioMonthlyReturns_verifyInit() {
    // SETUP
    final var sut = mock(MonthlyReturnsService.class);

    final var monthlyReturns = mock(Returns.class, RETURNS_SELF);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    final var fxRates = mock(Map.class);
    when(sut.getFxRates()).thenReturn(fxRates);

    doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList(), any(), any());

    // ACT
    sut.getPortfolioMonthlyReturns(mock(List.class), CAD, ReturnFactorScale.SCALE_OF_TWO);

    // VERIFY
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
  void getPortfolioMonthlyReturns_checkResult() {
    // SETUP
    final var sut = mock(MonthlyReturnsService.class);

    final var holdings = mock(List.class);
    final var monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).getPortfolioMonthlyReturns(anyList(), any(), any());

    // ACT
    final var actual = sut.getPortfolioMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    // VERIFY
    assertSame(monthlyReturns, actual);
  }

  @Test
  void getBenchmarkMonthlyReturns_verifyGetMonthlyReturns() {
    // SETUP
    final var sut = mock(MonthlyReturnsService.class);

    final var holdings = mock(List.class);
    final var benchmarkMonthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(sut).getBenchmarkMonthlyReturns(anyList(), any(), any());

    // ACT
    sut.getBenchmarkMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    // VERIFY
    verify(sut).getMonthlyReturns(holdings, CAD);
  }

  @Test
  void getBenchmarkMonthlyReturns_verifyInit() {
    // SETUP
    final var sut = mock(MonthlyReturnsService.class);

    final var monthlyReturns = mock(Returns.class, RETURNS_SELF);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(monthlyReturns);

    final var fxRates = mock(Map.class);
    when(sut.getFxRates()).thenReturn(fxRates);

    doCallRealMethod().when(sut).getBenchmarkMonthlyReturns(anyList(), any(), any());

    // ACT
    sut.getBenchmarkMonthlyReturns(mock(List.class), CAD, ReturnFactorScale.SCALE_OF_TWO);

    // VERIFY
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
  void getBenchmarkMonthlyReturns_checkResult() {
    // SETUP
    final var sut = mock(MonthlyReturnsService.class);

    final var holdings = mock(List.class);
    final var benchmarkMonthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    when(sut.getMonthlyReturns(anyList(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(sut).getBenchmarkMonthlyReturns(anyList(), any(), any());

    // ACT
    final var actual = sut.getBenchmarkMonthlyReturns(holdings, CAD, ReturnFactorScale.SCALE_OF_TWO);

    // VERIFY
    assertSame(benchmarkMonthlyReturns, actual);
  }

  @Test
  void getFxRates_checkResult() {
    // SETUP
    final var fxRatesCacheStorage = mock(FxRatesCacheStorage.class);
    final var gicMonthlyReturnsGenerator = mock(MonthlyReturnsGenerator.class);
    final var sut = mock(MonthlyReturnsService.class, withSettings()
        .useConstructor(mock(MonthlyReturnsCacheStorage.class), fxRatesCacheStorage, gicMonthlyReturnsGenerator));

    final var fxRates = mock(Map.class);
    when(fxRatesCacheStorage.loadFxRates()).thenReturn(fxRates);
    doCallRealMethod().when(sut).getFxRates();

    // ACT

    final var actual = sut.getFxRates();

    // VERIFY
    assertSame(fxRates, actual);
  }

}
