package com.fintex.ce.application.service.calculation.period;

import com.fintex.ce.application.calculation.RollingTotalReturnsCalculation;
import com.fintex.ce.application.service.calculation.MonthlyReturnsService;
import com.fintex.ce.application.service.calculation.period.RollingTotalReturnsCalculationServiceImpl;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.port.input.command.RollingCalculationCommand;
import com.fintex.ce.port.input.result.RollingTotalReturnsResult;
import com.fintex.ce.monthlyreturns.Returns;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.ReturnFactorScale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.domain.enumeration.HoldingIdentifierType.FUNDSERV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class RollingTotalReturnsCalculationServiceImplTest {

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null));

    final var holding = new FundSeriesHolding();
    holding.setFundServCode("RBF605");
    holding.setHoldingIdentifier(FUNDSERV);
    holding.setType(HoldingType.CANADA_MUTUAL_FUNDS);
    holding.setValue(BigDecimal.valueOf(50000));

    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setRollingPeriods(Set.of("12", "11", "10"));
    req.setCurrency(Currency.CAD);

    final var rollingCalculation = mock(RollingTotalReturnsCalculation.class);
    final var expected = mock(RollingTotalReturnsResult.class);

    when(sut.defineCalculationMethod(any())).thenReturn(rollingCalculation);
    when(rollingCalculation.calculate(any())).thenReturn(expected);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    final var actual = sut.perform(req);

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    // SETUP
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null));

    final var holding = new FundSeriesHolding();
    holding.setFundServCode("RBF605");
    holding.setHoldingIdentifier(FUNDSERV);
    holding.setType(HoldingType.CANADA_MUTUAL_FUNDS);
    holding.setValue(BigDecimal.valueOf(50000));

    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setRollingPeriods(Set.of("12", "11", "10"));
    req.setCurrency(Currency.CAD);

    final var rollingCalculation = mock(RollingTotalReturnsCalculation.class);

    when(sut.defineCalculationMethod(any())).thenReturn(rollingCalculation);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(req);

    // VERIFY
    verify(rollingCalculation).calculate(req.getRollingPeriods());
  }

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    // SETUP
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null));

    final var holding = new FundSeriesHolding();
    holding.setFundServCode("RBF605");
    holding.setHoldingIdentifier(FUNDSERV);
    holding.setType(HoldingType.CANADA_MUTUAL_FUNDS);
    holding.setValue(BigDecimal.valueOf(50000));

    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setCurrency(Currency.CAD);

    final var rollingCalculation = mock(RollingTotalReturnsCalculation.class);

    when(sut.defineCalculationMethod(req)).thenReturn(rollingCalculation);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(req);

    // VERIFY
    verify(sut).defineCalculationMethod(req);
  }

  @Test
  void shouldDefineCalculationMethod_whenCheckResult() {
    // SETUP
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(null, Set.of("10", "20")));

    final var req = new RollingCalculationCommand();
    req.setRollingPeriods(Set.of("100"));
    final var inputDTO = mock(CalculationDTO.class);
    final var portfolioTotalReturns = mock(TreeMap.class);
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;

    when(sut.buildCalculationDto(req, returnFactorScale)).thenReturn(inputDTO);
    when(inputDTO.getWeightedAveragePortfolioReturns()).thenReturn(portfolioTotalReturns);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    final var actual = sut.defineCalculationMethod(req);

    // VERIFY
    assertEquals(inputDTO.getWeightedAveragePortfolioReturns(), actual.getPortfolioTotalReturns());
    ComparisonUtils.compareCollections(Set.of("10", "20"), actual.getDefaultPeriods());
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyDefineCalculationMethod() {
    // SETUP
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null));

    final var req = new RollingCalculationCommand();
    req.setRollingPeriods(Set.of("100"));
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var input = mock(CalculationDTO.class);

    when(sut.buildCalculationDto(req, returnFactorScale)).thenReturn(input);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(req);

    // VERIFY
    verify(sut).buildCalculationDto(req, returnFactorScale);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenCheckResult() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var req = new RollingCalculationCommand();
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;

    final var monthlyReturns = mock(Returns.class);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any()))
        .thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());

    // ACT
    final var actual = sut.buildCalculationDto(req, returnFactorScale);

    // VERIFY
    final CalculationDTO expected = new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioBaseTotalReturns);
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetPortfolioMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, null));

    final var holding = new FundSeriesHolding();
    holding.setFundServCode("RBF605");
    holding.setHoldingIdentifier(FUNDSERV);
    holding.setType(HoldingType.CANADA_MUTUAL_FUNDS);
    holding.setValue(BigDecimal.valueOf(50000));
    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setCurrency(Currency.CAD);
    req.setCustomPsd(LocalDate.now());
    req.setCustomPed(LocalDate.now().plusMonths(10));
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    // ACT
    sut.buildCalculationDto(req, returnFactorScale);

    // VERIFY
    verify(monthlyReturnsService).getPortfolioMonthlyReturns(req.getHoldings(), req.getCurrency(), returnFactorScale);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, null));

    final var holding = new FundSeriesHolding();
    holding.setFundServCode("RBF605");
    holding.setHoldingIdentifier(FUNDSERV);
    holding.setType(HoldingType.CANADA_MUTUAL_FUNDS);
    holding.setValue(BigDecimal.valueOf(50000));
    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setCurrency(Currency.CAD);
    req.setCustomPsd(LocalDate.now());
    req.setCustomPed(LocalDate.now().plusMonths(10));
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var monthlyReturns = mock(Returns.class);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    // ACT
    sut.buildCalculationDto(req, returnFactorScale);

    // VERIFY
    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, req.getCustomPsd(), req
        .getCustomPed());
  }
}