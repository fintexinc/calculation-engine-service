package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.ReturnsData;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.MapUtils;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CurrencyType;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

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
import static com.fintex.ce.domain.constant.BigDecimalConstants.TWO;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_RRC_MR_002;
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
    final var sut = mock(Returns.class);
    final var other = mock(Returns.class);

    sut.ped = LOCAL_DATE_NOW;
    other.ped = LOCAL_DATE_NOW.minusMonths(1);

    doCallRealMethod().when(other).getPed();
    doCallRealMethod().when(sut).cutArgumentToTheSameEndDate(any());

    sut.cutArgumentToTheSameEndDate(other);

    verify(sut).cutArgumentToTheSameEndDate(other);
    verify(other).getPed();
    verifyNoMoreInteractions(sut, other);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenCheckResultWhenThisPedIsAfterOtherPed() {
    final var sut = mock(Returns.class);
    final var other = mock(Returns.class);

    sut.ped = LOCAL_DATE_NOW;
    other.ped = LOCAL_DATE_NOW.minusMonths(1);

    doCallRealMethod().when(other).getPed();
    doCallRealMethod().when(sut).cutArgumentToTheSameEndDate(any());

    final var actual = sut.cutArgumentToTheSameEndDate(other);

    assertSame(other, actual);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVerifyCutReturnsByEndDateWhenThisPedIsBeforeOtherPed() {
    final var sut = mock(Returns.class);
    final var other = mock(Returns.class);

    final var otherMonthlyReturns = mock(Map.class);
    other.returnsMap = otherMonthlyReturns;

    sut.ped = LOCAL_DATE_NOW;
    other.ped = LOCAL_DATE_NOW.plusMonths(1);
    doCallRealMethod().when(other).getPed();

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutArgumentToTheSameEndDate(any());

    sut.cutArgumentToTheSameEndDate(other);

    verify(monthlyReturnsCutComponent).cutReturnsByEndDate(otherMonthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVerifyInitWhenThisPedIsBeforeOtherPed() {
    final var sut = mock(Returns.class);
    final var other = mock(Returns.class);

    other.returnsMap = mock(Map.class);

    sut.ped = LOCAL_DATE_NOW;
    other.ped = LOCAL_DATE_NOW.plusMonths(1);
    doCallRealMethod().when(other).getPed();

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutArgumentToTheSameEndDate(any());

    sut.cutArgumentToTheSameEndDate(other);

    verify(other).findPedAndPsd();
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenCheckResult2WhenThisPedIsBeforeOtherPed() {
    final var sut = mock(Returns.class);
    final var other = mock(Returns.class);

    other.returnsMap = mock(Map.class);

    sut.ped = LOCAL_DATE_NOW;
    other.ped = LOCAL_DATE_NOW.plusMonths(1);
    doCallRealMethod().when(other).getPed();

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    final var cutedMonthlyReturns = mock(Map.class);
    when(monthlyReturnsCutComponent.cutReturnsByEndDate(any(), any())).thenReturn(cutedMonthlyReturns);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    final var clonedOther = mock(Returns.class);
    final var initedOther = mock(Returns.class);
    when(other.findPedAndPsd()).thenReturn(initedOther);
    doCallRealMethod().when(sut).cutArgumentToTheSameEndDate(any());

    final var actual = sut.cutArgumentToTheSameEndDate(other);

    assertSame(initedOther, actual);
  }

  @Test
  void shouldFxRatesApplied_whenVerifyConvert() {
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    final var holdingCurrency = mock(Map.class);
    sut.notification = new Notification();

    sut.returnsMap = monthlyReturns;
    sut.holdingCurrencyMap = holdingCurrency;

    doCallRealMethod().when(sut).setFxRatesConversionComponent(any());
    final var fxRatesConversionComponent = mock(FxRatesConversionComponent.class);
    sut.setFxRatesConversionComponent(fxRatesConversionComponent);

    doCallRealMethod().when(sut).fxRatesApplied();

    sut.fxRatesApplied();

    Assertions.assertNotNull(monthlyReturns);
    fxRatesConversionComponent.convert(monthlyReturns, holdingCurrency);
  }

  @Test
  void shouldCutByCpedIfCpedEmptyCutByPed_whenVerifyCutReturnsByEndDateWhenCpedIsNotNull() {
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.ped = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByCpedIfCpedEmptyCutByPed(any());

    sut.cutByCpedIfCpedEmptyCutByPed(LOCAL_DATE_NOW);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpedIfCpedEmptyCutByPed_whenVerifyCutReturnsByEndDateWhenCpedIsNull() {
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.ped = LOCAL_DATE_NOW;

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByCpedIfCpedEmptyCutByPed(any());

    sut.cutByCpedIfCpedEmptyCutByPed(null);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByPed_whenVerifyCutReturnsByEndDate() {
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.ped = LOCAL_DATE_NOW;

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByPed();

    sut.cutByPed();

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByPsd_whenVerifyCutReturnsByEndDate() {
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.psd = LOCAL_DATE_NOW;

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByPsd();

    sut.cutByPsd();

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpsdIfCpsdEmptyCutByPsd_whenVerifyCutReturnsByEndDateWhenCpedIsNotNull() {
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.psd = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByCpsdIfCpsdEmptyCutByPsd(any());

    sut.cutByCpsdIfCpsdEmptyCutByPsd(LOCAL_DATE_NOW);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpsdIfCpsdEmptyCutByPsd_whenVerifyCutReturnsByEndDateWhenCpedIsNull() {
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.psd = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByCpsdIfCpsdEmptyCutByPsd(any());

    sut.cutByCpsdIfCpsdEmptyCutByPsd(LOCAL_DATE_NOW);

    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW.plusMonths(3));
  }

  @Test
  void shouldGetWeightedAverage_whenVerifyGetWeightedAverage() {
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.notification = new Notification();

    doCallRealMethod().when(sut).setWeightedAverageComponent(any());
    final var weightedAverageComponent = mock(WeightedAverageComponent.class);
    sut.setWeightedAverageComponent(weightedAverageComponent);

    doCallRealMethod().when(sut).getWeightedAverage();

    sut.getWeightedAverage();

    verify(weightedAverageComponent).calculateWeightedAverage(monthlyReturns);
  }

  @Test
  void shouldGetWeightedAverage_whenCheckResult() {
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.notification = new Notification();

    doCallRealMethod().when(sut).setWeightedAverageComponent(any());
    final var weightedAverageComponent = mock(WeightedAverageComponent.class);
    sut.setWeightedAverageComponent(weightedAverageComponent);

    final var portfolioBaseTotalReturns = mock(NavigableMap.class);
    when(weightedAverageComponent.calculateWeightedAverage(any())).thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(sut).getWeightedAverage();

    sut.getWeightedAverage();

    verify(weightedAverageComponent).calculateWeightedAverage(monthlyReturns);
  }

  @Test
  void shouldValidateCped_whenVerifyValidatePortfolioCped() {
    final var sut = mock(Returns.class);
    Notification notification = mock(Notification.class);
    sut.notification = notification;

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.ped = LOCAL_DATE_NOW.plusMonths(2);
    sut.psd = LOCAL_DATE_NOW.plusMonths(1);

    doCallRealMethod().when(sut).setCpedDataValidation(any());
    final var cpedDataValidation = mock(PortfolioCpedDataValidation.class);
    sut.setCpedDataValidation(cpedDataValidation);

    doCallRealMethod().when(sut).validateCped(any());

    sut.validateCped(LOCAL_DATE_NOW);

    verify(cpedDataValidation)
        .validate(eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)), eq(LOCAL_DATE_NOW.plusMonths(2)), same(
            notification));
  }

  @Test
  void shouldValidateCpsd_whenVerifyValidatePortfolioCped() {
    final var sut = mock(Returns.class);
    Notification notification = mock(Notification.class);
    sut.notification = notification;

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.ped = LOCAL_DATE_NOW.plusMonths(2);
    sut.psd = LOCAL_DATE_NOW.plusMonths(1);

    doCallRealMethod().when(sut).setCpsdDataValidation(any());
    final var portfolioCpsdDataValidation = mock(PortfolioCpsdDataValidation.class);
    sut.setCpsdDataValidation(portfolioCpsdDataValidation);

    doCallRealMethod().when(sut).validateCpsd(any());

    sut.validateCpsd(LOCAL_DATE_NOW);

    verify(portfolioCpsdDataValidation)
        .validate(eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)), eq(LOCAL_DATE_NOW.plusMonths(2)), same(
            notification));
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase1() {
    final var sut = new Returns();
    var monthlyReturns = new HashMap<Holding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new Holding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf1",
        FiIdentifierType.TICKER));
    var h2 = new Holding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf2",
        FiIdentifierType.TICKER));
    var h3 = new Holding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf1",
        FiIdentifierType.TICKER));
    var h4 = new Holding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf2",
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

    sut.returnsMap = monthlyReturns;
    sut.findPedAndPsd();

    var validatedReturns = sut.validateReturns();

    var expectedErrorList = List.of(
        ERR_RRC_MR_002.error(h2),
        ERR_RRC_MR_002.error(h1),
        ERR_RRC_MR_002.error(h3));
    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase2() {
    final var sut = new Returns();
    var monthlyReturns = new HashMap<Holding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new Holding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf1",
        FiIdentifierType.TICKER));
    var h2 = new Holding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf2",
        FiIdentifierType.TICKER));
    var h3 = new Holding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf1",
        FiIdentifierType.TICKER));
    var h4 = new Holding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf2",
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

    sut.returnsMap = monthlyReturns;
    sut.findPedAndPsd();

    var validatedReturns = sut.validateReturns();

    var expectedErrorList = List.of(
        ERR_RRC_MR_002.error(h1),
        ERR_RRC_MR_002.error(h2),
        ERR_RRC_MR_002.error(h3));

    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase3() {
    final var sut = new Returns();
    var monthlyReturns = new HashMap<Holding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new Holding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf1",
        FiIdentifierType.TICKER));
    var h2 = new Holding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf2",
        FiIdentifierType.TICKER));
    var h3 = new Holding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf1",
        FiIdentifierType.TICKER));
    var h4 = new Holding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf2",
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

    sut.returnsMap = monthlyReturns;
    sut.findPedAndPsd();

    var validatedReturns = sut.validateReturns();

    var expectedErrorList = List.of(
        ERR_RRC_MR_002.error(h3),
        ERR_RRC_MR_002.error(h4));
    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCase4NoExceptionThrown() {
    final var sut = new Returns();
    var monthlyReturns = new HashMap<Holding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new Holding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf1",
        FiIdentifierType.TICKER));
    var h2 = new Holding(TWO, FinancialInstrumentType.ETF_CANADA, new SecurityIdentifier("cEtf2",
        FiIdentifierType.TICKER));
    var h3 = new Holding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf1",
        FiIdentifierType.TICKER));
    var h4 = new Holding(ONE, FinancialInstrumentType.ETF_US, new SecurityIdentifier("usEtf2",
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

    sut.returnsMap = monthlyReturns;
    sut.findPedAndPsd();

    assertDoesNotThrow(sut::validateReturns);

  }

  @Test
  void shouldGetMonthlyReturns_whenCheckResult() {
    final var sut = mock(Returns.class);
    final var monthlyReturns = Map.of(mock(Holding.class), new TreeMap<>(Map.of(LOCAL_DATE_NOW, BigDecimal.ONE)));
    sut.returnsMap = monthlyReturns;

    doCallRealMethod().when(sut).getReturnsMap();

    final var actual = sut.getReturnsMap();

    assertNotSame(monthlyReturns, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyCopy() {
    try (var mapUtilsMock = mockStatic(MapUtils.class)) {
      final var sut = mock(Returns.class);
      final var monthlyReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, BigDecimal.ONE));
      final var holdingMonthlyReturns = Map.of(mock(Holding.class), monthlyReturns);
      sut.returnsMap = holdingMonthlyReturns;

      doCallRealMethod().when(sut).getReturnsMap();

      final var actual = sut.getReturnsMap();

      mapUtilsMock.verify(() -> MapUtils.copyTreeMap(eq(monthlyReturns), any()));
    }
  }

  @Test
  void shouldFindPsdAmongHoldings_whenCheckResult() {
    final var holding = mock(Holding.class);
    final var sut = mock(Returns.class);

    sut.returnsMap = Map.of(holding,
        new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)),
            ONE)));

    doCallRealMethod().when(sut).findPsdAmongMonthlyReturns();
    final LocalDate psd = sut.findPsdAmongMonthlyReturns();

    assertEquals(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), psd);
  }

  @Test
  void shouldFindPedAmongHoldings_whenCheckResult() {
    final var holding = mock(Holding.class);
    final var sut = mock(Returns.class);
    sut.returnsMap = Map.of(holding,
        new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)),
            ONE)));

    doCallRealMethod().when(sut).findPedAmongMonthlyReturns();
    doCallRealMethod().when(sut).findPed(any());
    final LocalDate ped = sut.findPedAmongMonthlyReturns();

    assertEquals(toLastDayOfMonth(LOCAL_DATE_NOW), ped);
  }

  @Test
  void shouldRetrieveHoldingCurrencies_whenCheckResult() {
    final var sut = mock(Returns.class);
    sut.notification = new Notification();

    final var holding1 = mock(Holding.class);
    final var holding2 = mock(Holding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    when(rMonthlyReturns1.getCurrency()).thenReturn(CurrencyType.CAD.name());
    final var rMonthlyReturns2 = mock(ReturnsData.class);
    when(rMonthlyReturns2.getCurrency()).thenReturn(CurrencyType.USD.name());

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(sut).retrieveHoldingCurrencies(anyMap());

    final var actual = sut.retrieveHoldingCurrencies(originalMReturns);

    final var expected = Map.of(holding1, CurrencyType.CAD, holding2, CurrencyType.USD);
    assertEquals(expected, actual);
    assertTrue(sut.notification.getErrors().isEmpty());
  }

  @Test
  void shouldRetrieveHoldingCurrencies_whenCurrencyIsNull() {
    final var sut = mock(Returns.class);
    sut.notification = new Notification();

    final var holding1 = mock(Holding.class);
    final var holding2 = mock(Holding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    when(rMonthlyReturns1.getCurrency()).thenReturn(null);
    final var rMonthlyReturns2 = mock(ReturnsData.class);
    when(rMonthlyReturns2.getCurrency()).thenReturn(CurrencyType.USD.name());

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(sut).retrieveHoldingCurrencies(anyMap());

    final var actual = sut.retrieveHoldingCurrencies(originalMReturns);

    final var expected = new HashMap<Holding, CurrencyType>();
    expected.put(holding2, CurrencyType.USD);
    assertEquals(expected, actual);
    assertFalse(sut.notification.getErrors().isEmpty());
  }

  @Test
  void shouldRetrieveReturns_whenCheckResult() {
    final var sut = mock(Returns.class);

    final var holding1 = mock(Holding.class);
    final var holding2 = mock(Holding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    final var monthlyReturn1 = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    when(rMonthlyReturns1.getReturns()).thenReturn(monthlyReturn1);

    final var rMonthlyReturns2 = mock(ReturnsData.class);
    final var monthlyReturns2 = new TreeMap<>(Map.of(LOCAL_DATE_NOW.plusMonths(1), BigDecimal.TEN));
    when(rMonthlyReturns2.getReturns()).thenReturn(monthlyReturns2);

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(sut).retrieveReturns(anyMap());

    final var actual = sut.retrieveReturns(originalMReturns);

    final var expected = Map.of(holding1, monthlyReturn1, holding2, monthlyReturns2);
    assertEquals(expected, actual);
  }

  @Test
  void shouldMonthlyReturns_whenCheckResult() {
    final var rMonthlyReturns = mock(ReturnsData.class);
    final var monthlyReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    when(rMonthlyReturns.getCurrency()).thenReturn(CurrencyType.CAD.name());
    when(rMonthlyReturns.getReturns()).thenReturn(monthlyReturns);

    final var holding = mock(Holding.class);

    final var rMonthlyReturnsMap = Map.of(holding, rMonthlyReturns);

    final var sut = new Returns(rMonthlyReturnsMap);

    assertEquals(Map.of(holding, CurrencyType.CAD), sut.holdingCurrencyMap);
    assertEquals(Map.of(holding, monthlyReturns), sut.returnsMap);
  }

}
