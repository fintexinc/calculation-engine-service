package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.dto.CommonPerformanceDatesResDTO;
import com.fintex.ce.adapter.rest.dto.WarningDTO;
import com.fintex.ce.adapter.rest.dto.allocation.AssetAllocationEMResDTO;
import com.fintex.ce.adapter.rest.dto.allocation.AssetAllocationResDTO;
import com.fintex.ce.adapter.rest.dto.allocation.ClassificationAllocationResDto;
import com.fintex.ce.adapter.rest.dto.allocation.CreditQualityResDTO;
import com.fintex.ce.adapter.rest.dto.allocation.EquityMarketCapResDTO;
import com.fintex.ce.adapter.rest.dto.allocation.EquitySectorResDTO;
import com.fintex.ce.adapter.rest.dto.allocation.FixedIncomeSectorResDTO;
import com.fintex.ce.adapter.rest.dto.allocation.MaturityAllocationResDto;
import com.fintex.ce.adapter.rest.dto.correlation.CorrelationResDTO;
import com.fintex.ce.adapter.rest.dto.distribution.DistributionOfReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.exposure.CountryExposureResDTO;
import com.fintex.ce.adapter.rest.dto.exposure.EquityCountryExposureResDTO;
import com.fintex.ce.adapter.rest.dto.exposure.EquityStyleboxExposureResDto;
import com.fintex.ce.adapter.rest.dto.exposure.FixedIncomeStyleboxExposureResDto;
import com.fintex.ce.adapter.rest.dto.exposure.GeographicExposureResDTO;
import com.fintex.ce.adapter.rest.dto.fee.AverageMerResponse;
import com.fintex.ce.adapter.rest.dto.fee.ManagementFeeResponse;
import com.fintex.ce.adapter.rest.dto.fee.SalesChargeResDtos;
import com.fintex.ce.adapter.rest.dto.holding.TopCommonHoldingsResDTO;
import com.fintex.ce.adapter.rest.dto.income.IncomeForecastResDto;
import com.fintex.ce.adapter.rest.dto.income.YieldResDto;
import com.fintex.ce.adapter.rest.dto.period.BestWorstPeriodsResponseDTO;
import com.fintex.ce.adapter.rest.dto.returns.AnnualReturnResDTO;
import com.fintex.ce.adapter.rest.dto.returns.ExcessReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.returns.Growth10KResDTO;
import com.fintex.ce.adapter.rest.dto.returns.LeadingTotalReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.returns.MeanResDTO;
import com.fintex.ce.adapter.rest.dto.returns.TrailingTotalReturnsResDTO;
import com.fintex.ce.adapter.rest.dto.risk.AlphaResDTO;
import com.fintex.ce.adapter.rest.dto.risk.BetaResDTO;
import com.fintex.ce.adapter.rest.dto.risk.DownsideCaptureResDTO;
import com.fintex.ce.adapter.rest.dto.risk.DownsideDeviationResDTO;
import com.fintex.ce.adapter.rest.dto.risk.InformationRatioResDTO;
import com.fintex.ce.adapter.rest.dto.risk.MARRatioResDTO;
import com.fintex.ce.adapter.rest.dto.risk.MaxDrawdownResDTO;
import com.fintex.ce.adapter.rest.dto.risk.RSquaredResDTO;
import com.fintex.ce.adapter.rest.dto.risk.SharpeRatioResDTO;
import com.fintex.ce.adapter.rest.dto.risk.SortinoRatioResDTO;
import com.fintex.ce.adapter.rest.dto.risk.StandardDeviationResDTO;
import com.fintex.ce.adapter.rest.dto.risk.TrackingErrorResDTO;
import com.fintex.ce.adapter.rest.dto.risk.TreynorRatioResDTO;
import com.fintex.ce.adapter.rest.dto.risk.UpsideCaptureResDTO;
import com.fintex.ce.adapter.rest.dto.rolling.RollingCorrelationResDTO;
import com.fintex.ce.adapter.rest.dto.rolling.RollingSharpeRatioResDTO;
import com.fintex.ce.adapter.rest.dto.rolling.RollingStandardDeviationResDTO;
import com.fintex.ce.adapter.rest.dto.rolling.RollingTotalReturnsResDTO;
import com.fintex.ce.adapter.rest.util.ResponseMappingUtils;
import com.fintex.ce.adapter.rest.util.ResultCopyUtils;
import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.ErrorCode;

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

  public PortfolioCalculationController(
      List<CalculationService<?, ?>> calculationServices,
      RequestValidationFacade validationFacade) {
    this.serviceMap = calculationServices.stream()
        .collect(Collectors.toMap(CalculationService::getMetric, Function.identity(),
            (existing, duplicate) -> {
              throw ErrorCode.INTERNAL_SERVER_ERROR.toException();
            }));
    this.validationFacade = validationFacade;
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
  public WarningDTO calculate(
      @Parameter(description = "Calculation metric to execute", required = true, schema = @Schema(implementation = CalculationMetric.class)) @PathVariable String metricName,
      @RequestBody @Valid CalculationCommand command) {
    CalculationMetric metric = CalculationMetric.from(metricName);
    if (!serviceMap.containsKey(metric)) {
      throw ErrorCode.UNSUPPORTED_METRIC.toException(metricName);
    }

    if (command.getMetric() != null && command.getMetric() != metric) {
      throw ErrorCode.METRIC_MISMATCH.toException(metricName, command.getMetric().getValue());
    }

    validationFacade.validate(command, metric);

    CalculationService<?, ?> service = serviceMap.get(metric);
    Object result = ((CalculationService<?, CalculationCommand>) service).perform(command);
    WarningDTO response = ResponseMappingUtils.getResponseFactory(metric).get();
    ResultCopyUtils.copyProperties(result, response);
    return response;
  }
}
