package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.dto.response.AlphaResDTO;
import com.fintex.ce.adapter.rest.dto.response.AnnualReturnResDTO;
import com.fintex.ce.adapter.rest.dto.response.AssetAllocationEMResDTO;
import com.fintex.ce.adapter.rest.dto.response.AssetAllocationResDTO;
import com.fintex.ce.adapter.rest.dto.response.AverageMerResponse;
import com.fintex.ce.adapter.rest.dto.response.BestWorstPeriodsResponseDTO;
import com.fintex.ce.adapter.rest.dto.response.BetaResDTO;
import com.fintex.ce.adapter.rest.dto.response.ClassificationAllocationResDto;
import com.fintex.ce.adapter.rest.dto.response.CommonPerformanceDatesResDTO;
import com.fintex.ce.adapter.rest.dto.response.CorrelationResDTO;
import com.fintex.ce.adapter.rest.dto.response.CountryExposureResDTO;
import com.fintex.ce.adapter.rest.dto.response.CreditQualityResDTO;
import com.fintex.ce.adapter.rest.dto.response.DownsideCaptureResDTO;
import com.fintex.ce.adapter.rest.dto.response.DownsideDeviationResDTO;
import com.fintex.ce.adapter.rest.dto.response.EquityCountryExposureResDTO;
import com.fintex.ce.adapter.rest.dto.response.EquityMarketCapResDTO;
import com.fintex.ce.adapter.rest.dto.response.EquitySectorResDTO;
import com.fintex.ce.adapter.rest.dto.response.EquityStyleboxExposureResDto;
import com.fintex.ce.adapter.rest.dto.response.ExcessReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.response.FixedIncomeSectorResDTO;
import com.fintex.ce.adapter.rest.dto.response.FixedIncomeStyleboxExposureResDto;
import com.fintex.ce.adapter.rest.dto.response.GeographicExposureResDTO;
import com.fintex.ce.adapter.rest.dto.response.Growth10KResDTO;
import com.fintex.ce.adapter.rest.dto.response.IncomeForecastResDto;
import com.fintex.ce.adapter.rest.dto.response.InformationRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.LeadingTotalReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.response.MARRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.ManagementFeeResponse;
import com.fintex.ce.adapter.rest.dto.response.MaturityAllocationResDto;
import com.fintex.ce.adapter.rest.dto.response.MaxDrawdownResDTO;
import com.fintex.ce.adapter.rest.dto.response.MeanResDTO;
import com.fintex.ce.adapter.rest.dto.response.RSquaredResDTO;
import com.fintex.ce.adapter.rest.dto.response.RollingCorrelationResDTO;
import com.fintex.ce.adapter.rest.dto.response.RollingSharpeRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.RollingStandardDeviationResDTO;
import com.fintex.ce.adapter.rest.dto.response.RollingTotalReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.response.SalesChargeResDtos;
import com.fintex.ce.adapter.rest.dto.response.SharpeRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.SortinoRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.StandardDeviationResDTO;
import com.fintex.ce.adapter.rest.dto.response.TopCommonHoldingsResDTO;
import com.fintex.ce.adapter.rest.dto.response.TrackingErrorResDTO;
import com.fintex.ce.adapter.rest.dto.response.TrailingTotalReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.response.TreynorRatioResDTO;
import com.fintex.ce.adapter.rest.dto.response.UpsideCaptureResDTO;
import com.fintex.ce.adapter.rest.dto.response.YieldResDto;
import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;
import com.fintex.ce.adapter.rest.dto.response.distributionofreturns.DistributionOfReturnsResDTO;
import com.fintex.ce.adapter.rest.service.RestExceptionHandlingServiceImpl;
import com.fintex.ce.adapter.rest.util.ResponseMappingUtils;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import org.springframework.http.MediaType;
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
  private final RestExceptionHandlingServiceImpl restExceptionHandler;

  public PortfolioCalculationController(
      List<CalculationService<?, ?>> calculationServices,
      RestExceptionHandlingServiceImpl restExceptionHandler) {
    this.serviceMap = calculationServices.stream()
        .collect(Collectors.toMap(CalculationService::getMetric, Function.identity(),
            (existing, duplicate) -> {
              throw new IllegalStateException(
                  "Duplicate CalculationService registered for metric: " + existing.getMetric());
            }));
    this.restExceptionHandler = restExceptionHandler;
  }

  @Operation(summary = "Execute a portfolio calculation", description = "Performs the specified calculation metric on the provided portfolio holdings. "
      + "The request body schema depends on the metric — period-based metrics require time intervals, "
      + "breakdown metrics require holdings, and fee metrics require parameter types.")
  @ApiResponse(responseCode = "200", description = "Calculation result", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(oneOf = {
      TrailingTotalReturnsResDTO.class, LeadingTotalReturnsResDTO.class,
      RollingTotalReturnsResDTO.class, ExcessReturnsResDTO.class,
      AnnualReturnResDTO.class, Growth10KResDTO.class,
      BestWorstPeriodsResponseDTO.class, DistributionOfReturnsResDTO.class,
      StandardDeviationResDTO.class, RollingStandardDeviationResDTO.class,
      MeanResDTO.class, SharpeRatioResDTO.class,
      RollingSharpeRatioResDTO.class, SortinoRatioResDTO.class,
      MaxDrawdownResDTO.class, DownsideDeviationResDTO.class,
      MARRatioResDTO.class, TreynorRatioResDTO.class,
      InformationRatioResDTO.class, TrackingErrorResDTO.class,
      AlphaResDTO.class, BetaResDTO.class,
      RSquaredResDTO.class, CorrelationResDTO.class,
      RollingCorrelationResDTO.class, UpsideCaptureResDTO.class,
      DownsideCaptureResDTO.class,
      AssetAllocationResDTO.class, AssetAllocationEMResDTO.class,
      EquitySectorResDTO.class, EquityCountryExposureResDTO.class,
      EquityStyleboxExposureResDto.class, GeographicExposureResDTO.class,
      EquityMarketCapResDTO.class, CountryExposureResDTO.class,
      FixedIncomeSectorResDTO.class, FixedIncomeStyleboxExposureResDto.class,
      MaturityAllocationResDto.class, ClassificationAllocationResDto.class,
      AverageMerResponse.class, ManagementFeeResponse.class,
      SalesChargeResDtos.class,
      IncomeForecastResDto.class, YieldResDto.class,
      CommonPerformanceDatesResDTO.class, TopCommonHoldingsResDTO.class,
      CreditQualityResDTO.class
  })))
  @PostMapping("/{metricName}")
  @SuppressWarnings("unchecked")
  public ErrorDTO calculate(
      @Parameter(description = "Calculation metric to execute", required = true, schema = @Schema(implementation = CalculationMetric.class)) @PathVariable String metricName,
      @RequestBody CalculationCommand command) {
    CalculationMetric metric = CalculationMetric.from(metricName);
    if (!serviceMap.containsKey(metric)) {
      throw new IllegalArgumentException("Metric " + metricName + " is not supported");
    }

    if (command.getMetric() != null && command.getMetric() != metric) {
      throw new IllegalArgumentException(
          "Metric mismatch: path parameter is '" + metricName + "' but request body contains '" +
              command.getMetric().getValue() + "'");
    }

    // TODO refactor the validators at TMI-315
    CalculationService<?, ?> service = serviceMap.get(metric);
    return restExceptionHandler.handleWithResultMapping(
        () -> ((CalculationService<?, CalculationCommand>) service).perform(command),
        ResponseMappingUtils.getResponseFactory(metric));
  }
}
