package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.CommonPerformanceDatesResult;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationEMResult;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.ce.model.domain.result.allocation.ClassificationAllocationResult;
import com.fintex.ce.model.domain.result.allocation.CreditQualityResult;
import com.fintex.ce.model.domain.result.allocation.EquityMarketCapResult;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.domain.result.allocation.MaturityAllocationResult;
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
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.Observation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(PortfolioCalculationController.BASE_PATH)
@Tag(name = "Portfolio Calculations", description = "Unified endpoint for all portfolio calculation metrics: "
    + "returns, risk, benchmark comparison, allocations, fees, and income")
public class PortfolioCalculationController {
  public static final String BASE_PATH = "/api/v1/portfolio/calculations";

  private final Map<CalculationMetric, CalculationService<?, ?>> serviceMap;
  private final RequestValidationFacade validationFacade;
  private final CalculationObservability calculationObservability;

  public PortfolioCalculationController(
      List<CalculationService<?, ?>> calculationServices,
      RequestValidationFacade validationFacade,
      CalculationObservability calculationObservability) {
    this.serviceMap = calculationServices.stream()
        .collect(Collectors.toMap(CalculationService::getMetric, Function.identity(),
            (existing, duplicate) -> {
              throw ErrorCode.INTERNAL_SERVER_ERROR.toException();
            }));
    this.validationFacade = validationFacade;
    this.calculationObservability = calculationObservability;
  }

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
      EquitySectorResult.class, EquityCountryExposureResult.class,
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
  @SuppressWarnings("unchecked")
  public BaseCalculationResult calculate(
      @Parameter(description = "Calculation metric to execute", required = true, schema = @Schema(implementation = CalculationMetric.class)) @PathVariable String metricName,
      @RequestBody @Valid CalculationCommand command) {
    return calculationObservability.observe(metricName, command, observation -> {
      CalculationMetric metric = CalculationMetric.from(metricName);

      if (!serviceMap.containsKey(metric)) {
        throw ErrorCode.UNSUPPORTED_METRIC.toException(metricName);
      }

      if (command.getMetric() != null && command.getMetric() != metric) {
        throw ErrorCode.METRIC_MISMATCH.toException(metricName, command.getMetric().getValue());
      }

      observation.event(Observation.Event.of(CalculationObservability.VALIDATION_STARTED_EVENT));
      validationFacade.validate(command, metric);
      observation.event(Observation.Event.of(CalculationObservability.VALIDATION_COMPLETED_EVENT));

      CalculationService<?, ?> service = serviceMap.get(metric);
      observation.event(Observation.Event.of(CalculationObservability.SERVICE_STARTED_EVENT));
      BaseCalculationResult result = ((CalculationService<CalculationCommand, ?>) service).perform(command);
      observation.event(Observation.Event.of(CalculationObservability.SERVICE_COMPLETED_EVENT));
      return result;
    });
  }
}
