package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.AnnualReturnCalculation;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;
import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AnnualReturnServiceImplTest {

  @Test
  void shouldPerform_whenVerifyBuildAnnualReturnCalculation() {
    final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService));

    final var returnReqDTO = mock(ReturnCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));
    final var calculationDTO = mock(CalculationDTO.class);

    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());
    when(sut.buildAnnualReturnCalculation(any())).thenReturn(mock(AnnualReturnCalculation.class));

    doCallRealMethod().when(sut).perform(returnReqDTO);
    sut.perform(returnReqDTO);

    verify(sut).buildAnnualReturnCalculation(calculationDTO);
  }

  @Test
  void shouldCalculate_whenVerifyBuildWeightedAverageInputDto() {
    final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService));

    final var returnReqDTO = mock(ReturnCommand.class);
    final var holdings = List.of(mock(PortfolioHolding.class));
    when(returnReqDTO.getHoldings()).thenReturn(holdings);
    when(returnReqDTO.getCurrency()).thenReturn(Currency.CAD);
    when(returnReqDTO.getCustomPed()).thenReturn(LOCAL_DATE_NOW);

    final var calculationDTO = mock(CalculationDTO.class);
    when(sut.buildWeightedAverageInputDto(any())).thenReturn(calculationDTO);
    when(calculationDTO.getWeightedAveragePortfolioReturns()).thenReturn(new TreeMap<>());

    final var annual = mock(AnnualReturnCalculation.class);
    final var resDTO = mock(AnnualReturnResult.class);
    when(annual.calculate()).thenReturn(resDTO);
    when(sut.buildAnnualReturnCalculation(any())).thenReturn(annual);

    doCallRealMethod().when(sut).perform(returnReqDTO);
    final AnnualReturnResult actual = sut.perform(returnReqDTO);

    assertSame(actual, resDTO);

  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyBuildCalculationDto() {
    final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService));
    final var holdings = List.of(mock(PortfolioHolding.class));
    final var annual = mock(ReturnCommand.class);
    final var monthlyReturns = mock(ReturnsAggregate.class);
    when(annual.getCurrency()).thenReturn(Currency.CAD);
    when(annual.getHoldings()).thenReturn(holdings);
    when(annual.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(annual.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
    sut.buildWeightedAverageInputDto(annual);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenCheckResult() {
    final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(PortfolioHolding.class));
    final NavigableMap map = new TreeMap<>();
    final var monthlyReturns = mock(ReturnsAggregate.class);

    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(map);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);

    final var annual = mock(ReturnCommand.class);
    when(annual.getCurrency()).thenReturn(Currency.CAD);
    when(annual.getHoldings()).thenReturn(holdings);
    when(annual.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(annual.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
    final CalculationDTO actual = sut.buildWeightedAverageInputDto(annual);

    final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(map).setWarnings(List.of());
    assertEquals(expected, actual);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(PortfolioHolding.class));
    final var monthlyReturns = mock(ReturnsAggregate.class);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(Currency.class), eq(SCALE_OF_TWO)))
        .thenReturn(
            monthlyReturns);

    final var annual = mock(ReturnCommand.class);
    when(annual.getCurrency()).thenReturn(Currency.CAD);
    when(annual.getHoldings()).thenReturn(holdings);
    when(annual.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(annual.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
    sut.buildWeightedAverageInputDto(annual);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, LOCAL_DATE_NOW
        .minusMonths(2), LOCAL_DATE_NOW);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetPortfolioMonthlyReturns() {
    final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(PortfolioHolding.class));
    final var monthlyReturns = mock(ReturnsAggregate.class);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);

    final var annual = mock(ReturnCommand.class);
    when(annual.getCurrency()).thenReturn(Currency.CAD);
    when(annual.getHoldings()).thenReturn(holdings);
    when(annual.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(annual.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
    sut.buildWeightedAverageInputDto(annual);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildAnnualReturnCalculation_whenVerify() {
    try (var mockedAnnualReturnCalculation = mockConstruction(AnnualReturnCalculation.class)) {
      final var sut = mock(AnnualReturnServiceImpl.class);
      final var input = mock(CalculationDTO.class);
      final var map = mock(NavigableMap.class);

      when(input.getWeightedAveragePortfolioReturns()).thenReturn(map);
      doCallRealMethod().when(sut).buildAnnualReturnCalculation(any());

      final AnnualReturnCalculation actual = sut.buildAnnualReturnCalculation(input);

      final List<AnnualReturnCalculation> constructed = mockedAnnualReturnCalculation.constructed();

      verify(input).getWeightedAveragePortfolioReturns();
      assertEquals(1, constructed.size());
      assertTrue(constructed.contains(actual));
    }
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenCheckMonthlyReturnsWarnings() {
    final MonthlyReturnsService monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(AnnualReturnServiceImpl.class, withSettings().useConstructor(monthlyReturnsService));

    final var holdings = List.of(mock(PortfolioHolding.class));
    final NavigableMap map = new TreeMap<>();
    final var monthlyReturns = mock(ReturnsAggregate.class);
    final var warnings = List.of(mock(Warning.class));

    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any())).thenReturn(map);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(any(), any(), any())).thenReturn(monthlyReturns);
    when(monthlyReturns.getErrorsAsWarnings()).thenReturn(warnings);

    final var annual = mock(ReturnCommand.class);
    when(annual.getCurrency()).thenReturn(Currency.CAD);
    when(annual.getHoldings()).thenReturn(holdings);
    when(annual.getCustomPed()).thenReturn(LOCAL_DATE_NOW);
    when(annual.getCustomPsd()).thenReturn(LOCAL_DATE_NOW.minusMonths(2));

    doCallRealMethod().when(sut).buildWeightedAverageInputDto(any());
    final CalculationDTO actual = sut.buildWeightedAverageInputDto(annual);

    final var expected = new CalculationDTO().setWeightedAveragePortfolioReturns(map).setWarnings(warnings);
    assertEquals(expected, actual);
  }

}