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

    final ReturnsAggregate returnsAggregate = new ReturnsAggregate();
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));

    // ACT
    final ReturnsAggregate actual = returnsAggregate.initForNavPrices(Map.of(holding, historicalNavPrices));

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldInitOnly_whenWithReturnsDataValidation() {
    // SETUP
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);

    final ReturnsAggregate returnsAggregate = new ReturnsAggregate();
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));

    // ACT
    final ReturnsAggregate actual = returnsAggregate.initOnlyWithReturnsDataValidation(Map.of(holding,
        historicalNavPrices));

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

    final ReturnsAggregate returnsAggregate = new ReturnsAggregate();
    Mockito.when(monthlyReturns.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));
    Mockito.when(monthlyReturnsMissing.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));

    // ACT
    final ReturnsAggregate actual = returnsAggregate.initOnlyWithReturnsDataValidation(Map.of(holding, monthlyReturns,
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

    final ReturnsAggregate returnsAggregate = new ReturnsAggregate();
    Mockito.when(monthlyReturns.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));
    Mockito.when(monthlyReturnsMissing.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));
    Mockito.when(monthlyReturns.hasMonthlyReturnsErrors()).thenReturn(false);
    Mockito.when(monthlyReturnsMissing.hasMonthlyReturnsErrors()).thenReturn(true);
    Mockito.when(monthlyReturnsMissing.getOnlyMonthlyReturnsErrors()).thenReturn(
        List.of(ErrorCode.CPED_AFTER_PORTFOLIO_PED.toNotification("id", null, null, null)));

    // VERIFY
    assertThrows(CalculationsFailedException.class, () -> returnsAggregate.initOnlyWithReturnsDataValidation(Map.of(
        holding,
        monthlyReturns, holdingMissingReturns, monthlyReturnsMissing)));

  }

  @Test
  void shouldValidateAnd_whenUpdateCpsdAndCped() {
    // SETUP
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final DailyPerformanceCommand command = mock(DailyPerformanceCommand.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);
    final CpsdDataValidation cpsdDataValidation = mock(CpsdDataValidation.class);
    final CpedDataValidation cpedDataValidation = mock(CpedDataValidation.class);

    final ReturnsAggregate returnsAggregate = new ReturnsAggregate();
    final TreeMap returns = new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE));

    returnsAggregate.setCpsdDataValidation(cpsdDataValidation);
    returnsAggregate.setCpedDataValidation(cpedDataValidation);
    returnsAggregate.performanceStartDate = LocalDate.now().minusMonths(7);
    returnsAggregate.performanceEndDate = LocalDate.now().plusMonths(7);
    returnsAggregate.returnsMap = Map.of(holding, returns);
    Mockito.when(command.getStartDate()).thenReturn(LocalDate.now().minusMonths(7));
    Mockito.when(command.getEndDate()).thenReturn(LocalDate.now().plusMonths(7));
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(returns);

    // ACT
    final ReturnsAggregate actual = returnsAggregate.validateAndUpdateCpsdAndCped(Map.of(holding, historicalNavPrices),
        command);

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldValidateMonthly_whenDataMissing() {
    // SETUP
    final PortfolioHolding holding = mock(PortfolioHolding.class);
    final DailyPerformanceCommand command = mock(DailyPerformanceCommand.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);
    final CpsdDataValidation cpsdDataValidation = mock(CpsdDataValidation.class);
    final CpedDataValidation cpedDataValidation = mock(CpedDataValidation.class);

    final ReturnsAggregate returnsAggregate = new ReturnsAggregate();
    final TreeMap returns = new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE));

    returnsAggregate.setCpsdDataValidation(cpsdDataValidation);
    returnsAggregate.setCpedDataValidation(cpedDataValidation);
    returnsAggregate.performanceEndDate = LocalDate.now().minusMonths(7);
    returnsAggregate.performanceStartDate = LocalDate.now().plusMonths(7);
    returnsAggregate.returnsMap = Map.of(holding, returns);
    Mockito.when(command.getStartDate()).thenReturn(LocalDate.now().minusMonths(7));
    Mockito.when(command.getEndDate()).thenReturn(LocalDate.now().plusMonths(7));
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(returns);

    // ACT
    final ReturnsAggregate actual = returnsAggregate.validateMonthlyDataMissing(Map.of(holding, historicalNavPrices),
        command);

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldGetErrors_whenReturnsEmptyListWhenNoErrors() {
    ReturnsAggregate<HoldingMonthlyReturns> returnsAggregate = new ReturnsAggregate<>();
    assertTrue(returnsAggregate.getErrors().isEmpty());
  }

  @Test
  void shouldGetErrors_whenReturnsListOfErrorsWhenErrorsExist() {
    ReturnsAggregate<HoldingMonthlyReturns> returnsAggregate = new ReturnsAggregate<>();
    CalculationException error = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("id");
    returnsAggregate.notification.add(error);
    List<BasePceException> errors = returnsAggregate.getErrors();
    assertEquals(1, errors.size());
    assertEquals(error, errors.get(0));
  }

  @Test
  void shouldGetErrors_whenReturnsMultipleErrorsWhenMultipleErrorsExist() {
    ReturnsAggregate<HoldingMonthlyReturns> returnsAggregate = new ReturnsAggregate<>();
    CalculationException error1 = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("id");
    CalculationException error2 = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("id2");
    returnsAggregate.notification.add(error1);
    returnsAggregate.notification.add(error2);
    List<BasePceException> errors = returnsAggregate.getErrors();
    assertEquals(2, errors.size());
    assertTrue(errors.contains(error1));
    assertTrue(errors.contains(error2));
  }

  @Test
  void shouldValidateReturns_whenRemovesEntriesWithInvalidDates() {
    ReturnsAggregate<HoldingMonthlyReturns> returnsAggregate = new ReturnsAggregate<>();
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
    returnsAggregate.returnsMap = returnsMap;
    returnsAggregate.findPedAndPsd();

    returnsAggregate.validateReturns();
    System.out.println(returnsAggregate.returnsMap);
    assertFalse(returnsAggregate.returnsMap.containsKey(holding2));
  }

  @Test
  void shouldGetErrorsAsWarnings_whenReturnsEmptyListWhenNoErrors() {
    ReturnsAggregate<HoldingMonthlyReturns> returnsAggregate = new ReturnsAggregate<>();
    List<Warning> warnings = returnsAggregate.getErrorsAsWarnings();
    assertTrue(warnings.isEmpty());
  }

  @Test
  void shouldGetErrorsAsWarnings_whenReturnsListOfWarningsWhenErrorsExist() {
    ReturnsAggregate<HoldingMonthlyReturns> returnsAggregate = new ReturnsAggregate<>();
    CalculationException error = ErrorCode.HOLDING_PSD_OUT_OF_RANGE.toExceptionForId("id");
    returnsAggregate.notification.add(error);
    List<Warning> warnings = returnsAggregate.getErrorsAsWarnings();
    assertEquals(1, warnings.size());
    assertEquals(error.getId(), warnings.get(0).getId());
    assertEquals(error.getMessage(), warnings.get(0).getMessage());
    assertEquals(error.getErrorCode().getCode(), warnings.get(0).getCode());
  }

}
