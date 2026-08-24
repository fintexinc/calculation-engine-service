package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.calculation.CalculationOrchestrator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.CommonPerformanceDatesResult;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationEMResult;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.ce.model.domain.result.allocation.ClassificationAllocationResult;
import com.fintex.ce.model.domain.result.allocation.ConsolidatedSectorExposureResult;
import com.fintex.ce.model.domain.result.allocation.CreditQualityResult;
import com.fintex.ce.model.domain.result.allocation.EquityMarketCapResult;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.domain.result.allocation.MaturityAllocationResult;
import com.fintex.ce.model.domain.result.composite.CompositeCalculationResult;
import com.fintex.ce.model.domain.result.correlation.CorrelationResult;
import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsResult;
import com.fintex.ce.model.domain.result.exposure.CountryExposureResult;
import com.fintex.ce.model.domain.result.exposure.EquityCountryExposureResult;
import com.fintex.ce.model.domain.result.exposure.EquityStyleboxExposureResult;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.model.domain.result.exposure.GeographicExposureResult;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.domain.result.fee.ManagementFeeResult;
import com.fintex.ce.model.domain.result.fee.SalesChargeResult;
import com.fintex.ce.model.domain.result.holding.NumberOfUniqueHoldingsResult;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.domain.result.income.IncomeForecastResult;
import com.fintex.ce.model.domain.result.income.YieldResult;
import com.fintex.ce.model.domain.result.period.BestWorstPeriodsResult;
import com.fintex.ce.model.domain.result.returns.AnnualReturnResult;
import com.fintex.ce.model.domain.result.returns.ExcessReturnsResult;
import com.fintex.ce.model.domain.result.returns.Growth10KResult;
import com.fintex.ce.model.domain.result.returns.LeadingTotalReturnsResult;
import com.fintex.ce.model.domain.result.returns.MeanResult;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.domain.result.risk.AlphaResult;
import com.fintex.ce.model.domain.result.risk.BetaResult;
import com.fintex.ce.model.domain.result.risk.DownsideCaptureResult;
import com.fintex.ce.model.domain.result.risk.DownsideDeviationResult;
import com.fintex.ce.model.domain.result.risk.InformationRatioResult;
import com.fintex.ce.model.domain.result.risk.MarRatioResult;
import com.fintex.ce.model.domain.result.risk.MaxDrawdownResult;
import com.fintex.ce.model.domain.result.risk.RSquaredResult;
import com.fintex.ce.model.domain.result.risk.SharpeRatioResult;
import com.fintex.ce.model.domain.result.risk.SortinoRatioResult;
import com.fintex.ce.model.domain.result.risk.StandardDeviationResult;
import com.fintex.ce.model.domain.result.risk.TrackingErrorResult;
import com.fintex.ce.model.domain.result.risk.TreynorRatioResult;
import com.fintex.ce.model.domain.result.risk.UpsideCaptureResult;
import com.fintex.ce.model.domain.result.rolling.RollingCorrelationResult;
import com.fintex.ce.model.domain.result.rolling.RollingSharpeRatioResult;
import com.fintex.ce.model.domain.result.rolling.RollingStandardDeviationResult;
import com.fintex.ce.model.domain.result.rolling.RollingTotalReturnsResult;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.CompositeCalculationRequest;
import com.fintex.ce.model.dto.command.PortfolioBenchmarkCommand;
import com.fintex.ce.model.dto.command.contract.HoldingsProvider;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.port.observability.CalculationObservability;

import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.groupingBy;

/**
 * REST entry point for portfolio calculations. The controller owns the request lifecycle — metric resolution, request
 * validation and observability — and delegates the actual calculation, including Security Master data fetching, to the
 * {@link CalculationOrchestrator}. For composite requests the shared top-level holdings, data providers and currency
 * are propagated into every nested command that does not carry its own value, so the per-command validation chain keeps
 * working unchanged.
 *
 * <p>
 * A request is resolved and validated before it is handed to {@link CalculationObservability}, so the observed scope
 * begins where the calculation does. A request rejected at the boundary never reached a calculator, and counting it as
 * a failed execution would mean a client sending malformed requests could raise the failure rate of a metric that is
 * working perfectly.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(PortfolioCalculationController.BASE_PATH)
@Tag(name = "Portfolio Calculations", description = "Unified endpoint for all portfolio calculation metrics: "
    + "returns, risk, benchmark comparison, allocations, fees, and income")
public class PortfolioCalculationController {
  public static final String BASE_PATH = "/api/v1/portfolio/calculations";

  private final CalculationOrchestrator calculationOrchestrator;
  private final RequestValidationFacade validationFacade;
  private final CalculationObservability calculationObservability;
  private final Validator validator;

  @Operation(summary = "Execute a portfolio calculation", description = "Performs the specified calculation metric on the provided portfolio holdings. "
      + "The request body schema depends on the metric — period-based metrics require time intervals, "
      + "breakdown metrics require holdings, and fee metrics require parameter types.")
  @ApiResponse(responseCode = "200", description = "Calculation result", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(oneOf = {
      TrailingTotalReturnsResult.class, LeadingTotalReturnsResult.class,
      RollingTotalReturnsResult.class, ExcessReturnsResult.class,
      AnnualReturnResult.class, Growth10KResult.class,
      BestWorstPeriodsResult.class, DistributionOfReturnsResult.class,
      StandardDeviationResult.class, RollingStandardDeviationResult.class,
      MeanResult.class, SharpeRatioResult.class,
      RollingSharpeRatioResult.class, SortinoRatioResult.class,
      MaxDrawdownResult.class, DownsideDeviationResult.class,
      MarRatioResult.class, TreynorRatioResult.class,
      InformationRatioResult.class, TrackingErrorResult.class,
      AlphaResult.class, BetaResult.class,
      RSquaredResult.class, CorrelationResult.class,
      RollingCorrelationResult.class, UpsideCaptureResult.class,
      DownsideCaptureResult.class,
      AssetAllocationResult.class, AssetAllocationEMResult.class,
      EquitySectorResult.class, ConsolidatedSectorExposureResult.class,
      EquityCountryExposureResult.class,
      EquityStyleboxExposureResult.class, GeographicExposureResult.class,
      EquityMarketCapResult.class, CountryExposureResult.class,
      FixedIncomeSectorResult.class, FixedIncomeStyleboxExposureResult.class,
      MaturityAllocationResult.class, ClassificationAllocationResult.class,
      AverageMerResult.class, ManagementFeeResult.class,
      SalesChargeResult.class,
      IncomeForecastResult.class, YieldResult.class,
      CommonPerformanceDatesResult.class, TopCommonHoldingsResult.class,
      CreditQualityResult.class, NumberOfUniqueHoldingsResult.class
  })))
  @PostMapping("/{metricName}")
  public BaseCalculationResult calculate(
      @Parameter(description = "Calculation metric to execute", required = true, schema = @Schema(implementation = CalculationMetric.class)) @PathVariable String metricName,
      @RequestBody @Valid CalculationCommand command) {
    CalculationMetric metric = CalculationMetric.from(metricName);
    if (command.getMetric() != null && command.getMetric() != metric) {
      throw ErrorCode.METRIC_MISMATCH.toException(metricName, command.getMetric().getValue());
    }
    command.setMetric(metric);
    validateCommand(command);
    return calculationObservability.observe(metricName, command,
        () -> calculationOrchestrator.calculate(command));
  }

  @Operation(summary = "Execute several portfolio calculations in one request", description = """
      Performs any combination of calculation metrics in a single request.

      Portfolio holdings, data providers and target currency are declared once at the top level and shared by every
      nested command; a nested command may still override them with its own values. Each command carries its 'metric'
      discriminator plus metric-specific parameters and may appear at most once. The Security Master attributes
      required by the requested metrics are fetched together in as few round trips as possible before the individual
      calculations run. Successful metrics are returned under 'results'; metrics whose calculation failed are returned
      under 'failures' with the corresponding notifications, so one failing metric does not discard the other results.
      """)
  @ApiResponse(responseCode = "200", description = "Per-metric calculation results and failures", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CompositeCalculationResult.class)))
  @PostMapping
  public CompositeCalculationResult calculateComposite(@RequestBody @Valid CompositeCalculationRequest request) {
    List<CalculationCommand> commands = request.getCommands();
    commands.forEach(this::requireMetric);
    requireUniqueMetrics(commands);
    commands.forEach(command -> propagateSharedInputs(request, command));
    commands.forEach(this::validateCommand);
    return calculationObservability.observeComposite(commands,
        () -> calculationOrchestrator.calculateAll(commands));
  }

  @Operation(summary = "List supported calculation metrics", description = """
      Returns all supported portfolio calculation metrics with their identifiers and descriptions.

      Each metric identifier can be used as the metricName path parameter in POST requests or as the
      metric discriminator in composite calculation commands. The descriptions are sourced from the
      OpenAPI schema annotations on the CalculationMetric enum.
      """)
  @ApiResponse(responseCode = "200", description = "List of all supported metrics", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "array", implementation = MetricInfo.class)))
  @GetMapping("/metrics")
  public List<MetricInfo> listMetrics() {
    return Arrays.stream(CalculationMetric.values())
        .map(metric -> MetricInfo.builder()
            .id(metric.getValue())
            .description(getMetricDescription(metric))
            .build())
        .collect(Collectors.toList());
  }

  /**
   * Extracts the description from the @Schema annotation on the CalculationMetric enum constant. Falls back to a
   * generic label if the annotation is not present or description is empty.
   */
  private String getMetricDescription(CalculationMetric metric) {
    try {
      Field field = CalculationMetric.class.getDeclaredField(metric.name());
      Schema schema = field.getAnnotation(Schema.class);
      if (schema != null && !schema.description().isEmpty()) {
        return schema.description();
      }
    } catch (NoSuchFieldException e) {
      log.warn("Could not find field for metric {}", metric.name());
    }
    return metric.getUserFriendlyName();
  }

  private void requireMetric(CalculationCommand command) {
    if (command == null || command.getMetric() == null) {
      throw ErrorCode.METRIC_REQUIRED.toException();
    }
  }

  private void requireUniqueMetrics(List<CalculationCommand> commands) {
    List<String> duplicates = commands.stream()
        .collect(groupingBy(CalculationCommand::getMetric, Collectors.counting()))
        .entrySet().stream()
        .filter(entry -> entry.getValue() > 1)
        .map(entry -> entry.getKey().getValue())
        .toList();
    if (!duplicates.isEmpty()) {
      throw ErrorCode.DUPLICATE_METRIC.toException(String.join(", ", duplicates));
    }
  }

  private void propagateSharedInputs(CompositeCalculationRequest request, CalculationCommand command) {
    if (command instanceof HoldingsProvider holdingsProvider
        && CollectionUtils.isEmpty(holdingsProvider.getHoldings())
        && !CollectionUtils.isEmpty(request.getHoldings())) {
      holdingsProvider.setHoldings(request.getHoldings());
    }
    if (CollectionUtils.isEmpty(command.getDataProviders())
        && !CollectionUtils.isEmpty(request.getDataProviders())) {
      command.setDataProviders(request.getDataProviders());
    }
    if (command instanceof PortfolioBenchmarkCommand benchmarkCommand
        && benchmarkCommand.getCurrency() == null && request.getCurrency() != null) {
      benchmarkCommand.setCurrency(request.getCurrency());
    }
  }

  private void validateCommand(CalculationCommand command) {
    var violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
    validationFacade.validate(command);
  }
}
