package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.domain.result.rolling.RollingTotalReturnsResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.wm.commons.domain.id.FiIdentifierType.FUNDSERV;
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
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null));

    final var holding = new Holding(BigDecimal.valueOf(50000), FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("RBF605", FUNDSERV));

    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setRollingPeriods(Set.of("12", "11", "10"));
    req.setCurrency(Currency.CAD);

    final var rollingCalculation = mock(RollingTotalReturnsCalculation.class);
    final var expected = mock(RollingTotalReturnsResult.class);

    when(sut.defineCalculationMethod(any())).thenReturn(rollingCalculation);
    when(rollingCalculation.calculate(any())).thenReturn(expected);

    doCallRealMethod().when(sut).perform(any());
    final var actual = sut.perform(req);

    assertSame(expected, actual);
  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null));

    final var holding = new Holding(BigDecimal.valueOf(50000), FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("RBF605", FUNDSERV));

    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setRollingPeriods(Set.of("12", "11", "10"));
    req.setCurrency(Currency.CAD);

    final var rollingCalculation = mock(RollingTotalReturnsCalculation.class);

    when(sut.defineCalculationMethod(any())).thenReturn(rollingCalculation);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(req);

    verify(rollingCalculation).calculate(req.getRollingPeriods());
  }

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null));

    final var holding = new Holding(BigDecimal.valueOf(50000), FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("RBF605", FUNDSERV));

    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setCurrency(Currency.CAD);

    final var rollingCalculation = mock(RollingTotalReturnsCalculation.class);

    when(sut.defineCalculationMethod(req)).thenReturn(rollingCalculation);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(req);

    verify(sut).defineCalculationMethod(req);
  }

  @Test
  void shouldDefineCalculationMethod_whenCheckResult() {
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
    final var actual = sut.defineCalculationMethod(req);

    assertEquals(inputDTO.getWeightedAveragePortfolioReturns(), actual.getPortfolioTotalReturns());
    ComparisonUtils.compareCollections(Set.of("10", "20"), actual.getDefaultPeriods());
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyDefineCalculationMethod() {
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(null, null));

    final var req = new RollingCalculationCommand();
    req.setRollingPeriods(Set.of("100"));
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var input = mock(CalculationDTO.class);

    when(sut.buildCalculationDto(req, returnFactorScale)).thenReturn(input);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(req);

    verify(sut).buildCalculationDto(req, returnFactorScale);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var req = new RollingCalculationCommand();
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;

    final var monthlyReturns = mock(ReturnsAggregate.class);
    final var portfolioBaseTotalReturns = mock(TreeMap.class);

    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(any(), any(), any()))
        .thenReturn(portfolioBaseTotalReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());

    final var actual = sut.buildCalculationDto(req, returnFactorScale);

    final CalculationDTO expected = new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioBaseTotalReturns);
    Assertions.assertEquals(expected, actual);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, null));

    final var holding = new Holding(BigDecimal.valueOf(50000), FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("RBF605", FUNDSERV));
    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setCurrency(Currency.CAD);
    req.setCustomPsd(LocalDate.now());
    req.setCustomPed(LocalDate.now().plusMonths(10));
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;

    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    sut.buildCalculationDto(req, returnFactorScale);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(req.getHoldings(), req.getCurrency(), returnFactorScale);
  }

  @Test
  void shouldBuildWeightedAverageInputDto_whenVerifyGetWeightedAverageWithCpsdAndCpedValidation() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(RollingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, null));

    final var holding = new Holding(BigDecimal.valueOf(50000), FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("RBF605", FUNDSERV));
    final var req = new RollingCalculationCommand();
    req.setHoldings(List.of(holding));
    req.setCurrency(Currency.CAD);
    req.setCustomPsd(LocalDate.now());
    req.setCustomPed(LocalDate.now().plusMonths(10));
    final var returnFactorScale = ReturnFactorScale.SCALE_OF_TWO;
    final var monthlyReturns = mock(ReturnsAggregate.class);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
    doCallRealMethod().when(sut).buildCalculationDto(any(), any());
    sut.buildCalculationDto(req, returnFactorScale);

    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, req.getCustomPsd(), req
        .getCustomPed());
  }
}
