package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.ReturnBenchmarkComparisonService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_MTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class TrailingTotalReturnsCalculationServiceImplImplTest {

  @Test
  void shouldPerform_whenVerifyBuildPeriodCalculationInput() {
    var monthlyReturnsService = mock(PortfolioMonthlyReturnsContextProvider.class);
    var treasuryBillsFetcher = mock(TreasuryBillsFetcher.class);
    var service = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(monthlyReturnsService, null, treasuryBillsFetcher,
            new PeriodProperties(), null));

    PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(treasuryBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(service.buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_TWO, PortfolioBenchmarkReturns.EMPTY))
        .thenReturn(new PeriodCalculationInput());

    doCallRealMethod().when(service).perform(req, PortfolioBenchmarkReturns.EMPTY);
    try (var ignored = mockConstruction(TrailingTotalReturnsCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(service).buildPeriodCalculationInput(req, ReturnFactorScale.SCALE_OF_TWO, PortfolioBenchmarkReturns.EMPTY);
  }

  @Test
  void shouldFetchTBills_whenPerforming() {
    var treasuryBillsFetcher = mock(TreasuryBillsFetcher.class);
    var service = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, treasuryBillsFetcher, new PeriodProperties(), null));

    PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.CAD);
    when(treasuryBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(LocalDate.now(), BigDecimal.ONE)));
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(new PeriodCalculationInput());

    doCallRealMethod().when(service).perform(any(), any());
    try (var ignored = mockConstruction(TrailingTotalReturnsCalculation.class)) {
      service.perform(req, PortfolioBenchmarkReturns.EMPTY);
    }

    verify(treasuryBillsFetcher).fetch(Currency.CAD);
  }

  @Test
  void shouldThrowTBillSeriesNotAvailable_whenTBillSeriesEmptyForRequestedCurrency() {
    var treasuryBillsFetcher = mock(TreasuryBillsFetcher.class);
    var service = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, treasuryBillsFetcher, new PeriodProperties(), null));

    PeriodCommand req = mock(PeriodCommand.class);
    when(req.getCurrency()).thenReturn(Currency.EUR);
    when(service.buildPeriodCalculationInput(any(), any(), any())).thenReturn(new PeriodCalculationInput());
    when(treasuryBillsFetcher.fetch(Currency.EUR)).thenReturn(new TreeMap<>());

    doCallRealMethod().when(service).perform(any(), any());

    CalculationException ex = assertThrows(CalculationException.class,
        () -> service.perform(req, PortfolioBenchmarkReturns.EMPTY));
    assertEquals(ErrorCode.TBILL_SERIES_NOT_AVAILABLE_FOR_CURRENCY, ex.getErrorCode());
  }

  @ParameterizedTest
  @MethodSource("benchmarkReturnScenarios")
  void shouldReturnPortfolioBenchmarkAndExpectedComparison_whenBenchmarkHoldingsAreProvided(
      BigDecimal benchmarkMonthlyReturn, BigDecimal expectedBenchmarkReturn, BigDecimal expectedPercentDifference) {
    TrailingTotalReturnsResult result = calculateBenchmarkComparison(benchmarkMonthlyReturn, List.of());

    assertThat(result.getTrailingTotalReturn()).singleElement().satisfies(portfolio -> {
      assertThat(portfolio.period()).isEqualTo(ONE_MTH.name());
      assertThat(portfolio.value()).isEqualByComparingTo("0.1");
    });
    assertThat(result.getComparison()).singleElement().satisfies(comparison -> {
      assertThat(comparison.period()).isEqualTo(ONE_MTH);
      assertThat(comparison.portfolio()).isEqualByComparingTo("0.1");
      assertThat(comparison.benchmark()).isEqualByComparingTo(expectedBenchmarkReturn);
      if (expectedPercentDifference == null) {
        assertThat(comparison.percentDifference()).isNull();
      } else {
        assertThat(comparison.percentDifference()).isEqualByComparingTo(expectedPercentDifference);
      }
    });
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldReturnUnavailableBenchmarkResultsAndWarnings_whenBenchmarkWeightedAverageIsEmpty() {
    Notification benchmarkWarning = ErrorCode.MISSING_MONTHLY_RETURNS.asNotification();
    TrailingTotalReturnsResult result = calculateBenchmarkComparison(null, List.of(benchmarkWarning));

    assertThat(result.getTrailingTotalReturn()).singleElement().satisfies(portfolio -> {
      assertThat(portfolio.period()).isEqualTo(ONE_MTH.name());
      assertThat(portfolio.value()).isEqualByComparingTo("0.1");
    });
    assertThat(result.getComparison()).singleElement().satisfies(comparison -> {
      assertThat(comparison.period()).isEqualTo(ONE_MTH);
      assertThat(comparison.portfolio()).isEqualByComparingTo("0.1");
      assertThat(comparison.benchmark()).isNull();
      assertThat(comparison.percentDifference()).isNull();
    });
    assertThat(result.getWarnings()).containsExactly(benchmarkWarning);
  }

  @ParameterizedTest
  @MethodSource("unavailableBenchmarkErrors")
  void shouldReturnUnavailableBenchmarkResultsAndWarnings_whenBenchmarkReturnsAreUnavailable(ErrorCode errorCode) {
    CalculationException exception = errorCode.toException("benchmark");
    TrailingTotalReturnsResult result = calculateUnavailableBenchmarkComparison(exception);

    assertThat(result.getTrailingTotalReturn()).singleElement().satisfies(portfolio -> {
      assertThat(portfolio.period()).isEqualTo(ONE_MTH.name());
      assertThat(portfolio.value()).isEqualByComparingTo("0.1");
    });
    assertThat(result.getComparison()).singleElement().satisfies(comparison -> {
      assertThat(comparison.period()).isEqualTo(ONE_MTH);
      assertThat(comparison.portfolio()).isEqualByComparingTo("0.1");
      assertThat(comparison.benchmark()).isNull();
      assertThat(comparison.percentDifference()).isNull();
    });
    assertThat(result.getWarnings()).singleElement().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(errorCode.getCode());
      assertThat(warning.getMessage()).isEqualTo(exception.getMessage());
      assertThat(warning.getMetadata()).isEqualTo(exception.getMetadata());
    });
  }

  private static TrailingTotalReturnsResult calculateBenchmarkComparison(BigDecimal benchmarkMonthlyReturn,
      List<Notification> benchmarkWarnings) {
    var treasuryBillsFetcher = mock(TreasuryBillsFetcher.class);
    var benchmarkReturnsProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
    var benchmarkWeightedAveragePipeline = mock(BenchmarkWeightedAverageWithCpedPipeline.class);
    var benchmarkContext = mock(MonthlyReturnsContext.class);
    var benchmarkWeightedAverage = mock(WeightedAverageResult.class);
    var comparisonService = new ReturnBenchmarkComparisonService(
        benchmarkReturnsProvider, benchmarkWeightedAveragePipeline, null);
    var service = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, treasuryBillsFetcher, new PeriodProperties(), comparisonService));
    LocalDate date = LocalDate.of(2024, 12, 31);
    PeriodCalculationInput input = new PeriodCalculationInput();
    input.setWeightedAveragePortfolioReturns(new TreeMap<>(Map.of(date, new BigDecimal("1.10"))));
    var benchmarkWeightedAverageReturns = new TreeMap<LocalDate, BigDecimal>();
    if (benchmarkMonthlyReturn != null) {
      benchmarkWeightedAverageReturns.put(date, benchmarkMonthlyReturn);
    }
    PeriodCommand command = mock(PeriodCommand.class);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getPeriods()).thenReturn(Set.of(ONE_MTH));
    when(command.getBenchmarkHoldings()).thenReturn(List.of(mock(PortfolioHolding.class)));
    when(service.buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO, PortfolioBenchmarkReturns.EMPTY))
        .thenReturn(input);
    when(treasuryBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(date, BigDecimal.ONE)));
    when(benchmarkReturnsProvider.get(any(), any(), any())).thenReturn(benchmarkContext);
    when(benchmarkWeightedAveragePipeline.run(any(), any())).thenReturn(benchmarkWeightedAverage);
    when(benchmarkWeightedAverage.weightedAverage()).thenReturn(benchmarkWeightedAverageReturns);
    when(benchmarkWeightedAverage.getErrorsAsWarnings()).thenReturn(benchmarkWarnings);
    doCallRealMethod().when(service).perform(command, PortfolioBenchmarkReturns.EMPTY);

    return service.perform(command, PortfolioBenchmarkReturns.EMPTY);
  }

  private static Stream<Arguments> benchmarkReturnScenarios() {
    return Stream.of(
        Arguments.of(new BigDecimal("1.05"), new BigDecimal("0.05"), new BigDecimal("100")),
        Arguments.of(BigDecimal.ONE, BigDecimal.ZERO, null));
  }

  private static TrailingTotalReturnsResult calculateUnavailableBenchmarkComparison(CalculationException exception) {
    var treasuryBillsFetcher = mock(TreasuryBillsFetcher.class);
    var benchmarkReturnsProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
    var comparisonService = new ReturnBenchmarkComparisonService(benchmarkReturnsProvider, null, null);
    var service = mock(TrailingTotalReturnsCalculationServiceImpl.class,
        withSettings().useConstructor(null, null, treasuryBillsFetcher, new PeriodProperties(), comparisonService));
    LocalDate date = LocalDate.of(2024, 12, 31);
    PeriodCalculationInput input = new PeriodCalculationInput();
    input.setWeightedAveragePortfolioReturns(new TreeMap<>(Map.of(date, new BigDecimal("1.10"))));
    PeriodCommand command = mock(PeriodCommand.class);
    when(command.getCurrency()).thenReturn(Currency.CAD);
    when(command.getPeriods()).thenReturn(Set.of(ONE_MTH));
    when(command.getBenchmarkHoldings()).thenReturn(List.of(mock(PortfolioHolding.class)));
    when(service.buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO, PortfolioBenchmarkReturns.EMPTY))
        .thenReturn(input);
    when(treasuryBillsFetcher.fetch(Currency.CAD))
        .thenReturn(new TreeMap<>(Map.of(date, BigDecimal.ONE)));
    when(benchmarkReturnsProvider.get(any(), any(), any())).thenThrow(exception);
    doCallRealMethod().when(service).perform(command, PortfolioBenchmarkReturns.EMPTY);

    return service.perform(command, PortfolioBenchmarkReturns.EMPTY);
  }

  private static Stream<ErrorCode> unavailableBenchmarkErrors() {
    return Stream.of(ErrorCode.MISSING_MONTHLY_RETURNS, ErrorCode.NO_SECURITY_DATA_FOR_HOLDING);
  }

}
