package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.CpedDataValidation;
import com.fintex.ce.application.validation.CpsdDataValidation;
import com.fintex.ce.model.domain.calculation.returns.HistoricalNavPrices;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.DailyPerformanceCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ReturnsTest {

  @Test
  void shouldInitFor_whenNavPrices() {
    // SETUP
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);

    final ReturnsAggregate sut = new ReturnsAggregate();
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));

    // ACT
    final ReturnsAggregate actual = sut.initForNavPrices(Map.of(holding, historicalNavPrices));

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldInitOnly_whenWithReturnsDataValidation() {
    // SETUP
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);

    final ReturnsAggregate sut = new ReturnsAggregate();
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));

    // ACT
    final ReturnsAggregate actual = sut.initOnlyWithReturnsDataValidation(Map.of(holding, historicalNavPrices));

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldInitOnlyWithReturnsDataValidation_whenMonthlyReturnsError() {
    // SETUP
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final PortfolioHolding holdingMissingReturns = mock(PortfolioHolding.class);
    final HoldingMonthlyReturns monthlyReturns = mock(HoldingMonthlyReturns.class);
    final HoldingMonthlyReturns monthlyReturnsMissing = mock(HoldingMonthlyReturns.class);
    monthlyReturnsMissing.setErrors(List.of(ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toNotification("id", null, null, null)));

    final ReturnsAggregate sut = new ReturnsAggregate();
    Mockito.when(monthlyReturns.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));
    Mockito.when(monthlyReturnsMissing.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));

    // ACT
    final ReturnsAggregate actual = sut.initOnlyWithReturnsDataValidation(Map.of(holding, monthlyReturns,
        holdingMissingReturns,
        monthlyReturnsMissing));

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldInitOnlyWithReturnsDataValidation_whenNonAllowedMonthlyReturnsError() {
    // SETUP
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final PortfolioHolding holdingMissingReturns = mock(PortfolioHolding.class);
    final HoldingMonthlyReturns monthlyReturns = mock(HoldingMonthlyReturns.class);
    final HoldingMonthlyReturns monthlyReturnsMissing = mock(HoldingMonthlyReturns.class);

    final ReturnsAggregate sut = new ReturnsAggregate();
    Mockito.when(monthlyReturns.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));
    Mockito.when(monthlyReturnsMissing.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));
    Mockito.when(monthlyReturns.hasMonthlyReturnsErrors()).thenReturn(false);
    Mockito.when(monthlyReturnsMissing.hasMonthlyReturnsErrors()).thenReturn(true);
    Mockito.when(monthlyReturnsMissing.getOnlyMonthlyReturnsErrors()).thenReturn(
        List.of(ErrorCode.CPED_AFTER_PORTFOLIO_PED.toNotification("id", null, null, null)));

    // VERIFY
    assertThrows(CalculationsFailedException.class, () -> sut.initOnlyWithReturnsDataValidation(Map.of(holding,
        monthlyReturns, holdingMissingReturns, monthlyReturnsMissing)));

  }

  @Test
  void shouldValidateAnd_whenUpdateCpsdAndCped() {
    // SETUP
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final DailyPerformanceCommand dailyPerformanceReqDTO = mock(DailyPerformanceCommand.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);
    final CpsdDataValidation cpsdDataValidation = mock(CpsdDataValidation.class);
    final CpedDataValidation cpedDataValidation = mock(CpedDataValidation.class);

    final ReturnsAggregate sut = new ReturnsAggregate();
    final TreeMap returns = new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE));

    sut.setCpsdDataValidation(cpsdDataValidation);
    sut.setCpedDataValidation(cpedDataValidation);
    sut.psd = LocalDate.now().minusMonths(7);
    sut.ped = LocalDate.now().plusMonths(7);
    sut.returnsMap = Map.of(holding, returns);
    Mockito.when(dailyPerformanceReqDTO.getStartDate()).thenReturn(LocalDate.now().minusMonths(7));
    Mockito.when(dailyPerformanceReqDTO.getEndDate()).thenReturn(LocalDate.now().plusMonths(7));
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(returns);

    // ACT
    final ReturnsAggregate actual = sut.validateAndUpdateCpsdAndCped(Map.of(holding, historicalNavPrices),
        dailyPerformanceReqDTO);

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldValidateMonthly_whenDataMissing() {
    // SETUP
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final DailyPerformanceCommand dailyPerformanceReqDTO = mock(DailyPerformanceCommand.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);
    final CpsdDataValidation cpsdDataValidation = mock(CpsdDataValidation.class);
    final CpedDataValidation cpedDataValidation = mock(CpedDataValidation.class);

    final ReturnsAggregate sut = new ReturnsAggregate();
    final TreeMap returns = new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE));

    sut.setCpsdDataValidation(cpsdDataValidation);
    sut.setCpedDataValidation(cpedDataValidation);
    sut.ped = LocalDate.now().minusMonths(7);
    sut.psd = LocalDate.now().plusMonths(7);
    sut.returnsMap = Map.of(holding, returns);
    Mockito.when(dailyPerformanceReqDTO.getStartDate()).thenReturn(LocalDate.now().minusMonths(7));
    Mockito.when(dailyPerformanceReqDTO.getEndDate()).thenReturn(LocalDate.now().plusMonths(7));
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(returns);

    // ACT
    final ReturnsAggregate actual = sut.validateMonthlyDataMissing(Map.of(holding, historicalNavPrices),
        dailyPerformanceReqDTO);

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldGetErrors_whenReturnsEmptyListWhenNoErrors() {
    ReturnsAggregate<HoldingMonthlyReturns> sut = new ReturnsAggregate<>();
    assertTrue(sut.getErrors().isEmpty());
  }

  @Test
  void shouldGetErrors_whenReturnsListOfErrorsWhenErrorsExist() {
    ReturnsAggregate<HoldingMonthlyReturns> sut = new ReturnsAggregate<>();
    CalculationException error = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("id");
    sut.notification.add(error);
    List<BasePceException> errors = sut.getErrors();
    assertEquals(1, errors.size());
    assertEquals(error, errors.get(0));
  }

  @Test
  void shouldGetErrors_whenReturnsMultipleErrorsWhenMultipleErrorsExist() {
    ReturnsAggregate<HoldingMonthlyReturns> sut = new ReturnsAggregate<>();
    CalculationException error1 = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("id");
    CalculationException error2 = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("id2");
    sut.notification.add(error1);
    sut.notification.add(error2);
    List<BasePceException> errors = sut.getErrors();
    assertEquals(2, errors.size());
    assertTrue(errors.contains(error1));
    assertTrue(errors.contains(error2));
  }

  @Test
  void shouldValidateReturns_whenRemovesEntriesWithInvalidDates() {
    ReturnsAggregate<HoldingMonthlyReturns> sut = new ReturnsAggregate<>();
    PortfolioHolding holding1 = mock(PortfolioHolding.class);
    PortfolioHolding holding2 = mock(PortfolioHolding.class);
    TreeMap<LocalDate, BigDecimal> returns1 = new TreeMap<>(Map.of(
        LocalDate.of(2023, 1, 1), BigDecimal.ONE,
        LocalDate.of(2023, 2, 1), BigDecimal.ONE));
    TreeMap<LocalDate, BigDecimal> returns2 = new TreeMap<>(Map.of(
        LocalDate.of(2023, 3, 1), BigDecimal.ONE,
        LocalDate.of(2023, 4, 1), BigDecimal.ONE));

    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap = new HashMap<>();

    returnsMap.put(holding1, returns1);
    returnsMap.put(holding2, returns2);
    sut.returnsMap = returnsMap;
    sut.findPedAndPsd();

    sut.validateReturns();
    System.out.println(sut.returnsMap);
    assertFalse(sut.returnsMap.containsKey(holding2));
  }

  @Test
  void shouldGetErrorsAsWarnings_whenReturnsEmptyListWhenNoErrors() {
    ReturnsAggregate<HoldingMonthlyReturns> sut = new ReturnsAggregate<>();
    List<Warning> warnings = sut.getErrorsAsWarnings();
    assertTrue(warnings.isEmpty());
  }

  @Test
  void shouldGetErrorsAsWarnings_whenReturnsListOfWarningsWhenErrorsExist() {
    ReturnsAggregate<HoldingMonthlyReturns> sut = new ReturnsAggregate<>();
    CalculationException error = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("id");
    sut.notification.add(error);
    List<Warning> warnings = sut.getErrorsAsWarnings();
    assertEquals(1, warnings.size());
    assertEquals(error.getId(), warnings.get(0).getId());
    assertEquals(error.getMessage(), warnings.get(0).getMessage());
    assertEquals(error.getErrorCode().getCode(), warnings.get(0).getCode());
  }

}
