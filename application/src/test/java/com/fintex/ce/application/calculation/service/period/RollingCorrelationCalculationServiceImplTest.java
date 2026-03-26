package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RollingCorrelationCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.ReturnFactorScale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class RollingCorrelationCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(Holding.class));

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(reqDTO)).thenReturn(mock(RollingCorrelationCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).defineCalculationMethod(reqDTO);

  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = List.of(mock(Holding.class));
    final var rollingCorrelationCalculation = mock(RollingCorrelationCalculation.class);
    final var rollingPeriods = Set.of("12");

    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(sut.defineCalculationMethod(reqDTO)).thenReturn(rollingCorrelationCalculation);
    when(reqDTO.getRollingPeriods()).thenReturn(rollingPeriods);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(rollingCorrelationCalculation).calculate(rollingPeriods);

  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildCalculationDto() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var input = mock(BenchmarkCalculationDTO.class);

    when(sut.buildCalculationDto(any(), any())).thenReturn(input);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    // ACT
    sut.defineCalculationMethod(reqDTO);

    // VERIFY
    verify(sut).buildCalculationDto(reqDTO, SCALE_OF_TWO);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyGetBaseTotalReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var input = mock(BenchmarkCalculationDTO.class);

    when(sut.buildCalculationDto(any(), any())).thenReturn(input);

    doCallRealMethod().when(sut).defineCalculationMethod(any());

    // ACT
    sut.defineCalculationMethod(reqDTO);

    // VERIFY
    verify(sut).getBaseTotalReturns(reqDTO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(Returns.class));
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(mock(Returns.class));
    doCallRealMethod().when(sut).buildCalculationDto(any(RollingCalculationCommand.class), any(ReturnFactorScale.class));

    // ACT
    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    // VERIFY
    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetBenchmarkMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(reqDTO.getBenchmarkHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(Returns.class));
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(mock(Returns.class));
    doCallRealMethod().when(sut).buildCalculationDto(any(RollingCalculationCommand.class), any(ReturnFactorScale.class));

    // ACT
    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    // VERIFY
    verify(monthlyReturnsService).getBenchmarkMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyPortfolioMonthlyReturnsCutArgumentToTheSameEndDateWhenPedIsGreater() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(reqDTO.getBenchmarkHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    final var portfolioMonthlyReturns = mock(Returns.class);
    final var benchmarkMonthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(RollingCalculationCommand.class), any(ReturnFactorScale.class));

    // ACT
    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    // VERIFY
    verify(portfolioMonthlyReturns).cutArgumentToTheSameEndDate(benchmarkMonthlyReturns);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyBenchmarkMonthlyReturnsCutArgumentToTheSameEndDateWhenPedIsGreater() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    final var holdings = mock(List.class);
    when(reqDTO.getBenchmarkHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    final var portfolioMonthlyReturns = mock(Returns.class);
    final var benchmarkMonthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(RollingCalculationCommand.class), any(ReturnFactorScale.class));

    // ACT
    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    // VERIFY
    verify(benchmarkMonthlyReturns).cutArgumentToTheSameEndDate(portfolioMonthlyReturns);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyPortfolioMonthlyReturnsGetWeightedAverageWithCpsdAndCpedValidation() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    when(reqDTO.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(reqDTO.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));

    final var portfolioMonthlyReturns = mock(Returns.class);
    final var benchmarkMonthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    final var portfolioMonthlyReturnsAfterCut = mock(Returns.class);
    when(portfolioMonthlyReturns.cutArgumentToTheSameEndDate(any())).thenReturn(portfolioMonthlyReturnsAfterCut);

    doCallRealMethod().when(sut).buildCalculationDto(any(RollingCalculationCommand.class), any(ReturnFactorScale.class));

    // ACT
    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    // VERIFY
    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(
        same(portfolioMonthlyReturns), eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)));
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyBenchmarkMonthlyReturnsGetWeightedAverageWithCpsdAndCpedValidation() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    when(reqDTO.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(reqDTO.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));

    final var portfolioMonthlyReturns = mock(Returns.class);
    final var benchmarkMonthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    final var benchmarkMonthlyReturnsAfterCut = mock(Returns.class);
    when(benchmarkMonthlyReturns.cutArgumentToTheSameEndDate(any())).thenReturn(benchmarkMonthlyReturnsAfterCut);

    doCallRealMethod().when(sut).buildCalculationDto(any(RollingCalculationCommand.class), any(ReturnFactorScale.class));

    // ACT
    sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    // VERIFY
    verify(monthlyReturnsService).getWeightedAverageWithCpsdAndCpedValidation(
        same(benchmarkMonthlyReturns), eq(LOCAL_DATE_NOW), eq(LOCAL_DATE_NOW.plusMonths(1)));
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var reqDTO = mock(RollingCalculationCommand.class);
    when(reqDTO.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);
    when(reqDTO.getCustomPed()).thenReturn(LOCAL_DATE_NOW.plusMonths(1));
    when(reqDTO.getCustomIntervalPsd()).thenReturn(LOCAL_DATE_NOW.plusDays(3));

    final var portfolioMonthlyReturns = mock(Returns.class);
    final var benchmarkMonthlyReturns = mock(Returns.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(portfolioMonthlyReturns);
    when(monthlyReturnsService.getBenchmarkMonthlyReturns(anyList(), any(), any())).thenReturn(benchmarkMonthlyReturns);

    final var portfolioMonthlyReturnsAfterCut = mock(Returns.class);
    final var benchmarkMonthlyReturnsAfterCut = mock(Returns.class);
    when(portfolioMonthlyReturns.cutArgumentToTheSameEndDate(any())).thenReturn(benchmarkMonthlyReturnsAfterCut);
    when(benchmarkMonthlyReturns.cutArgumentToTheSameEndDate(any())).thenReturn(portfolioMonthlyReturnsAfterCut);

    final var portfolioTotalReturns = mock(TreeMap.class);
    final var benchmarkTotalReturns = mock(TreeMap.class);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(
        portfolioMonthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(1)))
        .thenReturn(portfolioTotalReturns);
    when(monthlyReturnsService.getWeightedAverageWithCpsdAndCpedValidation(
        benchmarkMonthlyReturns, LOCAL_DATE_NOW, LOCAL_DATE_NOW.plusMonths(1)))
        .thenReturn(benchmarkTotalReturns);

    doCallRealMethod().when(sut).buildCalculationDto(any(RollingCalculationCommand.class), any(ReturnFactorScale.class));

    // ACT
    final var actual = sut.buildCalculationDto(reqDTO, SCALE_OF_TWO);

    // VERIFY
    final var expected = new BenchmarkCalculationDTO()
        .setWeightedAverageBenchmarkReturns(benchmarkTotalReturns)
        .setWeightedAveragePortfolioReturns(portfolioTotalReturns)
        .setCipsd(LOCAL_DATE_NOW.plusDays(3));

    assertEquals(expected, actual);
  }

  @Test
  void shouldGetBaseTotalReturns_whenVerifyGetPortfolioMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var reqDTO = mock(RollingCalculationCommand.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(mock(Returns.class,
        RETURNS_SELF));

    doCallRealMethod().when(sut).getBaseTotalReturns(any());

    // ACT
    sut.getBaseTotalReturns(reqDTO);

    // VERIFY
    verify(monthlyReturnsService).getPortfolioMonthlyReturns(same(holdings), eq(Currency.CAD), eq(SCALE_OF_TWO));
  }

  @Test
  void shouldGetBaseTotalReturns_whenVerifyGetMonthlyReturns() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var reqDTO = mock(RollingCalculationCommand.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    final Returns monthlyReturns = mock(Returns.class, RETURNS_SELF);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);

    doCallRealMethod().when(sut).getBaseTotalReturns(any());

    // ACT
    sut.getBaseTotalReturns(reqDTO);

    // VERIFY
    verify(monthlyReturns).getReturnsMap();
  }

  @Test
  void shouldGetBaseTotalReturns_whenCheckResult() {
    // SETUP
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var defaultPeriods = Set.of();
    final var sut = mock(RollingCorrelationCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, defaultPeriods));

    final var holdings = mock(List.class);
    final var reqDTO = mock(RollingCalculationCommand.class);
    when(reqDTO.getHoldings()).thenReturn(holdings);
    when(reqDTO.getCurrency()).thenReturn(Currency.CAD);

    final Returns monthlyReturns = mock(Returns.class, RETURNS_DEEP_STUBS);
    final Map baseTotalReturn = mock(Map.class);
    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
    when(monthlyReturns
        .validateCped(reqDTO.getCustomPed())
        .cutByCpedIfCpedEmptyCutByPed(reqDTO.getCustomPed())
        .fxRatesApplied()
        .getReturnsMap()).thenReturn(baseTotalReturn);

    doCallRealMethod().when(sut).getBaseTotalReturns(any());

    // ACT
    final Map<Holding, Map<LocalDate, BigDecimal>> actual = sut.getBaseTotalReturns(reqDTO);

    // VERIFY
    assertSame(baseTotalReturn, actual);
  }

}