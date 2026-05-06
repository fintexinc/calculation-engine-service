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
    final var monthlyReturns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.minusMonths(1);

    doCallRealMethod().when(other).getPerformanceEndDate();
    doCallRealMethod().when(monthlyReturns).cutArgumentToTheSameEndDate(any());

    monthlyReturns.cutArgumentToTheSameEndDate(other);

    verify(monthlyReturns).cutArgumentToTheSameEndDate(other);
    verify(other).getPerformanceEndDate();
    verifyNoMoreInteractions(monthlyReturns, other);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenCheckResultWhenThisPedIsAfterOtherPed() {
    final var monthlyReturns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.minusMonths(1);

    doCallRealMethod().when(other).getPerformanceEndDate();
    doCallRealMethod().when(monthlyReturns).cutArgumentToTheSameEndDate(any());

    final var actual = monthlyReturns.cutArgumentToTheSameEndDate(other);

    assertSame(other, actual);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVerifyCutReturnsByEndDateWhenThisPedIsBeforeOtherPed() {
    final var monthlyReturns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    final var otherMonthlyReturns = mock(Map.class);
    other.returnsMap = otherMonthlyReturns;

    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.plusMonths(1);
    doCallRealMethod().when(other).getPerformanceEndDate();

    doCallRealMethod().when(monthlyReturns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    monthlyReturns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(monthlyReturns).cutArgumentToTheSameEndDate(any());

    monthlyReturns.cutArgumentToTheSameEndDate(other);

    verify(monthlyReturnsCutComponent).cutReturnsByEndDate(otherMonthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVerifyInitWhenThisPedIsBeforeOtherPed() {
    final var monthlyReturns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    other.returnsMap = mock(Map.class);

    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.plusMonths(1);
    doCallRealMethod().when(other).getPerformanceEndDate();

    doCallRealMethod().when(monthlyReturns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    monthlyReturns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(monthlyReturns).cutArgumentToTheSameEndDate(any());

    monthlyReturns.cutArgumentToTheSameEndDate(other);

    verify(other).findPedAndPsd();
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenCheckResult2WhenThisPedIsBeforeOtherPed() {
    final var monthlyReturns = mock(ReturnsAggregate.class);
    final var other = mock(ReturnsAggregate.class);

    other.returnsMap = mock(Map.class);

    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW;
    other.performanceEndDate = LOCAL_DATE_NOW.plusMonths(1);
    doCallRealMethod().when(other).getPerformanceEndDate();

    doCallRealMethod().when(monthlyReturns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    final var cutedMonthlyReturns = mock(Map.class);
    when(monthlyReturnsCutComponent.cutReturnsByEndDate(any(), any())).thenReturn(cutedMonthlyReturns);
    monthlyReturns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    final var clonedOther = mock(ReturnsAggregate.class);
    final var initedOther = mock(ReturnsAggregate.class);
    when(other.findPedAndPsd()).thenReturn(initedOther);
    doCallRealMethod().when(monthlyReturns).cutArgumentToTheSameEndDate(any());

    final var actual = monthlyReturns.cutArgumentToTheSameEndDate(other);

    assertSame(initedOther, actual);
  }

  @Test
  void shouldFxRatesApplied_whenVerifyConvert() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    final var holdingCurrency = mock(Map.class);
    monthlyReturns.notification = new PceExceptionCollector();

    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.holdingCurrencyMap = holdingCurrency;

    doCallRealMethod().when(monthlyReturns).setFxRateService(any());
    doCallRealMethod().when(monthlyReturns).setFxRates(any(), any());
    var fxRateService = mock(FxRateService.class);
    monthlyReturns.setFxRateService(fxRateService);
    monthlyReturns.setFxRates(Map.of(), Currency.CAD);

    doCallRealMethod().when(monthlyReturns).fxRatesApplied();

    monthlyReturns.fxRatesApplied();

    Assertions.assertNotNull(monthlyReturns);
    verify(fxRateService).convertReturns(eq(monthlyReturns), eq(holdingCurrency), any(), any(), any());
  }

  @Test
  void shouldCutByCpedIfCpedEmptyCutByPed_whenVerifyCutReturnsByEndDateWhenCpedIsNotNull() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(monthlyReturns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    monthlyReturns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(monthlyReturns).cutByCpedIfCpedEmptyCutByPed(any());

    monthlyReturns.cutByCpedIfCpedEmptyCutByPed(LOCAL_DATE_NOW);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpedIfCpedEmptyCutByPed_whenVerifyCutReturnsByEndDateWhenCpedIsNull() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW;

    doCallRealMethod().when(monthlyReturns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    monthlyReturns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(monthlyReturns).cutByCpedIfCpedEmptyCutByPed(any());

    monthlyReturns.cutByCpedIfCpedEmptyCutByPed(null);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByPed_whenVerifyCutReturnsByEndDate() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW;

    doCallRealMethod().when(monthlyReturns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    monthlyReturns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(monthlyReturns).cutByPed();

    monthlyReturns.cutByPed();

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByPsd_whenVerifyCutReturnsByEndDate() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.performanceStartDate = LOCAL_DATE_NOW;

    doCallRealMethod().when(monthlyReturns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    monthlyReturns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(monthlyReturns).cutByPsd();

    monthlyReturns.cutByPsd();

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpsdIfCpsdEmptyCutByPsd_whenVerifyCutReturnsByEndDateWhenCpedIsNotNull() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.performanceStartDate = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(monthlyReturns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    monthlyReturns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(monthlyReturns).cutByCpsdIfCpsdEmptyCutByPsd(any());

    monthlyReturns.cutByCpsdIfCpsdEmptyCutByPsd(LOCAL_DATE_NOW);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpsdIfCpsdEmptyCutByPsd_whenVerifyCutReturnsByEndDateWhenCpedIsNull() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.performanceStartDate = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(monthlyReturns).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    monthlyReturns.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(monthlyReturns).cutByCpsdIfCpsdEmptyCutByPsd(any());

    monthlyReturns.cutByCpsdIfCpsdEmptyCutByPsd(LOCAL_DATE_NOW);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW.plusMonths(3));
  }

  @Test
  void shouldGetWeightedAverage_whenVerifyGetWeightedAverage() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.notification = new PceExceptionCollector();

    doCallRealMethod().when(monthlyReturns).setWeightedAverageComponent(any());
    final var weightedAverageComponent = mock(WeightedAverageComponent.class);
    monthlyReturns.setWeightedAverageComponent(weightedAverageComponent);

    doCallRealMethod().when(monthlyReturns).getWeightedAverage();

    monthlyReturns.getWeightedAverage();

    verify(weightedAverageComponent).calculateWeightedAverage(monthlyReturns);
  }

  @Test
  void shouldGetWeightedAverage_whenCheckResult() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.notification = new PceExceptionCollector();

    doCallRealMethod().when(monthlyReturns).setWeightedAverageComponent(any());
    final var weightedAverageComponent = mock(WeightedAverageComponent.class);
    monthlyReturns.setWeightedAverageComponent(weightedAverageComponent);

    final var portfolioBaseTotalReturns = mock(NavigableMap.class);
    when(weightedAverageComponent.calculateWeightedAverage(any())).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(monthlyReturns).getWeightedAverage();

    monthlyReturns.getWeightedAverage();

    verify(weightedAverageComponent).calculateWeightedAverage(monthlyReturns);
  }

  @Test
  void shouldValidateCped_whenVerifyValidatePortfolioCped() {
    final var monthlyReturns = mock(ReturnsAggregate.class);
    PceExceptionCollector notification = mock(PceExceptionCollector.class);
    monthlyReturns.notification = notification;

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW.plusMonths(2);
    monthlyReturns.performanceStartDate = LOCAL_DATE_NOW.plusMonths(1);

    doCallRealMethod().when(monthlyReturns).setCpedDataValidation(any());
    final var cpedDataValidation = mock(PortfolioCpedDataValidation.class);
    monthlyReturns.setCpedDataValidation(cpedDataValidation);

    doCallRealMethod().when(monthlyReturns).validateCped(any());

    monthlyReturns.validateCped(LOCAL_DATE_NOW);

    verify(cpedDataValidation)
        .validate(eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)), eq(LOCAL_DATE_NOW.plusMonths(2)), same(
            notification));
  }

  @Test
  void shouldValidateCpsd_whenVerifyValidatePortfolioCped() {
    final var monthlyReturns = mock(ReturnsAggregate.class);
    PceExceptionCollector notification = mock(PceExceptionCollector.class);
    monthlyReturns.notification = notification;

    final var monthlyReturns = mock(Map.class);
    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.performanceEndDate = LOCAL_DATE_NOW.plusMonths(2);
    monthlyReturns.performanceStartDate = LOCAL_DATE_NOW.plusMonths(1);

    doCallRealMethod().when(monthlyReturns).setCpsdDataValidation(any());
    final var portfolioCpsdDataValidation = mock(PortfolioCpsdDataValidation.class);
    monthlyReturns.setCpsdDataValidation(portfolioCpsdDataValidation);

    doCallRealMethod().when(monthlyReturns).validateCpsd(any());

    monthlyReturns.validateCpsd(LOCAL_DATE_NOW);

    verify(portfolioCpsdDataValidation)
        .validate(eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)), eq(LOCAL_DATE_NOW.plusMonths(2)), same(
            notification));
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase1() {
    final var monthlyReturns = new ReturnsAggregate();
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

    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.findPedAndPsd();

    var validatedReturns = monthlyReturns.validateReturns();

    var expectedErrorList = List.of(
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h2),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h1),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h3));
    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase2() {
    final var monthlyReturns = new ReturnsAggregate();
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

    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.findPedAndPsd();

    var validatedReturns = monthlyReturns.validateReturns();

    var expectedErrorList = List.of(
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h1),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h2),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h3));

    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase3() {
    final var monthlyReturns = new ReturnsAggregate();
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

    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.findPedAndPsd();

    var validatedReturns = monthlyReturns.validateReturns();

    var expectedErrorList = List.of(
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h3),
        HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(h4));
    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCase4NoExceptionThrown() {
    final var monthlyReturns = new ReturnsAggregate();
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

    monthlyReturns.returnsMap = monthlyReturns;
    monthlyReturns.findPedAndPsd();

    assertDoesNotThrow(monthlyReturns::validateReturns);

  }

  @Test
  void shouldGetMonthlyReturns_whenCheckResult() {
    final var monthlyReturns = mock(ReturnsAggregate.class);
    final var monthlyReturns = Map.of(mock(PortfolioHolding.class), new TreeMap<>(Map.of(LOCAL_DATE_NOW,
        BigDecimal.ONE)));
    monthlyReturns.returnsMap = monthlyReturns;

    doCallRealMethod().when(monthlyReturns).getReturnsMap();

    final var actual = monthlyReturns.getReturnsMap();

    assertNotSame(monthlyReturns, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyCopy() {
    try (var mapUtilsMock = mockStatic(MapUtils.class)) {
      final var monthlyReturns = mock(ReturnsAggregate.class);
      final var monthlyReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, BigDecimal.ONE));
      final var holdingMonthlyReturns = Map.of(mock(PortfolioHolding.class), monthlyReturns);
      monthlyReturns.returnsMap = holdingMonthlyReturns;

      doCallRealMethod().when(monthlyReturns).getReturnsMap();

      final var actual = monthlyReturns.getReturnsMap();

      mapUtilsMock.verify(() -> MapUtils.copyTreeMap(eq(monthlyReturns), any()));
    }
  }

  @Test
  void shouldFindPsdAmongHoldings_whenCheckResult() {
    final var holding = mock(PortfolioHolding.class);
    final var monthlyReturns = mock(ReturnsAggregate.class);

    monthlyReturns.returnsMap = Map.of(holding,
        new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)),
            ONE)));

    doCallRealMethod().when(monthlyReturns).findPsdAmongMonthlyReturns();
    final LocalDate psd = monthlyReturns.findPsdAmongMonthlyReturns();

    assertEquals(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), psd);
  }

  @Test
  void shouldFindPedAmongHoldings_whenCheckResult() {
    final var holding = mock(PortfolioHolding.class);
    final var monthlyReturns = mock(ReturnsAggregate.class);
    monthlyReturns.returnsMap = Map.of(holding,
        new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)),
            ONE)));

    doCallRealMethod().when(monthlyReturns).findPedAmongMonthlyReturns();
    doCallRealMethod().when(monthlyReturns).findPed(any());
    final LocalDate ped = monthlyReturns.findPedAmongMonthlyReturns();

    assertEquals(toLastDayOfMonth(LOCAL_DATE_NOW), ped);
  }

  @Test
  void shouldRetrieveHoldingCurrencies_whenCheckResult() {
    final var monthlyReturns = mock(ReturnsAggregate.class);
    monthlyReturns.notification = new PceExceptionCollector();

    final var holding1 = mock(PortfolioHolding.class);
    final var holding2 = mock(PortfolioHolding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    when(rMonthlyReturns1.getCurrency()).thenReturn(Currency.CAD.name());
    final var rMonthlyReturns2 = mock(ReturnsData.class);
    when(rMonthlyReturns2.getCurrency()).thenReturn(Currency.USD.name());

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(monthlyReturns).retrieveHoldingCurrencies(anyMap());

    final var actual = monthlyReturns.retrieveHoldingCurrencies(originalMReturns);

    final var expected = Map.of(holding1, Currency.CAD, holding2, Currency.USD);
    assertEquals(expected, actual);
    assertTrue(monthlyReturns.notification.getExceptions().isEmpty());
  }

  @Test
  void shouldRetrieveHoldingCurrencies_whenCurrencyIsNull() {
    final var monthlyReturns = mock(ReturnsAggregate.class);
    monthlyReturns.notification = new PceExceptionCollector();

    final var holding1 = mock(PortfolioHolding.class);
    final var holding2 = mock(PortfolioHolding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    when(rMonthlyReturns1.getCurrency()).thenReturn(null);
    final var rMonthlyReturns2 = mock(ReturnsData.class);
    when(rMonthlyReturns2.getCurrency()).thenReturn(Currency.USD.name());

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(monthlyReturns).retrieveHoldingCurrencies(anyMap());

    final var actual = monthlyReturns.retrieveHoldingCurrencies(originalMReturns);

    final var expected = new HashMap<PortfolioHolding, Currency>();
    expected.put(holding2, Currency.USD);
    assertEquals(expected, actual);
    assertFalse(monthlyReturns.notification.getExceptions().isEmpty());
  }

  @Test
  void shouldRetrieveReturns_whenCheckResult() {
    final var monthlyReturns = mock(ReturnsAggregate.class);

    final var holding1 = mock(PortfolioHolding.class);
    final var holding2 = mock(PortfolioHolding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    final var monthlyReturn1 = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    when(rMonthlyReturns1.getReturns()).thenReturn(monthlyReturn1);

    final var rMonthlyReturns2 = mock(ReturnsData.class);
    final var monthlyReturns2 = new TreeMap<>(Map.of(LOCAL_DATE_NOW.plusMonths(1), BigDecimal.TEN));
    when(rMonthlyReturns2.getReturns()).thenReturn(monthlyReturns2);

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(monthlyReturns).retrieveReturns(anyMap());

    final var actual = monthlyReturns.retrieveReturns(originalMReturns);

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

    final var monthlyReturns = new ReturnsAggregate(rMonthlyReturnsMap);

    assertEquals(Map.of(holding, Currency.CAD), monthlyReturns.holdingCurrencyMap);
    assertEquals(Map.of(holding, monthlyReturns), monthlyReturns.returnsMap);
  }

}
