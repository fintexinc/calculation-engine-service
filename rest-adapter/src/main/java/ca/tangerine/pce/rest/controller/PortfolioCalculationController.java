package ca.tangerine.pce.rest.controller;

import ca.tangerine.pce.calculation.CalculationOrchestrator;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.result.CommonPerformanceDatesResult;
import ca.tangerine.pce.model.domain.result.allocation.AssetAllocationEMResult;
import ca.tangerine.pce.model.domain.result.allocation.AssetAllocationResult;
import ca.tangerine.pce.model.domain.result.allocation.ConsolidatedSectorExposureResult;
import ca.tangerine.pce.model.domain.result.allocation.EquitySectorResult;
import ca.tangerine.pce.model.domain.result.allocation.FixedIncomeSectorResult;
import ca.tangerine.pce.model.domain.result.composite.CompositeCalculationResult;
import ca.tangerine.pce.model.domain.result.exposure.CountryExposureResult;
import ca.tangerine.pce.model.domain.result.exposure.EquityCountryExposureResult;
import ca.tangerine.pce.model.domain.result.exposure.GeographicExposureResult;
import ca.tangerine.pce.model.domain.result.fee.AverageMerResult;
import ca.tangerine.pce.model.domain.result.fee.FeesResult;
import ca.tangerine.pce.model.domain.result.fee.ManagementFeeResult;
import ca.tangerine.pce.model.domain.result.fee.MerComparisonResult;
import ca.tangerine.pce.model.domain.result.holding.NumberOfUniqueHoldingsResult;
import ca.tangerine.pce.model.domain.result.holding.TopCommonHoldingsResult;
import ca.tangerine.pce.model.domain.result.returns.AnnualReturnResult;
import ca.tangerine.pce.model.domain.result.returns.Growth10KResult;
import ca.tangerine.pce.model.domain.result.returns.TrailingTotalReturnsResult;
import ca.tangerine.pce.model.domain.result.risk.MaxDrawdownResult;
import ca.tangerine.pce.model.domain.result.risk.SharpeRatioResult;
import ca.tangerine.pce.model.domain.result.risk.StandardDeviationResult;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.CompositeCalculationRequest;
import ca.tangerine.pce.model.dto.command.PortfolioBenchmarkCommand;
import ca.tangerine.pce.model.dto.command.contract.HoldingsProvider;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.port.observability.CalculationObservability;
import ca.tangerine.pce.rest.validation.RequestValidationFacade;

import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
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

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.groupingBy;

/**
 * REST entry point for portfolio calculations. The controller owns the request lifecycle — metric resolution, request
 * validation and observability — and delegates the actual calculation, including Market Investment Catalogue data
 * fetching, to the {@link CalculationOrchestrator}. For composite requests the shared top-level holdings, data
 * providers and currency are propagated into every nested command that does not carry its own value, so the per-command
 * validation chain keeps working unchanged.
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
      TrailingTotalReturnsResult.class, AnnualReturnResult.class, Growth10KResult.class,
      StandardDeviationResult.class, SharpeRatioResult.class, MaxDrawdownResult.class, AssetAllocationResult.class,
      AssetAllocationEMResult.class, EquitySectorResult.class, ConsolidatedSectorExposureResult.class,
      EquityCountryExposureResult.class, GeographicExposureResult.class, CountryExposureResult.class,
      FixedIncomeSectorResult.class, AverageMerResult.class, ManagementFeeResult.class, FeesResult.class,
      MerComparisonResult.class, CommonPerformanceDatesResult.class, TopCommonHoldingsResult.class,
      NumberOfUniqueHoldingsResult.class
  })))
  @PostMapping("/{metric-name}")
  public BaseCalculationResult calculate(
      @Parameter(description = "Calculation metric to execute", required = true, schema = @Schema(implementation = CalculationMetric.class)) @PathVariable("metric-name") String metricName,
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
      discriminator plus metric-specific parameters and may appear at most once. The Market Investment Catalogue attributes
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
