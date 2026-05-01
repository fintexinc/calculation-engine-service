package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.LeadingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.ReturnsAggregate;
import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.dto.command.LeadingTotalReturnCommand;
import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.application.util.ReturnFactorScale.SCALE_OF_TWO;
import static com.fintex.ce.application.util.TestConstants.LOCAL_DATE_NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class LeadingTotalReturnsCalculationServiceImplTest {

  @Test
  void shouldPerform_whenVerifyDefineCalculationMethod() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var command = mock(LeadingTotalReturnCommand.class);
    final var leadingTotalReturnCalculation = mock(LeadingTotalReturnsCalculation.class);

    when(sut.defineCalculationMethod(any())).thenReturn(leadingTotalReturnCalculation);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(command);

    verify(sut).defineCalculationMethod(command);
  }

  @Test
  void shouldPerform_whenVerifyLeadingTotalReturnCalculationCalculate() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var command = mock(LeadingTotalReturnCommand.class);
    final var periods = mock(Set.class);
    final var leadingTotalReturnCalculation = mock(LeadingTotalReturnsCalculation.class);

    when(command.getPeriods()).thenReturn(periods);
    when(sut.defineCalculationMethod(any())).thenReturn(leadingTotalReturnCalculation);

    doCallRealMethod().when(sut).perform(any());
    sut.perform(command);

    verify(leadingTotalReturnCalculation).calculate(periods);

  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyBuildPeriodCalculationInput() {
    final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class);

    final var command = mock(LeadingTotalReturnCommand.class);
    final var input = mock(PeriodCalculationInput.class);

    when(sut.buildPeriodCalculationInput(any(), any())).thenReturn(input);

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    sut.defineCalculationMethod(command);

    verify(sut).buildPeriodCalculationInput(command, SCALE_OF_TWO);
  }

  @Test
  void shouldDefineCalculationMethod_whenVerifyLeadingTotalReturnCalculationConstructor() {
    final var defaultPeriods = Set.of("3", "6", "12", "24");
    final LeadingTotalReturnsCalculationServiceImpl sut = Mockito.spy(new LeadingTotalReturnsCalculationServiceImpl(
        null, defaultPeriods));

    final var command = mock(LeadingTotalReturnCommand.class);
    final var context = new PeriodCalculationInput();
    LocalDate cipsd = LocalDate.now();
    context.setCipsd(cipsd);
    context.setWeightedAveragePortfolioReturns(mock(TreeMap.class));

    doReturn(context).when(sut).buildPeriodCalculationInput(any(), any());

    doCallRealMethod().when(sut).defineCalculationMethod(any());
    final var actual = sut.defineCalculationMethod(command);

    ComparisonUtils.compareCollections(defaultPeriods, actual.getDefaultPeriods());
    assertEquals(context.getCipsd(), actual.getCipsd());
    assertEquals(context.getWeightedAveragePortfolioReturns(), actual.getPortfolioTotalReturns());
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetPortfolioMonthlyReturns() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var command = mock(LeadingTotalReturnCommand.class);
    final var holdings = mock(List.class);
    final var currency = Currency.CAD;
    when(command.getHoldings()).thenReturn(holdings);
    when(command.getCurrency()).thenReturn(currency);

    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_SELF);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    verify(monthlyReturnsService).getPortfolioMonthlyReturns(holdings, Currency.CAD, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenVerifyGetWeightedAverage() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var command = mock(LeadingTotalReturnCommand.class);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);

    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);

    when(monthlyReturns
        .validateCpsd(command.getCustomPsd())
        .validateReturns()
        .cutByPed()
        .cutByCpsdIfCpsdEmptyCutByPsd(command.getCustomPsd())
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(mock(TreeMap.class));

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());

    sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);
  }

  @Test
  void shouldBuildCalculationDto_whenCheckResult() {
    final var monthlyReturnsService = mock(MonthlyReturnsService.class);
    final var sut = mock(LeadingTotalReturnsCalculationServiceImpl.class, withSettings()
        .useConstructor(monthlyReturnsService, Set.of()));

    final var command = mock(LeadingTotalReturnCommand.class);
    when(command.getCustomPsd()).thenReturn(LOCAL_DATE_NOW);

    final var monthlyReturns = mock(ReturnsAggregate.class, RETURNS_DEEP_STUBS);
    final var portfolioBaseTotalReturn = mock(NavigableMap.class);
    when(monthlyReturns
        .validateCpsd(command.getCustomPsd())
        .cutByPed()
        .cutByCpsdIfCpsdEmptyCutByPsd(command.getCustomPsd())
        .fxRatesApplied()
        .getWeightedAverage()).thenReturn(portfolioBaseTotalReturn);

    when(monthlyReturnsService.getPortfolioMonthlyReturns(anyList(), any(), any())).thenReturn(monthlyReturns);
    doCallRealMethod().when(sut).buildPeriodCalculationInput(any(), any());

    final PeriodCalculationInput actual = sut.buildPeriodCalculationInput(command, SCALE_OF_TWO);

    final var expected = new PeriodCalculationInput(portfolioBaseTotalReturn);
    assertEquals(actual, expected);

  }

}