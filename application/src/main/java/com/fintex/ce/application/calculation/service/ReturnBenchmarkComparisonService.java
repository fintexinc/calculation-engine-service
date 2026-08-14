package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.returns.pipeline.CpedScaleParams;
import com.fintex.ce.application.returns.pipeline.CpsdCpedScaleParams;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.domain.result.returns.ReturnComparison;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;

@Service
@RequiredArgsConstructor
public class ReturnBenchmarkComparisonService {

  private static final Set<ErrorCode> UNAVAILABLE_BENCHMARK_ERRORS = Set.of(
      ErrorCode.MISSING_MONTHLY_RETURNS,
      ErrorCode.NO_SECURITY_DATA_FOR_HOLDING,
      ErrorCode.NO_COMPLETE_CALENDAR_YEAR,
      ErrorCode.INCOMPLETE_YEAR_SKIPPED);

  private final BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider;
  private final BenchmarkWeightedAverageWithCpedPipeline benchmarkWeightedAverageWithCped;
  private final BenchmarkWeightedAverageWithCpsdAndCpedPipeline benchmarkWeightedAverageWithCpsdAndCped;

  public <K, R extends BaseCalculationResult> ComparisonOutcome<K> compare(
      ReturnBenchmarkComparisonRequest<K, R> request) {
    WeightedAverageResult<HoldingMonthlyReturns> benchmarkWeightedAverage = request.benchmarkWeightedAverage();
    if (CollectionUtils.isEmpty(benchmarkWeightedAverage.weightedAverage())) {
      return new ComparisonOutcome<>(
          compare(request.portfolioReturns(), List.of()),
          benchmarkWeightedAverage.getErrorsAsWarnings());
    }

    try {
      R benchmarkResult = request.benchmarkCalculation().apply(benchmarkWeightedAverage);
      return new ComparisonOutcome<>(
          compare(request.portfolioReturns(), request.benchmarkReturnsExtractor().apply(benchmarkResult)),
          benchmarkResult.getWarnings());
    } catch (CalculationException exception) {
      if (!UNAVAILABLE_BENCHMARK_ERRORS.contains(exception.getErrorCode())) {
        throw exception;
      }
      return new ComparisonOutcome<>(
          compare(request.portfolioReturns(), List.of()),
          mergeWarnings(benchmarkWeightedAverage.getErrorsAsWarnings(), toWarnings(exception)));
    }
  }

  public WeightedAverageResult<HoldingMonthlyReturns> benchmarkWeightedAverage(
      PeriodCommand command,
      PortfolioBenchmarkReturns returnsData,
      ReturnFactorScale scale) {
    return calculateBenchmarkWeightedAverage(() -> {
      MonthlyReturnsContext<HoldingMonthlyReturns> benchmarkContext = benchmarkMonthlyReturnsContextProvider.get(
          command.getBenchmarkHoldings(), command.getCurrency(), returnsData.benchmark());
      return benchmarkWeightedAverageWithCped.run(benchmarkContext,
          new CpedScaleParams(command.getCustomPed(), scale));
    });
  }

  public WeightedAverageResult<HoldingMonthlyReturns> benchmarkWeightedAverage(
      ReturnCommand command,
      PortfolioBenchmarkReturns returnsData,
      ReturnFactorScale scale) {
    return calculateBenchmarkWeightedAverage(() -> {
      MonthlyReturnsContext<HoldingMonthlyReturns> benchmarkContext = benchmarkMonthlyReturnsContextProvider.get(
          command.getBenchmarkHoldings(), command.getCurrency(), returnsData.benchmark());
      return benchmarkWeightedAverageWithCpsdAndCped.run(benchmarkContext,
          new CpsdCpedScaleParams(command.getCustomPsd(), command.getCustomPed(), scale));
    });
  }

  public List<Notification> mergeWarnings(List<Notification> portfolioWarnings,
      List<Notification> benchmarkWarnings) {
    List<Notification> warnings = new ArrayList<>(portfolioWarnings);
    warnings.addAll(benchmarkWarnings);
    return warnings;
  }

  private WeightedAverageResult<HoldingMonthlyReturns> calculateBenchmarkWeightedAverage(
      Supplier<WeightedAverageResult<HoldingMonthlyReturns>> calculation) {
    try {
      return calculation.get();
    } catch (CalculationException exception) {
      if (!UNAVAILABLE_BENCHMARK_ERRORS.contains(exception.getErrorCode())) {
        throw exception;
      }
      ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot
          .<HoldingMonthlyReturns>empty()
          .withAddedErrors(List.of(exception));
      return new WeightedAverageResult<>(new TreeMap<>(), snapshot);
    }
  }

  private static <K> List<ReturnComparison<K>> compare(List<KeyValueResult<K>> portfolioReturns,
      List<KeyValueResult<K>> benchmarkReturns) {
    Map<K, KeyValueResult<K>> benchmarksByPeriod = benchmarkReturns.stream()
        .collect(Collectors.toMap(KeyValueResult::key, Function.identity()));
    return portfolioReturns.stream()
        .map(portfolioReturn -> compare(portfolioReturn, benchmarksByPeriod.get(portfolioReturn.key())))
        .toList();
  }

  private static <K> ReturnComparison<K> compare(KeyValueResult<K> portfolioReturn,
      KeyValueResult<K> benchmarkReturn) {
    BigDecimal benchmarkValue = benchmarkReturn == null ? null : benchmarkReturn.value();
    return new ReturnComparison<>(
        portfolioReturn.key(),
        portfolioReturn.value(),
        benchmarkValue,
        percentDifference(portfolioReturn.value(), benchmarkValue));
  }

  private static BigDecimal percentDifference(BigDecimal portfolioReturn, BigDecimal benchmarkReturn) {
    if (portfolioReturn == null || benchmarkReturn == null || benchmarkReturn.signum() == 0) {
      return null;
    }
    return toUserScale(divide(portfolioReturn.subtract(benchmarkReturn), benchmarkReturn).multiply(HUNDRED));
  }

  private static List<Notification> toWarnings(CalculationException exception) {
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = ReturnsSnapshot
        .<HoldingMonthlyReturns>empty()
        .withAddedErrors(List.of(exception));
    return snapshot.getErrorsAsWarnings();
  }

  public record ComparisonOutcome<K>(List<ReturnComparison<K>> comparison, List<Notification> warnings) {
  }

  public record ReturnBenchmarkComparisonRequest<K, R extends BaseCalculationResult>(
      List<KeyValueResult<K>> portfolioReturns,
      WeightedAverageResult<HoldingMonthlyReturns> benchmarkWeightedAverage,
      Function<WeightedAverageResult<HoldingMonthlyReturns>, R> benchmarkCalculation,
      Function<R, List<KeyValueResult<K>>> benchmarkReturnsExtractor) {
  }
}
