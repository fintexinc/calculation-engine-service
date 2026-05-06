package com.fintex.ce.application.returns;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.util.MapUtils;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.PceExceptionCollector;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.ce.model.error.ErrorCode.HOLDING_PSD_OUT_OF_RANGE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MonthlyReturnsTest {

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVeryfyNoActionWhenThisPedIsAfterOtherPed() {
    final var returns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    returns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.minusMonths(1);

    doCallRealMethod().when(other).getPerformanceEndDate();
    doCallRealMethod().when(returns).cutArgumentToTheSameEndDate(any());

    returns.cutArgumentToTheSameEndDate(other);

    verify(returns).cutArgumentToTheSameEndDate(other);
    verify(other).getPerformanceEndDate();
    verifyNoMoreInteractions(returns, other);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenCheckResultWhenThisPedIsAfterOtherPed() {
    final var returns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    returns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.minusMonths(1);

    doCallRealMethod().when(other).getPerformanceEndDate();
    doCallRealMethod().when(returns).cutArgumentToTheSameEndDate(any());

    final var actual = returns.cutArgumentToTheSameEndDate(other);

    assertSame(other, actual);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVerifyCutReturnsByEndDateWhenThisPedIsBeforeOtherPed() {
    final var returns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    final var otherMonthlyReturns = mock(Map.class);
    other.returnsMap = otherMonthlyReturns;

    returns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.plusMonths(1);
    doCallRealMethod().when(other).getPerformanceEndDate();

    doCallRealMethod().when(returns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    returns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(returns).cutArgumentToTheSameEndDate(any());

    returns.cutArgumentToTheSameEndDate(other);

    verify(monthlyReturnsCutComponent).cutReturnsByEndDate(otherMonthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVerifyInitWhenThisPedIsBeforeOtherPed() {
    final var returns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    other.returnsMap = mock(Map.class);

    returns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.plusMonths(1);
    doCallRealMethod().when(other).getPerformanceEndDate();

    doCallRealMethod().when(returns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    returns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(returns).cutArgumentToTheSameEndDate(any());

    returns.cutArgumentToTheSameEndDate(other);

    verify(other).findPedAndPsd();
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenCheckResult2WhenThisPedIsBeforeOtherPed() {
    final var returns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    other.returnsMap = mock(Map.class);

    returns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.plusMonths(1);
    doCallRealMethod().when(other).getPerformanceEndDate();

    doCallRealMethod().when(returns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    final var cutedMonthlyReturns = mock(Map.class);
    when(monthlyReturnsCutComponent.cutReturnsByEndDate(any(), any())).thenReturn(cutedMonthlyReturns);
    returns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    final var clonedOther = mock(ReturnsAggregate.class);
    final var initedOther = mock(ReturnsAggregate.class);
    when(other.findPedAndPsd()).thenReturn(initedOther);
    doCallRealMethod().when(returns).cutArgumentToTheSameEndDate(any());

    final var actual = returns.cutArgumentToTheSameEndDate(other);

    assertSame(initedOther, actual);
  }

  @Test
  void shouldFxRatesApplied_whenVerifyConvert() {
    final var returns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    final var holdingCurrency = mock(Map.class);
    returns.notification = new PceExceptionCollector();

    returns.returnsMap = monthlyReturns;
    returns.holdingCurrencyMap = holdingCurrency;

    doCallRealMethod().when(returns).setFxRateService(any());
    doCallRealMethod().when(returns).setFxRates(any(), any());
    var fxRateService = mock(FxRateService.class);
    returns.setFxRateService(fxRateService);
    returns.setFxRates(Map.of(), Currency.CAD);

    doCallRealMethod().when(returns).fxRatesApplied();

    returns.fxRatesApplied();

    Assertions.assertNotNull(monthlyReturns);
    verify(fxRateService).convertReturns(eq(monthlyReturns), eq(holdingCurrency), any(), any(), any());
  }

  @Test
  void shouldCutByCpedIfCpedEmptyCutByPed_whenVerifyCutReturnsByEndDateWhenCpedIsNotNull() {
    final var returns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.performanceEndDate = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(returns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    returns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(returns).cutByCpedIfCpedEmptyCutByPed(any());

    returns.cutByCpedIfCpedEmptyCutByPed(LOCAL_DATE_NOW);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpedIfCpedEmptyCutByPed_whenVerifyCutReturnsByEndDateWhenCpedIsNull() {
    final var returns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.performanceEndDate = LOCAL_DATE_NOW;

    doCallRealMethod().when(returns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    returns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(returns).cutByCpedIfCpedEmptyCutByPed(any());

    returns.cutByCpedIfCpedEmptyCutByPed(null);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByPed_whenVerifyCutReturnsByEndDate() {
    final var returns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.performanceEndDate = LOCAL_DATE_NOW;

    doCallRealMethod().when(returns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    returns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(returns).cutByPed();

    returns.cutByPed();

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByPsd_whenVerifyCutReturnsByEndDate() {
    final var returns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.performanceStartDate = LOCAL_DATE_NOW;

    doCallRealMethod().when(returns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    returns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(returns).cutByPsd();

    returns.cutByPsd();

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpsdIfCpsdEmptyCutByPsd_whenVerifyCutReturnsByEndDateWhenCpedIsNotNull() {
    final var returns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.performanceStartDate = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(returns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    returns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(returns).cutByCpsdIfCpsdEmptyCutByPsd(any());

    returns.cutByCpsdIfCpsdEmptyCutByPsd(LOCAL_DATE_NOW);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpsdIfCpsdEmptyCutByPsd_whenVerifyCutReturnsByEndDateWhenCpedIsNull() {
    final var returns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.performanceStartDate = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(returns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    returns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(returns).cutByCpsdIfCpsdEmptyCutByPsd(any());

    returns.cutByCpsdIfCpsdEmptyCutByPsd(LOCAL_DATE_NOW);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW.plusMonths(3));
  }

  @Test
  void shouldGetWeightedAverage_whenVerifyGetWeightedAverage() {
    final var returns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.notification = new PceExceptionCollector();

    doCallRealMethod().when(returns).setWeightedAverageComponent(any());
    final var weightedAverageComponent = mock(WeightedAverageComponent.class);
    returns.setWeightedAverageComponent(weightedAverageComponent);

    doCallRealMethod().when(returns).getWeightedAverage();

    returns.getWeightedAverage();

    verify(weightedAverageComponent).calculateWeightedAverage(monthlyReturns);
  }

  @Test
  void shouldGetWeightedAverage_whenCheckResult() {
    final var returns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.notification = new PceExceptionCollector();

    doCallRealMethod().when(returns).setWeightedAverageComponent(any());
    final var weightedAverageComponent = mock(WeightedAverageComponent.class);
    returns.setWeightedAverageComponent(weightedAverageComponent);

    final var portfolioBaseTotalReturns = mock(NavigableMap.class);
    when(weightedAverageComponent.calculateWeightedAverage(any())).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(returns).getWeightedAverage();

    returns.getWeightedAverage();

    verify(weightedAverageComponent).calculateWeightedAverage(monthlyReturns);
  }

  @Test
  void shouldValidateCped_whenVerifyValidatePortfolioCped() {
    final var returns = mock(ReturnsAggregate.class);
    PceExceptionCollector notification = mock(PceExceptionCollector.class);
    returns.notification = notification;

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.performanceEndDate = LOCAL_DATE_NOW.plusMonths(2);
    returns.performanceStartDate = LOCAL_DATE_NOW.plusMonths(1);

    doCallRealMethod().when(returns).setCpedDataValidation(any());
    final var cpedDataValidation = mock(PortfolioCpedDataValidation.class);
    returns.setCpedDataValidation(cpedDataValidation);

    doCallRealMethod().when(returns).validateCped(any());

    returns.validateCped(LOCAL_DATE_NOW);

    verify(cpedDataValidation)
        .validate(eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)), eq(LOCAL_DATE_NOW.plusMonths(2)), same(
            notification));
  }

  @Test
  void shouldValidateCpsd_whenVerifyValidatePortfolioCped() {
    final var returns = mock(ReturnsAggregate.class);
    PceExceptionCollector notification = mock(PceExceptionCollector.class);
    returns.notification = notification;

    final var monthlyReturns = mock(Map.class);
    returns.returnsMap = monthlyReturns;
    returns.performanceEndDate = LOCAL_DATE_NOW.plusMonths(2);
    returns.performanceStartDate = LOCAL_DATE_NOW.plusMonths(1);

    doCallRealMethod().when(returns).setCpsdDataValidation(any());
    final var portfolioCpsdDataValidation = mock(PortfolioCpsdDataValidation.class);
    returns.setCpsdDataValidation(portfolioCpsdDataValidation);

    doCallRealMethod().when(returns).validateCpsd(any());

    returns.validateCpsd(LOCAL_DATE_NOW);

    verify(portfolioCpsdDataValidation)
        .validate(eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)), eq(LOCAL_DATE_NOW.plusMonths(2)), same(
            notification));
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase1() {
    final var returns = new ReturnsAggregate();
    var monthlyReturns = new HashMap<PortfolioHolding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new PortfolioHolding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf1",
        FiIdentifierType.TICKER));
    var h2 = new PortfolioHolding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf2",
        FiIdentifierType.TICKER));
    var h3 = new PortfolioHolding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf1",
        FiIdentifierType.TICKER));
    var h4 = new PortfolioHolding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf2",
        FiIdentifierType.TICKER));
    monthlyReturns.put(h1, new TreeMap<>(Map.of(
        LocalDate.of(2020, 1, 1), ONE,
        LocalDate.of(2020, 2, 1), ONE)));
    monthlyReturns.put(h2, new TreeMap<>(Map.of(
        LocalDate.of(2021, 1, 1), ONE,
        LocalDate.of(2021, 2, 1), ONE)));
    monthlyReturns.put(h3, new TreeMap<>(Map.of(
        LocalDate.of(2018, 1, 1), ONE,
        LocalDate.of(2018, 2, 1), ONE)));
    monthlyReturns.put(h4, new TreeMap<>(Map.of(
        LocalDate.of(2017, 1, 1), ONE,
        LocalDate.of(2017, 2, 1), ONE)));

    returns.returnsMap = monthlyReturns;
    returns.findPedAndPsd();

    var validatedReturns = returns.validateReturns();

    var expectedErrorList = List.of(
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h2),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h1),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h3));
    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase2() {
    final var returns = new ReturnsAggregate();
    var monthlyReturns = new HashMap<PortfolioHolding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new PortfolioHolding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf1",
        FiIdentifierType.TICKER));
    var h2 = new PortfolioHolding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf2",
        FiIdentifierType.TICKER));
    var h3 = new PortfolioHolding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf1",
        FiIdentifierType.TICKER));
    var h4 = new PortfolioHolding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf2",
        FiIdentifierType.TICKER));
    monthlyReturns.put(h1, new TreeMap<>(Map.of(
        LocalDate.of(2020, 1, 1), ONE,
        LocalDate.of(2020, 2, 1), ONE)));
    monthlyReturns.put(h2, new TreeMap<>(Map.of(
        LocalDate.of(2021, 12, 1), ONE,
        LocalDate.of(2022, 1, 1), ONE)));
    monthlyReturns.put(h3, new TreeMap<>(Map.of(
        LocalDate.of(2022, 1, 1), ONE,
        LocalDate.of(2022, 2, 1), ONE)));
    monthlyReturns.put(h4, new TreeMap<>(Map.of(
        LocalDate.of(2017, 1, 1), ONE,
        LocalDate.of(2017, 2, 1), ONE)));

    returns.returnsMap = monthlyReturns;
    returns.findPedAndPsd();

    var validatedReturns = returns.validateReturns();

    var expectedErrorList = List.of(
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h1),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h2),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h3));

    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase3() {
    final var returns = new ReturnsAggregate();
    var monthlyReturns = new HashMap<PortfolioHolding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new PortfolioHolding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf1",
        FiIdentifierType.TICKER));
    var h2 = new PortfolioHolding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf2",
        FiIdentifierType.TICKER));
    var h3 = new PortfolioHolding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf1",
        FiIdentifierType.TICKER));
    var h4 = new PortfolioHolding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf2",
        FiIdentifierType.TICKER));
    monthlyReturns.put(h1, new TreeMap<>(Map.of(
        LocalDate.of(2020, 12, 1), ONE,
        LocalDate.of(2021, 1, 1), ONE)));
    monthlyReturns.put(h2, new TreeMap<>(Map.of(
        LocalDate.of(2021, 1, 1), ONE,
        LocalDate.of(2021, 2, 1), ONE,
        LocalDate.of(2021, 3, 1), ONE)));
    monthlyReturns.put(h3, new TreeMap<>(Map.of(
        LocalDate.of(2021, 2, 1), ONE,
        LocalDate.of(2021, 3, 1), ONE)));
    monthlyReturns.put(h4, new TreeMap<>(Map.of(
        LocalDate.of(2021, 3, 1), ONE,
        LocalDate.of(2021, 4, 1), ONE)));

    returns.returnsMap = monthlyReturns;
    returns.findPedAndPsd();

    var validatedReturns = returns.validateReturns();

    var expectedErrorList = List.of(
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h3),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h4));
    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCase4NoExceptionThrown() {
    final var returns = new ReturnsAggregate();
    var monthlyReturns = new HashMap<PortfolioHolding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new PortfolioHolding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf1",
        FiIdentifierType.TICKER));
    var h2 = new PortfolioHolding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf2",
        FiIdentifierType.TICKER));
    var h3 = new PortfolioHolding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf1",
        FiIdentifierType.TICKER));
    var h4 = new PortfolioHolding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf2",
        FiIdentifierType.TICKER));
    monthlyReturns.put(h1, new TreeMap<>(Map.of(
        LocalDate.of(2020, 1, 1), ONE,
        LocalDate.of(2020, 2, 1), ONE)));
    monthlyReturns.put(h2, new TreeMap<>(Map.of(
        LocalDate.of(2020, 1, 1), ONE,
        LocalDate.of(2020, 2, 1), ONE)));
    monthlyReturns.put(h3, new TreeMap<>(Map.of(
        LocalDate.of(2020, 1, 1), ONE,
        LocalDate.of(2020, 2, 1), ONE)));
    monthlyReturns.put(h4, new TreeMap<>(Map.of(
        LocalDate.of(2020, 1, 1), ONE,
        LocalDate.of(2020, 2, 1), ONE)));

    returns.returnsMap = monthlyReturns;
    returns.findPedAndPsd();

    assertDoesNotThrow(returns::validateReturns);

  }

  @Test
  void shouldGetMonthlyReturns_whenCheckResult() {
    final var returns = mock(ReturnsAggregate.class);
    final var monthlyReturns = Map.of(mock(PortfolioHolding.class), new TreeMap<>(Map.of(LOCAL_DATE_NOW,
        BigDecimal.ONE)));
    returns.returnsMap = monthlyReturns;

    doCallRealMethod().when(returns).getReturnsMap();

    final var actual = returns.getReturnsMap();

    assertNotSame(monthlyReturns, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyCopy() {
    try (var mapUtilsMock = mockStatic(MapUtils.class)) {
      final var returns = mock(ReturnsAggregate.class);
      final var monthlyReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, BigDecimal.ONE));
      final var holdingMonthlyReturns = Map.of(mock(PortfolioHolding.class), monthlyReturns);
      returns.returnsMap = holdingMonthlyReturns;

      doCallRealMethod().when(returns).getReturnsMap();

      final var actual = returns.getReturnsMap();

      mapUtilsMock.verify(() -> MapUtils.copyTreeMap(eq(monthlyReturns), any()));
    }
  }

  @Test
  void shouldFindPsdAmongHoldings_whenCheckResult() {
    final var holding = mock(PortfolioHolding.class);
    final var returns = mock(ReturnsAggregate.class);

    returns.returnsMap = Map.of(holding,
        new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)),
            ONE)));

    doCallRealMethod().when(returns).findPsdAmongMonthlyReturns();
    final LocalDate psd = returns.findPsdAmongMonthlyReturns();

    assertEquals(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), psd);
  }

  @Test
  void shouldFindPedAmongHoldings_whenCheckResult() {
    final var holding = mock(PortfolioHolding.class);
    final var returns = mock(ReturnsAggregate.class);
    returns.returnsMap = Map.of(holding,
        new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)),
            ONE)));

    doCallRealMethod().when(returns).findPedAmongMonthlyReturns();
    doCallRealMethod().when(returns).findPed(any());
    final LocalDate ped = returns.findPedAmongMonthlyReturns();

    assertEquals(toLastDayOfMonth(LOCAL_DATE_NOW), ped);
  }

  @Test
  void shouldRetrieveHoldingCurrencies_whenCheckResult() {
    final var returns = mock(ReturnsAggregate.class);
    returns.notification = new PceExceptionCollector();

    final var holding1 = mock(PortfolioHolding.class);
    final var holding2 = mock(PortfolioHolding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    when(rMonthlyReturns1.getCurrency()).thenReturn(Currency.CAD.name());
    final var rMonthlyReturns2 = mock(ReturnsData.class);
    when(rMonthlyReturns2.getCurrency()).thenReturn(Currency.USD.name());

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(returns).retrieveHoldingCurrencies(anyMap());

    final var actual = returns.retrieveHoldingCurrencies(originalMReturns);

    final var expected = Map.of(holding1, Currency.CAD, holding2, Currency.USD);
    assertEquals(expected, actual);
    assertTrue(returns.notification.getExceptions().isEmpty());
  }

  @Test
  void shouldRetrieveHoldingCurrencies_whenCurrencyIsNull() {
    final var returns = mock(ReturnsAggregate.class);
    returns.notification = new PceExceptionCollector();

    final var holding1 = mock(PortfolioHolding.class);
    final var holding2 = mock(PortfolioHolding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    when(rMonthlyReturns1.getCurrency()).thenReturn(null);
    final var rMonthlyReturns2 = mock(ReturnsData.class);
    when(rMonthlyReturns2.getCurrency()).thenReturn(Currency.USD.name());

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(returns).retrieveHoldingCurrencies(anyMap());

    final var actual = returns.retrieveHoldingCurrencies(originalMReturns);

    final var expected = new HashMap<PortfolioHolding, Currency>();
    expected.put(holding2, Currency.USD);
    assertEquals(expected, actual);
    assertFalse(returns.notification.getExceptions().isEmpty());
  }

  @Test
  void shouldRetrieveReturns_whenCheckResult() {
    final var returns = mock(ReturnsAggregate.class);

    final var holding1 = mock(PortfolioHolding.class);
    final var holding2 = mock(PortfolioHolding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    final var monthlyReturn1 = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    when(rMonthlyReturns1.getReturns()).thenReturn(monthlyReturn1);

    final var rMonthlyReturns2 = mock(ReturnsData.class);
    final var monthlyReturns2 = new TreeMap<>(Map.of(LOCAL_DATE_NOW.plusMonths(1), BigDecimal.TEN));
    when(rMonthlyReturns2.getReturns()).thenReturn(monthlyReturns2);

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(returns).retrieveReturns(anyMap());

    final var actual = returns.retrieveReturns(originalMReturns);

    final var expected = Map.of(holding1, monthlyReturn1, holding2, monthlyReturns2);
    assertEquals(expected, actual);
  }

  @Test
  void shouldMonthlyReturns_whenCheckResult() {
    final var rMonthlyReturns = mock(ReturnsData.class);
    final var monthlyReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    when(rMonthlyReturns.getCurrency()).thenReturn(Currency.CAD.name());
    when(rMonthlyReturns.getReturns()).thenReturn(monthlyReturns);

    final var holding = mock(PortfolioHolding.class);

    final var rMonthlyReturnsMap = Map.of(holding, rMonthlyReturns);

    final var returns = new ReturnsAggregate(rMonthlyReturnsMap);

    assertEquals(Map.of(holding, Currency.CAD), returns.holdingCurrencyMap);
    assertEquals(Map.of(holding, monthlyReturns), returns.returnsMap);
  }

}
