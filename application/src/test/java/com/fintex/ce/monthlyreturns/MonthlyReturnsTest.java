package com.fintex.ce.monthlyreturns;

import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.domain.model.ReturnsData;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.monthlyreturns.FxRatesConversionComponent;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.monthlyreturns.ReturnsCutComponent;
import com.fintex.ce.monthlyreturns.WeightedAverageComponent;
import com.fintex.ce.util.MapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.domain.constant.BigDecimalConstants.TWO;
import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_MR_002;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.TestConstants.LOCAL_DATE_NOW;
import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonthlyReturnsTest {

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVeryfyNoActionWhenThisPedIsAfterOtherPed() {
    // SETUP
    final var sut = mock(Returns.class);
    final var other = mock(Returns.class);

    sut.ped = LOCAL_DATE_NOW;
    other.ped = LOCAL_DATE_NOW.minusMonths(1);

    doCallRealMethod().when(other).getPed();
    doCallRealMethod().when(sut).cutArgumentToTheSameEndDate(any());

    // ACT
    sut.cutArgumentToTheSameEndDate(other);

    // VERIFY
    verify(sut).cutArgumentToTheSameEndDate(other);
    verify(other).getPed();
    verifyNoMoreInteractions(sut, other);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenCheckResultWhenThisPedIsAfterOtherPed() {
    // SETUP
    final var sut = mock(Returns.class);
    final var other = mock(Returns.class);

    sut.ped = LOCAL_DATE_NOW;
    other.ped = LOCAL_DATE_NOW.minusMonths(1);

    doCallRealMethod().when(other).getPed();
    doCallRealMethod().when(sut).cutArgumentToTheSameEndDate(any());

    // ACT
    final var actual = sut.cutArgumentToTheSameEndDate(other);

    // VERIFY
    assertSame(other, actual);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVerifyCutReturnsByEndDateWhenThisPedIsBeforeOtherPed() {
    // SETUP
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

    // ACT
    sut.cutArgumentToTheSameEndDate(other);

    // VERIFY
    verify(monthlyReturnsCutComponent).cutReturnsByEndDate(otherMonthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenVerifyInitWhenThisPedIsBeforeOtherPed() {
    // SETUP
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

    // ACT
    sut.cutArgumentToTheSameEndDate(other);

    // VERIFY
    verify(other).findPedAndPsd();
  }

  @Test
  void shouldCutArgumentToTheSameEndDateWhenPedIsGreater_whenCheckResult2WhenThisPedIsBeforeOtherPed() {
    // SETUP
    final var sut = mock(Returns.class);
    final var other = mock(Returns.class);

    final var otherMonthlyReturns = mock(Map.class);
    other.returnsMap = otherMonthlyReturns;

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

    // ACT
    final var actual = sut.cutArgumentToTheSameEndDate(other);

    // VERIFY
    assertSame(initedOther, actual);
  }

  @Test
  void shouldFxRatesApplied_whenVerifyConvert() {
    // SETUP
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

    // ACT
    sut.fxRatesApplied();

    // VERIFY
    Assertions.assertNotNull(monthlyReturns);
    fxRatesConversionComponent.convert(monthlyReturns, holdingCurrency);
  }

  @Test
  void shouldCutByCpedIfCpedEmptyCutByPed_whenVerifyCutReturnsByEndDateWhenCpedIsNotNull() {
    // SETUP
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.ped = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByCpedIfCpedEmptyCutByPed(any());

    // ACT
    sut.cutByCpedIfCpedEmptyCutByPed(LOCAL_DATE_NOW);

    // VERIFY
    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpedIfCpedEmptyCutByPed_whenVerifyCutReturnsByEndDateWhenCpedIsNull() {
    // SETUP
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.ped = LOCAL_DATE_NOW;

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByCpedIfCpedEmptyCutByPed(any());

    // ACT
    sut.cutByCpedIfCpedEmptyCutByPed(null);

    // VERIFY
    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByPed_whenVerifyCutReturnsByEndDate() {
    // SETUP
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.ped = LOCAL_DATE_NOW;

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByPed();

    // ACT
    sut.cutByPed();

    // VERIFY
    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByEndDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByPsd_whenVerifyCutReturnsByEndDate() {
    // SETUP
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.psd = LOCAL_DATE_NOW;

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByPsd();

    // ACT
    sut.cutByPsd();

    // VERIFY
    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpsdIfCpsdEmptyCutByPsd_whenVerifyCutReturnsByEndDateWhenCpedIsNotNull() {
    // SETUP
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.psd = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByCpsdIfCpsdEmptyCutByPsd(any());

    // ACT
    sut.cutByCpsdIfCpsdEmptyCutByPsd(LOCAL_DATE_NOW);

    // VERIFY
    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW);
  }

  @Test
  void shouldCutByCpsdIfCpsdEmptyCutByPsd_whenVerifyCutReturnsByEndDateWhenCpedIsNull() {
    // SETUP
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.psd = LOCAL_DATE_NOW.plusMonths(3);

    doCallRealMethod().when(sut).setMonthlyReturnsCutComponent(any());
    final var monthlyReturnsCutComponent = mock(ReturnsCutComponent.class);
    sut.setMonthlyReturnsCutComponent(monthlyReturnsCutComponent);

    doCallRealMethod().when(sut).cutByCpsdIfCpsdEmptyCutByPsd(any());

    // ACT
    sut.cutByCpsdIfCpsdEmptyCutByPsd(LOCAL_DATE_NOW);

    // VERIFY
    Assertions.assertNotNull(monthlyReturns);
    monthlyReturnsCutComponent.cutReturnsByStartDate(monthlyReturns, LOCAL_DATE_NOW.plusMonths(3));
  }

  @Test
  void shouldGetWeightedAverage_whenVerifyGetWeightedAverage() {
    // SETUP
    final var sut = mock(Returns.class);

    final var monthlyReturns = mock(Map.class);
    sut.returnsMap = monthlyReturns;
    sut.notification = new Notification();

    doCallRealMethod().when(sut).setWeightedAverageComponent(any());
    final var weightedAverageComponent = mock(WeightedAverageComponent.class);
    sut.setWeightedAverageComponent(weightedAverageComponent);

    doCallRealMethod().when(sut).getWeightedAverage();

    // ACT
    sut.getWeightedAverage();

    // VERIFY
    verify(weightedAverageComponent).calculateWeightedAverage(monthlyReturns);
  }

  @Test
  void shouldGetWeightedAverage_whenCheckResult() {
    // SETUP
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

    // ACT
    sut.getWeightedAverage();

    // VERIFY
    verify(weightedAverageComponent).calculateWeightedAverage(monthlyReturns);
  }

  @Test
  void shouldValidateCped_whenVerifyValidatePortfolioCped() {
    // SETUP
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

    // ACT
    sut.validateCped(LOCAL_DATE_NOW);

    // VERIFY
    verify(cpedDataValidation)
        .validate(eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)), eq(LOCAL_DATE_NOW.plusMonths(2)), same(
            notification));
  }

  @Test
  void shouldValidateCpsd_whenVerifyValidatePortfolioCped() {
    // SETUP
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

    // ACT
    sut.validateCpsd(LOCAL_DATE_NOW);

    // VERIFY
    verify(portfolioCpsdDataValidation)
        .validate(eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)), eq(LOCAL_DATE_NOW.plusMonths(2)), same(
            notification));
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase1() {
    // SETUP
    final var sut = new Returns();
    var monthlyReturns = new HashMap<Holding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new EtfHolding(TWO, HoldingType.CANADA_ETF, "exchangeCode", "cEtf1");
    var h2 = new EtfHolding(TWO, HoldingType.CANADA_ETF, "exchangeCode", "cEtf2");
    var h3 = new EtfHolding(ONE, HoldingType.US_ETF, "exchangeCode", "usEtf1");
    var h4 = new EtfHolding(ONE, HoldingType.US_ETF, "exchangeCode", "usEtf2");
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

    // ACT
    var validatedReturns = sut.validateReturns();

    // VERIFY
    var expectedErrorList = List.of(
        ERR_RRC_MR_002.error(h2),
        ERR_RRC_MR_002.error(h1),
        ERR_RRC_MR_002.error(h3));
    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase2() {
    // SETUP
    final var sut = new Returns();
    var monthlyReturns = new HashMap<Holding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new EtfHolding(TWO, HoldingType.CANADA_ETF, "exchangeCode", "cEtf1");
    var h2 = new EtfHolding(TWO, HoldingType.CANADA_ETF, "exchangeCode", "cEtf2");
    var h3 = new EtfHolding(ONE, HoldingType.US_ETF, "exchangeCode", "usEtf1");
    var h4 = new EtfHolding(ONE, HoldingType.US_ETF, "exchangeCode", "usEtf2");
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

    // ACT
    var validatedReturns = sut.validateReturns();

    // VERIFY
    var expectedErrorList = List.of(
        ERR_RRC_MR_002.error(h1),
        ERR_RRC_MR_002.error(h2),
        ERR_RRC_MR_002.error(h3));

    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCheckExceptionCase3() {
    // SETUP
    final var sut = new Returns();
    var monthlyReturns = new HashMap<Holding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new EtfHolding(TWO, HoldingType.CANADA_ETF, "exchangeCode", "cEtf1");
    var h2 = new EtfHolding(TWO, HoldingType.CANADA_ETF, "exchangeCode", "cEtf2");
    var h3 = new EtfHolding(ONE, HoldingType.US_ETF, "exchangeCode", "usEtf1");
    var h4 = new EtfHolding(ONE, HoldingType.US_ETF, "exchangeCode", "usEtf2");
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

    // ACT
    var validatedReturns = sut.validateReturns();

    // VERIFY
    var expectedErrorList = List.of(
        ERR_RRC_MR_002.error(h3),
        ERR_RRC_MR_002.error(h4));
    assertTrue(validatedReturns.getErrors().containsAll(expectedErrorList));
    assertEquals(expectedErrorList.size(), validatedReturns.getErrors().size());
  }

  @Test
  void shouldValidateMonthlyReturns_whenCase4NoExceptionThrown() {
    // SETUP
    final var sut = new Returns();
    var monthlyReturns = new HashMap<Holding, TreeMap<LocalDate, BigDecimal>>();
    var h1 = new EtfHolding(TWO, HoldingType.CANADA_ETF, "exchangeCode", "cEtf1");
    var h2 = new EtfHolding(TWO, HoldingType.CANADA_ETF, "exchangeCode", "cEtf2");
    var h3 = new EtfHolding(ONE, HoldingType.US_ETF, "exchangeCode", "usEtf1");
    var h4 = new EtfHolding(ONE, HoldingType.US_ETF, "exchangeCode", "usEtf2");
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

    // ACT
    assertDoesNotThrow(sut::validateReturns);

    // VERIFY
  }

  @Test
  void shouldGetMonthlyReturns_whenCheckResult() {
    // SETUP
    final var sut = mock(Returns.class);
    final var monthlyReturns = Map.of(mock(Holding.class), new TreeMap<>(Map.of(LOCAL_DATE_NOW, BigDecimal.ONE)));
    sut.returnsMap = monthlyReturns;

    doCallRealMethod().when(sut).getReturnsMap();

    // ACT
    final var actual = sut.getReturnsMap();

    // VERIFY
    assertNotSame(monthlyReturns, actual);
  }

  @Test
  void shouldGetMonthlyReturns_whenVerifyCopy() {
    // SETUP
    try (var mapUtilsMock = mockStatic(MapUtils.class)) {
      final var sut = mock(Returns.class);
      final var monthlyReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, BigDecimal.ONE));
      final var holdingMonthlyReturns = Map.of(mock(Holding.class), monthlyReturns);
      sut.returnsMap = holdingMonthlyReturns;

      doCallRealMethod().when(sut).getReturnsMap();

      // ACT
      final var actual = sut.getReturnsMap();

      // VERIFY
      mapUtilsMock.verify(() -> MapUtils.copyTreeMap(eq(monthlyReturns), any()));
    }
  }

  @Test
  void shouldFindPsdAmongHoldings_whenCheckResult() {
    // SETUP
    final var holding = mock(Holding.class);
    final var sut = mock(Returns.class);

    sut.returnsMap = Map.of(holding,
        new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)),
            ONE)));

    doCallRealMethod().when(sut).findPsdAmongMonthlyReturns();
    // ACT
    final LocalDate psd = sut.findPsdAmongMonthlyReturns();

    // VERIFY
    assertEquals(toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)), psd);
  }

  @Test
  void shouldFindPedAmongHoldings_whenCheckResult() {
    // SETUP
    final var holding = mock(Holding.class);
    final var sut = mock(Returns.class);
    sut.returnsMap = Map.of(holding,
        new TreeMap<>(Map.of(toLastDayOfMonth(LOCAL_DATE_NOW), ONE, toLastDayOfMonth(LOCAL_DATE_NOW.minusMonths(1)),
            ONE)));

    doCallRealMethod().when(sut).findPedAmongMonthlyReturns();
    doCallRealMethod().when(sut).findPed(any());
    // ACT
    final LocalDate ped = sut.findPedAmongMonthlyReturns();

    // VERIFY
    assertEquals(toLastDayOfMonth(LOCAL_DATE_NOW), ped);
  }

  @Test
  void shouldRetrieveHoldingCurrencies_whenCheckResult() {
    // SETUP
    final var sut = mock(Returns.class);
    sut.notification = new Notification();

    final var holding1 = mock(Holding.class);
    final var holding2 = mock(Holding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    when(rMonthlyReturns1.getCurrency()).thenReturn(Currency.CAD.name());
    final var rMonthlyReturns2 = mock(ReturnsData.class);
    when(rMonthlyReturns2.getCurrency()).thenReturn(Currency.USD.name());

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(sut).retrieveHoldingCurrencies(anyMap());

    // ACT
    final var actual = sut.retrieveHoldingCurrencies(originalMReturns);

    // VERIFY
    final var expected = Map.of(holding1, Currency.CAD, holding2, Currency.USD);
    assertEquals(expected, actual);
    assertTrue(sut.notification.getErrors().isEmpty());
  }

  @Test
  void shouldRetrieveHoldingCurrencies_whenCurrencyIsNull() {
    // SETUP
    final var sut = mock(Returns.class);
    sut.notification = new Notification();

    final var holding1 = mock(Holding.class);
    final var holding2 = mock(Holding.class);

    final var rMonthlyReturns1 = mock(ReturnsData.class);
    when(rMonthlyReturns1.getCurrency()).thenReturn(null);
    final var rMonthlyReturns2 = mock(ReturnsData.class);
    when(rMonthlyReturns2.getCurrency()).thenReturn(Currency.USD.name());

    final var originalMReturns = Map.of(holding1, rMonthlyReturns1, holding2, rMonthlyReturns2);

    doCallRealMethod().when(sut).retrieveHoldingCurrencies(anyMap());

    // ACT
    final var actual = sut.retrieveHoldingCurrencies(originalMReturns);

    // VERIFY

    final var expected = new HashMap<Holding, Currency>();
    expected.put(holding2, Currency.USD);
    assertEquals(expected, actual);
    assertFalse(sut.notification.getErrors().isEmpty());
  }

  @Test
  void shouldRetrieveReturns_whenCheckResult() {
    // SETUP
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

    // ACT
    final var actual = sut.retrieveReturns(originalMReturns);

    // VERIFY
    final var expected = Map.of(holding1, monthlyReturn1, holding2, monthlyReturns2);
    assertEquals(expected, actual);
  }

  @Test
  void shouldMonthlyReturns_whenCheckResult() {
    // SETUP
    final var rMonthlyReturns = mock(ReturnsData.class);
    final var monthlyReturns = new TreeMap<>(Map.of(LOCAL_DATE_NOW, ONE));
    when(rMonthlyReturns.getCurrency()).thenReturn(Currency.CAD.name());
    when(rMonthlyReturns.getReturns()).thenReturn(monthlyReturns);

    final var holding = mock(Holding.class);

    final var rMonthlyReturnsMap = Map.of(holding, rMonthlyReturns);

    // ACT
    final var sut = new Returns(rMonthlyReturnsMap);

    // VERIFY
    assertEquals(Map.of(holding, Currency.CAD), sut.holdingCurrencyMap);
    assertEquals(Map.of(holding, monthlyReturns), sut.returnsMap);
  }

}
