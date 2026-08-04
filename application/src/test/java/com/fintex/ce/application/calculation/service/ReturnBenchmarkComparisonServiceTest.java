package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.returns.pipeline.CpsdCpedScaleParams;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReturnBenchmarkComparisonServiceTest {

  private static final LocalDate MONTH_END = LocalDate.of(2024, 12, 31);

  private final BenchmarkMonthlyReturnsContextProvider contextProvider = mock(
      BenchmarkMonthlyReturnsContextProvider.class);
  private final BenchmarkWeightedAverageWithCpedPipeline cpedPipeline = mock(
      BenchmarkWeightedAverageWithCpedPipeline.class);
  private final BenchmarkWeightedAverageWithCpsdAndCpedPipeline cpsdAndCpedPipeline = mock(
      BenchmarkWeightedAverageWithCpsdAndCpedPipeline.class);
  private final ReturnBenchmarkComparisonService service = new ReturnBenchmarkComparisonService(
      contextProvider, cpedPipeline, cpsdAndCpedPipeline);

  @Test
  void shouldCompareEveryPortfolioValue_whenBenchmarkReturnsAreAvailable() {
    PortfolioHolding benchmarkHolding = mock(PortfolioHolding.class);
    ReturnCommand command = new ReturnCommand();
    command.setBenchmarkHoldings(List.of(benchmarkHolding));
    command.setCurrency(Currency.CAD);
    command.setCustomPsd(LocalDate.of(2024, 1, 31));
    command.setCustomPed(MONTH_END);
    MonthlyReturnsContext<HoldingMonthlyReturns> context = mock(MonthlyReturnsContext.class);
    WeightedAverageResult<HoldingMonthlyReturns> weightedAverage = weightedAverage();
    when(contextProvider.get(command.getBenchmarkHoldings(), Currency.CAD, Map.of())).thenReturn(context);
    when(cpsdAndCpedPipeline.run(context,
        new CpsdCpedScaleParams(command.getCustomPsd(), command.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO)))
        .thenReturn(weightedAverage);
    Notification warning = mock(Notification.class);
    AnnualReturnResult<Integer> benchmarkResult = new AnnualReturnResult<>();
    benchmarkResult.setAnnualReturns(List.of(
        new KeyValueResult<>(2022, new BigDecimal("0.05")),
        new KeyValueResult<>(2023, BigDecimal.ZERO)));
    benchmarkResult.setWarnings(List.of(warning));
    List<KeyValueResult<Integer>> portfolioReturns = List.of(
        new KeyValueResult<>(2022, new BigDecimal("0.10")),
        new KeyValueResult<>(2023, new BigDecimal("0.20")),
        new KeyValueResult<>(2024, null));

    var benchmarkWeightedAverage = service.benchmarkWeightedAverage(
        command, PortfolioBenchmarkReturns.EMPTY, ReturnFactorScale.SCALE_OF_TWO);
    var outcome = service.compare(new ReturnBenchmarkComparisonService.ReturnBenchmarkComparisonRequest<>(
        portfolioReturns,
        benchmarkWeightedAverage,
        ignored -> benchmarkResult,
        AnnualReturnResult<Integer>::getAnnualReturns));

    assertThat(outcome.comparison()).hasSize(3);
    assertThat(outcome.comparison().get(0).period()).isEqualTo(2022);
    assertThat(outcome.comparison().get(0).portfolio()).isEqualByComparingTo("0.10");
    assertThat(outcome.comparison().get(0).benchmark()).isEqualByComparingTo("0.05");
    assertThat(outcome.comparison().get(0).percentDifference()).isEqualByComparingTo("100");
    assertThat(outcome.comparison().get(1).period()).isEqualTo(2023);
    assertThat(outcome.comparison().get(1).portfolio()).isEqualByComparingTo("0.20");
    assertThat(outcome.comparison().get(1).benchmark()).isEqualByComparingTo("0");
    assertThat(outcome.comparison().get(1).percentDifference()).isNull();
    assertThat(outcome.comparison().get(2).period()).isEqualTo(2024);
    assertThat(outcome.comparison().get(2).portfolio()).isNull();
    assertThat(outcome.comparison().get(2).benchmark()).isNull();
    assertThat(outcome.comparison().get(2).percentDifference()).isNull();
    assertThat(outcome.warnings()).containsExactly(warning);
    verify(cpsdAndCpedPipeline).run(context,
        new CpsdCpedScaleParams(command.getCustomPsd(), command.getCustomPed(), ReturnFactorScale.SCALE_OF_TWO));
  }

  @Test
  void shouldReturnNullBenchmarkValuesAndWarning_whenBenchmarkReturnsAreUnavailable() {
    PortfolioHolding benchmarkHolding = mock(PortfolioHolding.class);
    PeriodCommand command = new PeriodCommand();
    command.setBenchmarkHoldings(List.of(benchmarkHolding));
    command.setCurrency(Currency.CAD);
    command.setCustomPed(MONTH_END);
    CalculationException exception = ErrorCode.NO_SECURITY_DATA_FOR_HOLDING.toException("benchmark");
    when(contextProvider.get(command.getBenchmarkHoldings(), Currency.CAD, Map.of())).thenThrow(exception);
    List<KeyValueResult<String>> portfolioReturns = List.of(
        new KeyValueResult<>("ONE_MTH", new BigDecimal("0.10")));
    var benchmarkWeightedAverage = service.benchmarkWeightedAverage(
        command, PortfolioBenchmarkReturns.EMPTY, ReturnFactorScale.SCALE_OF_TWO);
    var outcome = service.compare(new ReturnBenchmarkComparisonService.ReturnBenchmarkComparisonRequest<>(
        portfolioReturns,
        benchmarkWeightedAverage,
        ignored -> {
          throw new AssertionError("Benchmark calculation must not run without benchmark returns");
        },
        AnnualReturnResult<String>::getAnnualReturns));

    assertThat(outcome.comparison()).singleElement().satisfies(comparison -> {
      assertThat(comparison.period()).isEqualTo("ONE_MTH");
      assertThat(comparison.portfolio()).isEqualByComparingTo("0.10");
      assertThat(comparison.benchmark()).isNull();
      assertThat(comparison.percentDifference()).isNull();
    });
    assertThat(outcome.warnings()).singleElement().satisfies(warning -> {
      assertThat(warning.getCode()).isEqualTo(exception.getErrorCode().getCode());
      assertThat(warning.getMessage()).isEqualTo(exception.getMessage());
      assertThat(warning.getMetadata()).isEqualTo(exception.getMetadata());
    });
    verifyNoInteractions(cpedPipeline);
  }

  private static WeightedAverageResult<HoldingMonthlyReturns> weightedAverage() {
    return new WeightedAverageResult<>(
        new TreeMap<>(Map.of(MONTH_END, BigDecimal.ONE)),
        ReturnsSnapshot.empty());
  }
}
