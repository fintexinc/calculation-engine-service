package com.fintex.ce.application.returns;

import com.fintex.ce.application.validation.CpedDataValidation;
import com.fintex.ce.application.validation.CpsdDataValidation;
import com.fintex.ce.domain.dto.command.DailyPerformanceCommand;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.exception.FdsDataValidationException;
import com.fintex.ce.domain.model.HistoricalNavPrices;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.domain.model.ValidationError;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.holding.Holding;

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
    final Holding holding = mock(Holding.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);

    final Returns sut = new Returns();
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));

    // ACT
    final Returns actual = sut.initForNavPrices(Map.of(holding, historicalNavPrices));

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldInitOnly_whenWithReturnsDataValidation() {
    // SETUP
    final Holding holding = mock(Holding.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);

    final Returns sut = new Returns();
    Mockito.when(historicalNavPrices.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));

    // ACT
    final Returns actual = sut.initOnlyWithReturnsDataValidation(Map.of(holding, historicalNavPrices));

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldInitOnlyWithReturnsDataValidation_whenMonthlyReturnsError() {
    // SETUP
    final Holding holding = mock(Holding.class);
    final Holding holdingMissingReturns = mock(Holding.class);
    final HoldingMonthlyReturns monthlyReturns = mock(HoldingMonthlyReturns.class);
    final HoldingMonthlyReturns monthlyReturnsMissing = mock(HoldingMonthlyReturns.class);
    monthlyReturnsMissing.setErrors(List.of(new ValidationError("id", ExceptionCode.ERR_RRC_MR_002.name(), "message")));

    final Returns sut = new Returns();
    Mockito.when(monthlyReturns.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));
    Mockito.when(monthlyReturnsMissing.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));

    // ACT
    final Returns actual = sut.initOnlyWithReturnsDataValidation(Map.of(holding, monthlyReturns, holdingMissingReturns,
        monthlyReturnsMissing));

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldInitOnlyWithReturnsDataValidation_whenNonAllowedMonthlyReturnsError() {
    // SETUP
    final Holding holding = mock(Holding.class);
    final Holding holdingMissingReturns = mock(Holding.class);
    final HoldingMonthlyReturns monthlyReturns = mock(HoldingMonthlyReturns.class);
    final HoldingMonthlyReturns monthlyReturnsMissing = mock(HoldingMonthlyReturns.class);

    final Returns sut = new Returns();
    Mockito.when(monthlyReturns.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));
    Mockito.when(monthlyReturnsMissing.getReturns()).thenReturn(new TreeMap(Map.of(LocalDate.now(), BigDecimal.ONE)));
    Mockito.when(monthlyReturns.hasMonthlyReturnsErrors()).thenReturn(false);
    Mockito.when(monthlyReturnsMissing.hasMonthlyReturnsErrors()).thenReturn(true);
    Mockito.when(monthlyReturnsMissing.getOnlyMonthlyReturnsErrors()).thenReturn(
        List.of(new ValidationError("id", ExceptionCode.ERR_RRC_CPED_003.name(), "message")));

    // VERIFY
    assertThrows(FdsDataValidationException.class, () -> sut.initOnlyWithReturnsDataValidation(Map.of(holding,
        monthlyReturns, holdingMissingReturns, monthlyReturnsMissing)));

  }

  @Test
  void shouldValidateAnd_whenUpdateCpsdAndCped() {
    // SETUP
    final Holding holding = mock(Holding.class);
    final DailyPerformanceCommand dailyPerformanceReqDTO = mock(DailyPerformanceCommand.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);
    final CpsdDataValidation cpsdDataValidation = mock(CpsdDataValidation.class);
    final CpedDataValidation cpedDataValidation = mock(CpedDataValidation.class);

    final Returns sut = new Returns();
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
    final Returns actual = sut.validateAndUpdateCpsdAndCped(Map.of(holding, historicalNavPrices),
        dailyPerformanceReqDTO);

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldValidateMonthly_whenDataMissing() {
    // SETUP
    final Holding holding = mock(Holding.class);
    final DailyPerformanceCommand dailyPerformanceReqDTO = mock(DailyPerformanceCommand.class);
    final HistoricalNavPrices historicalNavPrices = mock(HistoricalNavPrices.class);
    final CpsdDataValidation cpsdDataValidation = mock(CpsdDataValidation.class);
    final CpedDataValidation cpedDataValidation = mock(CpedDataValidation.class);

    final Returns sut = new Returns();
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
    final Returns actual = sut.validateMonthlyDataMissing(Map.of(holding, historicalNavPrices), dailyPerformanceReqDTO);

    // VERIFY
    assertNotNull(actual);
  }

  @Test
  void shouldGetErrors_whenReturnsEmptyListWhenNoErrors() {
    Returns<HoldingMonthlyReturns> sut = new Returns<>();
    assertTrue(sut.getErrors().isEmpty());
  }

  @Test
  void shouldGetErrors_whenReturnsListOfErrorsWhenErrorsExist() {
    Returns<HoldingMonthlyReturns> sut = new Returns<>();
    DataErrorException error = new DataErrorException("message", "id", ExceptionCode.ERR_RRC_MR_002);
    sut.notification.addError(error);
    List<DataErrorException> errors = sut.getErrors();
    assertEquals(1, errors.size());
    assertEquals(error, errors.get(0));
  }

  @Test
  void shouldGetErrors_whenReturnsMultipleErrorsWhenMultipleErrorsExist() {
    Returns<HoldingMonthlyReturns> sut = new Returns<>();
    DataErrorException error1 = new DataErrorException("message", "id", ExceptionCode.ERR_RRC_MR_002);
    DataErrorException error2 = new DataErrorException("message", "id2", ExceptionCode.ERR_RRC_MR_002);
    sut.notification.addError(error1);
    sut.notification.addError(error2);
    List<DataErrorException> errors = sut.getErrors();
    assertEquals(2, errors.size());
    assertTrue(errors.contains(error1));
    assertTrue(errors.contains(error2));
  }

  @Test
  void shouldValidateReturns_whenRemovesEntriesWithInvalidDates() {
    Returns<HoldingMonthlyReturns> sut = new Returns<>();
    Holding holding1 = mock(Holding.class);
    Holding holding2 = mock(Holding.class);
    TreeMap<LocalDate, BigDecimal> returns1 = new TreeMap<>(Map.of(
        LocalDate.of(2023, 1, 1), BigDecimal.ONE,
        LocalDate.of(2023, 2, 1), BigDecimal.ONE));
    TreeMap<LocalDate, BigDecimal> returns2 = new TreeMap<>(Map.of(
        LocalDate.of(2023, 3, 1), BigDecimal.ONE,
        LocalDate.of(2023, 4, 1), BigDecimal.ONE));

    Map<Holding, TreeMap<LocalDate, BigDecimal>> returnsMap = new HashMap<>();

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
    Returns<HoldingMonthlyReturns> sut = new Returns<>();
    List<Warning> warnings = sut.getErrorsAsWarnings();
    assertTrue(warnings.isEmpty());
  }

  @Test
  void shouldGetErrorsAsWarnings_whenReturnsListOfWarningsWhenErrorsExist() {
    Returns<HoldingMonthlyReturns> sut = new Returns<>();
    DataErrorException error = new DataErrorException("message", "id", ExceptionCode.ERR_RRC_MR_002);
    sut.notification.addError(error);
    List<Warning> warnings = sut.getErrorsAsWarnings();
    assertEquals(1, warnings.size());
    assertEquals(error.getId(), warnings.get(0).getId());
    assertEquals(error.getMessage(), warnings.get(0).getMessage());
    assertEquals(error.getCode().name(), warnings.get(0).getCode());
  }

}
