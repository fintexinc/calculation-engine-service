package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.aop.annotation.LogRequest;
import com.fintex.ce.adapter.rest.dto.request.AverageMerRequestDTO;
import com.fintex.ce.adapter.rest.dto.request.BestWorstPeriodsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.CorrelationReqDTO;
import com.fintex.ce.adapter.rest.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.IncomeForecastReqDTO;
import com.fintex.ce.adapter.rest.dto.request.LeadingTotalReturnPeriodsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.adapter.rest.dto.request.PeriodsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.ReturnReqDTO;
import com.fintex.ce.adapter.rest.dto.request.RollingCalculationReqDTO;
import com.fintex.ce.adapter.rest.dto.request.RollingCorrelationCalculationReqDTO;
import com.fintex.ce.adapter.rest.dto.request.TopCommonHoldingsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.YieldReqDTO;
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
import com.fintex.ce.adapter.rest.dto.response.distributionofreturns.DistributionOfReturnsResDTO;
import com.fintex.ce.adapter.rest.mapper.RestCommandMapper;
import com.fintex.ce.adapter.rest.service.RestExceptionHandlingServiceImpl;
import com.fintex.ce.adapter.rest.validation.RequestValidationFacade;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegionEmType;
import com.fintex.ce.domain.enumeration.calculation.ClassificationAllocationType;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType;
import com.fintex.ce.domain.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.domain.enumeration.calculation.EquityStyleboxType;
import com.fintex.ce.domain.enumeration.calculation.FixedIncomeSectorType;
import com.fintex.ce.domain.enumeration.calculation.FixedIncomeStyleboxType;
import com.fintex.ce.domain.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.domain.enumeration.calculation.MaturityAllocationType;
import com.fintex.ce.application.result.*;
import com.fintex.ce.application.command.BestWorstPeriodsCommand;
import com.fintex.ce.application.command.DistributionOfReturnsCommand;
import com.fintex.ce.application.command.IncomeForecastCommand;
import com.fintex.ce.application.command.LeadingTotalReturnCommand;
import com.fintex.ce.application.command.ReturnCommand;
import com.fintex.ce.application.command.RollingCalculationCommand;
import com.fintex.ce.application.command.TopCommonHoldingsCommand;
import com.fintex.ce.application.command.YieldCommand;
import com.fintex.ce.application.service.calculation.AverageManagementExpenseCalculationService;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.service.calculation.BreakdownCalculationService;
import com.fintex.ce.service.calculation.CalculationService;
import com.fintex.ce.service.calculation.CommonPerformanceDateService;
import com.fintex.ce.service.calculation.PeriodCalculationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
@LogRequest
public class PortfolioController {

  private final PeriodCalculationService<InformationRatioResult, PeriodCommand> informationRatioCalculationService;
  private final PeriodCalculationService<TrailingTotalReturnsResult, PeriodCommand> trailingTotalReturnCalculation;
  private final PeriodCalculationService<LeadingTotalReturnsResult, LeadingTotalReturnCommand> leadingTotalReturnCalculationService;
  private final PeriodCalculationService<RollingTotalReturnsResult, RollingCalculationCommand> rollingTotalReturnsCalculationService;
  private final PeriodCalculationService<StandardDeviationResult, PeriodCommand> standardDeviationCalculationService;
  private final PeriodCalculationService<MeanResult, PeriodCommand> meanCalculationService;
  private final PeriodCalculationService<SharpeRatioResult, PeriodCommand> sharpeRatioCalculationService;
  private final PeriodCalculationService<UpsideCaptureResult, PeriodCommand> upsideCaptureService;
  private final PeriodCalculationService<ExcessReturnsResult, PeriodCommand> excessReturnsService;
  private final PeriodCalculationService<DownsideCaptureResult, PeriodCommand> downsideCaptureService;
  private final PeriodCalculationService<TrackingErrorResult, PeriodCommand> trackingErrorService;
  private final PeriodCalculationService<MaxDrawdownResult, PeriodCommand> maxDrawdownService;
  private final PeriodCalculationService<DownsideDeviationResult, PeriodCommand> downsideDeviationService;
  private final PeriodCalculationService<SortinoRatioResult, PeriodCommand> sortinoRatioService;
  private final PeriodCalculationService<BetaResult, PeriodCommand> betaCalculationService;
  private final PeriodCalculationService<AlphaResult, PeriodCommand> alphaCalculationService;
  private final PeriodCalculationService<RSquaredResult, PeriodCommand> rSquaredCalculationService;
  private final PeriodCalculationService<CorrelationResult, PeriodCommand> correlationService;
  private final AverageManagementExpenseCalculationService<AverageMerResult> merCalculationService;
  private final AverageManagementExpenseCalculationService<ManagementFeeResult> managementFeeCalculationService;
  private final CommonPerformanceDateService commonPerformanceDateService;
  private final CalculationService<AnnualReturnResult<Integer>, ReturnCommand> annualReturnService;
  private final CalculationService<CreditQualityResult, PortfolioHoldingsCommand> creditQualityService;
  private final BreakdownCalculationService<EquitySectorResult, EquitySectorAllocationType> equitySectorCalculation;
  private final BreakdownCalculationService<EquityCountryExposureResult, CountryRegionType> equityCountryExposureCalculationService;
  private final BreakdownCalculationService<EquityStyleboxExposureResult, EquityStyleboxType> equityStyleboxExposureCalculationService;
  private final CalculationService<IncomeForecastResult, IncomeForecastCommand> incomeForecastCalculationService;
  private final CalculationService<YieldResult, YieldCommand> yieldCalculationService;
  private final BreakdownCalculationService<MaturityAllocationResult, MaturityAllocationType> maturityAllocationCalculationService;
  private final BreakdownCalculationService<FixedIncomeStyleboxExposureResult, FixedIncomeStyleboxType> fixedIncomeStyleboxExposureCalculationService;
  private final BreakdownCalculationService<ClassificationAllocationResult, ClassificationAllocationType> classificationAllocationCalculationService;
  private final BreakdownCalculationService<GeographicExposureResult, GeographicRegionType> equityGeographicExposureCalculationService;
  private final BreakdownCalculationService<CountryExposureResult, CountryRegionType> countryExposureCalculation;
  private final BreakdownCalculationService<GeographicExposureResult, GeographicRegionType> fixedIncomeGeographicExposureCalculation;
  private final BreakdownCalculationService<AssetAllocationResult, AssetAllocationRegion> assetAllocationService;
  private final BreakdownCalculationService<EquityMarketCapResult, EquityMarketCapType> equityMarketCapCalculationService;
  private final BreakdownCalculationService<FixedIncomeSectorResult, FixedIncomeSectorType> fixedIncomeBondSectorCalculationService;
  private final BreakdownCalculationService<AssetAllocationEMResult, AssetAllocationRegionEmType> assetAllocationEMCalculation;
  private final CalculationService<Growth10KResult, ReturnCommand> growthOf10KCalculationService;
  private final CalculationService<BestWorstPeriodsResult, BestWorstPeriodsCommand> bestWorstPeriodsCalculationService;
  private final CalculationService<TopCommonHoldingsResult, TopCommonHoldingsCommand> commonHoldingsService;
  private final PeriodCalculationService<RollingStandardDeviationResult, RollingCalculationCommand> rollingStandardDeviationCalculationService;
  private final PeriodCalculationService<RollingSharpeRatioResult, RollingCalculationCommand> rollingSharpeRatioCalculationService;
  private final CalculationService<DistributionOfReturnsResult, DistributionOfReturnsCommand> distributionOfReturns;
  private final PeriodCalculationService<MARRatioResult, PeriodCommand> marRationService;
  private final PeriodCalculationService<TreynorRatioResult, PeriodCommand> treynorRatioService;
  private final PeriodCalculationService<RollingCorrelationResult, RollingCalculationCommand> rollingCorrelationCalculationService;
  private final CalculationService<SalesChargeResult, PortfolioHoldingsCommand> salesChargeService;
  private final RestExceptionHandlingServiceImpl restExceptionHandler;
  private final RestCommandMapper commandMapper;
  private final RequestValidationFacade validation;

  public PortfolioController(
      PeriodCalculationService<TrailingTotalReturnsResult, PeriodCommand> trailingTotalReturnCalculation,
      PeriodCalculationService<InformationRatioResult, PeriodCommand> informationRatioCalculationService,
      PeriodCalculationService<LeadingTotalReturnsResult, LeadingTotalReturnCommand> leadingTotalReturnsCalculationServiceImpl,
      PeriodCalculationService<RollingTotalReturnsResult, RollingCalculationCommand> rollingTotalReturnsCalculationService,
      PeriodCalculationService<StandardDeviationResult, PeriodCommand> standardDeviationCalculationService,
      PeriodCalculationService<MeanResult, PeriodCommand> meanCalculationService,
      PeriodCalculationService<DownsideDeviationResult, PeriodCommand> downsideDeviationCalculationService,
      PeriodCalculationService<SortinoRatioResult, PeriodCommand> sortinoRatioCalculationService,
      PeriodCalculationService<SharpeRatioResult, PeriodCommand> sharpeRatioCalculationService,
      PeriodCalculationService<CorrelationResult, PeriodCommand> correlationService,
      PeriodCalculationService<MaxDrawdownResult, PeriodCommand> maxDrawdownService,
      PeriodCalculationService<UpsideCaptureResult, PeriodCommand> upsideCaptureService,
      PeriodCalculationService<RSquaredResult, PeriodCommand> rSquaredCalculationService,
      AverageManagementExpenseCalculationService<AverageMerResult> merCalculationService,
      AverageManagementExpenseCalculationService<ManagementFeeResult> managementFeeCalculationService,
      CommonPerformanceDateService commonPerformanceDateService,
      BreakdownCalculationService<EquityCountryExposureResult, CountryRegionType> equityCountryExposureCalculationService,
      @Qualifier("equityGeographicExposureCalculationServiceImpl") BreakdownCalculationService<GeographicExposureResult, GeographicRegionType> equityGeographicExposureCalculationService,
      CalculationService<CreditQualityResult, PortfolioHoldingsCommand> creditQualityService,
      BreakdownCalculationService<AssetAllocationEMResult, AssetAllocationRegionEmType> assetAllocationEMCalculation,
      BreakdownCalculationService<AssetAllocationResult, AssetAllocationRegion> assetAllocationService,
      BreakdownCalculationService<EquitySectorResult, EquitySectorAllocationType> equitySectorCalculation,
      CalculationService<AnnualReturnResult<Integer>, ReturnCommand> annualReturnService,
      BreakdownCalculationService<EquityMarketCapResult, EquityMarketCapType> equityMarketCapCalculationService,
      BreakdownCalculationService<FixedIncomeSectorResult, FixedIncomeSectorType> fixedIncomeBondSectorCalculationService,
      BreakdownCalculationService<CountryExposureResult, CountryRegionType> countryExposureCalculation,
      @Qualifier("fixedIncomeGeographicExposureCalculationImpl") BreakdownCalculationService<GeographicExposureResult, GeographicRegionType> fixedIncomeGeographicExposureCalculation,
      CalculationService<Growth10KResult, ReturnCommand> growthOf10KCalculationService,
      CalculationService<BestWorstPeriodsResult, BestWorstPeriodsCommand> bestWorstPeriodsCalculationService,
      PeriodCalculationService<ExcessReturnsResult, PeriodCommand> excessReturnsService,
      PeriodCalculationService<BetaResult, PeriodCommand> betaCalculationService,
      PeriodCalculationService<DownsideCaptureResult, PeriodCommand> downsideCaptureService,
      PeriodCalculationService<TrackingErrorResult, PeriodCommand> trackingErrorService,
      PeriodCalculationService<AlphaResult, PeriodCommand> alphaCalculationService,
      BreakdownCalculationService<EquityStyleboxExposureResult, EquityStyleboxType> equityStyleboxExposureCalculationService,
      CalculationService<IncomeForecastResult, IncomeForecastCommand> incomeForecastCalculationService,
      CalculationService<YieldResult, YieldCommand> yieldCalculationService,
      BreakdownCalculationService<MaturityAllocationResult, MaturityAllocationType> maturityAllocationCalculationService,
      BreakdownCalculationService<FixedIncomeStyleboxExposureResult, FixedIncomeStyleboxType> fixedIncomeStyleboxExposureCalculationService,
      BreakdownCalculationService<ClassificationAllocationResult, ClassificationAllocationType> classificationAllocationCalculationService,
      CalculationService<TopCommonHoldingsResult, TopCommonHoldingsCommand> commonHoldingsService,
      PeriodCalculationService<RollingStandardDeviationResult, RollingCalculationCommand> rollingStandardDeviationCalculationService,
      PeriodCalculationService<RollingSharpeRatioResult, RollingCalculationCommand> rollingSharpeRatioCalculationService,
      CalculationService<DistributionOfReturnsResult, DistributionOfReturnsCommand> distributionOfReturnsService,
      PeriodCalculationService<MARRatioResult, PeriodCommand> marRatioService,
      PeriodCalculationService<TreynorRatioResult, PeriodCommand> treynorRatioService,
      PeriodCalculationService<RollingCorrelationResult, RollingCalculationCommand> rollingCorrelationCalculationService,
      CalculationService<SalesChargeResult, PortfolioHoldingsCommand> salesChargeService,
      RestExceptionHandlingServiceImpl restExceptionHandler,
      RestCommandMapper commandMapper,
      RequestValidationFacade validation) {
    this.trailingTotalReturnCalculation = trailingTotalReturnCalculation;
    this.leadingTotalReturnCalculationService = leadingTotalReturnsCalculationServiceImpl;
    this.informationRatioCalculationService = informationRatioCalculationService;
    this.rollingTotalReturnsCalculationService = rollingTotalReturnsCalculationService;
    this.upsideCaptureService = upsideCaptureService;
    this.rSquaredCalculationService = rSquaredCalculationService;
    this.merCalculationService = merCalculationService;
    this.managementFeeCalculationService = managementFeeCalculationService;
    this.commonPerformanceDateService = commonPerformanceDateService;
    this.equityCountryExposureCalculationService = equityCountryExposureCalculationService;
    this.creditQualityService = creditQualityService;
    this.assetAllocationEMCalculation = assetAllocationEMCalculation;
    this.assetAllocationService = assetAllocationService;
    this.equityGeographicExposureCalculationService = equityGeographicExposureCalculationService;
    this.countryExposureCalculation = countryExposureCalculation;
    this.equitySectorCalculation = equitySectorCalculation;
    this.annualReturnService = annualReturnService;
    this.equityMarketCapCalculationService = equityMarketCapCalculationService;
    this.growthOf10KCalculationService = growthOf10KCalculationService;
    this.bestWorstPeriodsCalculationService = bestWorstPeriodsCalculationService;
    this.downsideCaptureService = downsideCaptureService;
    this.excessReturnsService = excessReturnsService;
    this.betaCalculationService = betaCalculationService;
    this.trackingErrorService = trackingErrorService;
    this.alphaCalculationService = alphaCalculationService;
    this.standardDeviationCalculationService = standardDeviationCalculationService;
    this.meanCalculationService = meanCalculationService;
    this.maxDrawdownService = maxDrawdownService;
    this.sharpeRatioCalculationService = sharpeRatioCalculationService;
    this.correlationService = correlationService;
    this.fixedIncomeGeographicExposureCalculation = fixedIncomeGeographicExposureCalculation;
    this.equityStyleboxExposureCalculationService = equityStyleboxExposureCalculationService;
    this.incomeForecastCalculationService = incomeForecastCalculationService;
    this.yieldCalculationService = yieldCalculationService;
    this.maturityAllocationCalculationService = maturityAllocationCalculationService;
    this.fixedIncomeStyleboxExposureCalculationService = fixedIncomeStyleboxExposureCalculationService;
    this.classificationAllocationCalculationService = classificationAllocationCalculationService;
    this.commonHoldingsService = commonHoldingsService;
    this.fixedIncomeBondSectorCalculationService = fixedIncomeBondSectorCalculationService;
    this.downsideDeviationService = downsideDeviationCalculationService;
    this.sortinoRatioService = sortinoRatioCalculationService;
    this.rollingStandardDeviationCalculationService = rollingStandardDeviationCalculationService;
    this.rollingSharpeRatioCalculationService = rollingSharpeRatioCalculationService;
    this.distributionOfReturns = distributionOfReturnsService;
    this.marRationService = marRatioService;
    this.treynorRatioService = treynorRatioService;
    this.rollingCorrelationCalculationService = rollingCorrelationCalculationService;
    this.salesChargeService = salesChargeService;
    this.restExceptionHandler = restExceptionHandler;
    this.commandMapper = commandMapper;
    this.validation = validation;
  }

  @PostMapping(value = "/information-ratio/calculation")
  public InformationRatioResDTO getInformationRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateBenchmarkPeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> informationRatioCalculationService.perform(commandMapper.toPeriodCommand(reqDTO)),
        InformationRatioResDTO::new, request);
  }

  @PostMapping(value = "/rolling-total-returns/calculation")
  public RollingTotalReturnsResDTO getRollingReturns(@RequestBody RollingCalculationReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateRollingTotalReturns(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> rollingTotalReturnsCalculationService.perform(commandMapper.toRollingCalculationCommand(reqDTO)),
        RollingTotalReturnsResDTO::new, request);
  }

  @PostMapping(value = "/mer/calculation")
  public AverageMerResponse getAverageManagementExpenseRatio(@RequestBody AverageMerRequestDTO reqDTO,
      HttpServletRequest request) {
    validation.validateAverageMerRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> merCalculationService.perform(commandMapper.toAverageMerCommand(reqDTO)),
        AverageMerResponse::new, request);
  }

  @PostMapping(value = "/management-fee/calculation")
  public ManagementFeeResponse getAverageManagementFee(@RequestBody AverageMerRequestDTO reqDTO,
      HttpServletRequest request) {
    validation.validateAverageMerRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> managementFeeCalculationService.perform(commandMapper.toAverageMerCommand(reqDTO)),
        ManagementFeeResponse::new, request);
  }

  @PostMapping(value = "/common-performance-dates/calculation")
  public CommonPerformanceDatesResDTO getCommonPerformanceDate(@RequestBody MultiplePortfoliosReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateCommonDatesRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> commonPerformanceDateService.commonPerformanceDate(commandMapper.toMultiplePortfoliosCommand(reqDTO)),
        CommonPerformanceDatesResDTO::new, request);
  }

  @PostMapping(value = "/upside-capture/calculation")
  public UpsideCaptureResDTO getUpsideCapture(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateBenchmarkPeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> upsideCaptureService.perform(commandMapper.toPeriodCommand(reqDTO)),
        UpsideCaptureResDTO::new, request);
  }

  @PostMapping(value = "/trailing-total-return/calculation")
  public TrailingTotalReturnsResDTO getTrailingTotalReturns(@RequestBody PeriodsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateTrailingTotalReturn(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> trailingTotalReturnCalculation.perform(commandMapper.toPeriodCommand(reqDTO)),
        TrailingTotalReturnsResDTO::new, request);
  }

  @PostMapping(value = "/leading-total-return/calculation")
  public LeadingTotalReturnsResDTO getLeadingReturns(@RequestBody LeadingTotalReturnPeriodsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateLeadingTotalReturn(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> leadingTotalReturnCalculationService.perform(commandMapper.toLeadingTotalReturnCommand(reqDTO)),
        LeadingTotalReturnsResDTO::new, request);
  }

  @PostMapping(value = "/equity-country-exposure/calculation")
  public EquityCountryExposureResDTO getEquityCountryExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> equityCountryExposureCalculationService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        EquityCountryExposureResDTO::new, request);
  }

  @PostMapping(value = "/equity-stylebox-exposure/calculation")
  public EquityStyleboxExposureResDto getEquityStyleboxExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> equityStyleboxExposureCalculationService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        EquityStyleboxExposureResDto::new, request);
  }

  @PostMapping(value = "/income-forecast/calculation")
  public IncomeForecastResDto getIncomeForecast(@RequestBody IncomeForecastReqDTO reqDTO, HttpServletRequest request) {
    validation.validateIncomeForecast(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> incomeForecastCalculationService.perform(commandMapper.toIncomeForecastCommand(reqDTO)),
        IncomeForecastResDto::new, request);
  }

  @PostMapping(value = "/yield/calculation")
  public YieldResDto getYield(@RequestBody YieldReqDTO reqDTO, HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> yieldCalculationService.perform(commandMapper.toYieldCommand(reqDTO)),
        YieldResDto::new, request);
  }

  @PostMapping(value = "/maturity-allocation/calculation")
  public MaturityAllocationResDto getMaturityAllocation(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> maturityAllocationCalculationService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        MaturityAllocationResDto::new, request);
  }

  @PostMapping(value = "/fixed-income-stylebox-exposure/calculation")
  public FixedIncomeStyleboxExposureResDto getFixedIncomeStyleboxExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> fixedIncomeStyleboxExposureCalculationService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        FixedIncomeStyleboxExposureResDto::new, request);
  }

  @PostMapping(value = "/classification-allocation/calculation")
  public ClassificationAllocationResDto getClassificationAllocation(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateClassificationAllocation(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> classificationAllocationCalculationService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        ClassificationAllocationResDto::new, request);
  }

  @PostMapping(value = "/equity-geographic-exposure/calculation")
  public GeographicExposureResDTO getEquityGeographicExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> equityGeographicExposureCalculationService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        GeographicExposureResDTO::new, request);
  }

  @PostMapping(value = "/asset-allocations/calculation")
  public AssetAllocationResDTO getAssetAllocations(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> assetAllocationService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        AssetAllocationResDTO::new, request);
  }

  @PostMapping(value = "/asset-allocations-em/calculation")
  public AssetAllocationEMResDTO getAssetAllocationsEm(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> assetAllocationEMCalculation.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        AssetAllocationEMResDTO::new, request);
  }

  @PostMapping(value = "/fixed-income-credit-quality/calculation")
  public CreditQualityResDTO getFixedIncomeCreditQuality(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> creditQualityService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        CreditQualityResDTO::new, request);
  }

  @PostMapping(value = "/equity-sector/calculation")
  public EquitySectorResDTO getEquitySector(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> equitySectorCalculation.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        EquitySectorResDTO::new, request);
  }

  @PostMapping(value = "/fixed-income-country-exposure/calculation")
  public CountryExposureResDTO getFixedIncomeCountryExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> countryExposureCalculation.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        CountryExposureResDTO::new, request);
  }

  @PostMapping(value = "/fixed-income-geographic-exposure/calculation")
  public GeographicExposureResDTO getFixedIncomeGeographyExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> fixedIncomeGeographicExposureCalculation.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        GeographicExposureResDTO::new, request);
  }

  @PostMapping(value = "/annual-return/calculation")
  public AnnualReturnResDTO<Integer> getAnnualReturn(@RequestBody ReturnReqDTO reqDTO, HttpServletRequest request) {
    validation.validateReturnRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> annualReturnService.perform(commandMapper.toReturnCommand(reqDTO)),
        AnnualReturnResDTO::new, request);
  }

  @PostMapping(value = "/equity-market-capitalization/calculation")
  public EquityMarketCapResDTO getEquityMarketCapCalculation(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> equityMarketCapCalculationService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        EquityMarketCapResDTO::new, request);
  }

  @PostMapping(value = "/growth-of-10k/calculation")
  public Growth10KResDTO getGrowthOf10KCalculation(@RequestBody ReturnReqDTO reqDTO, HttpServletRequest request) {
    validation.validateReturnRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> growthOf10KCalculationService.perform(commandMapper.toReturnCommand(reqDTO)),
        Growth10KResDTO::new, request);
  }

  @PostMapping(value = "/best-worst-periods/calculation")
  public BestWorstPeriodsResponseDTO getBestWorstPeriodsCalculation(@RequestBody BestWorstPeriodsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateBestWorstPeriods(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> bestWorstPeriodsCalculationService.perform(commandMapper.toBestWorstPeriodsCommand(reqDTO)),
        BestWorstPeriodsResponseDTO::new, request);
  }

  @PostMapping(value = "/excess-returns/calculation")
  public ExcessReturnsResDTO getExcessReturns(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateBenchmarkPeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> excessReturnsService.perform(commandMapper.toPeriodCommand(reqDTO)),
        ExcessReturnsResDTO::new, request);
  }

  @PostMapping(value = "/downside-capture/calculation")
  public DownsideCaptureResDTO getDownsideCapture(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateBenchmarkPeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> downsideCaptureService.perform(commandMapper.toPeriodCommand(reqDTO)),
        DownsideCaptureResDTO::new, request);
  }

  @PostMapping(value = "/beta/calculation")
  public BetaResDTO getBeta(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateBenchmarkPeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> betaCalculationService.perform(commandMapper.toPeriodCommand(reqDTO)),
        BetaResDTO::new, request);
  }

  @PostMapping(value = "/alpha/calculation")
  public AlphaResDTO getAlpha(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateBenchmarkPeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> alphaCalculationService.perform(commandMapper.toPeriodCommand(reqDTO)),
        AlphaResDTO::new, request);
  }

  @PostMapping(value = "/rsquared/calculation")
  public RSquaredResDTO getRSquared(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateBenchmarkPeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> rSquaredCalculationService.perform(commandMapper.toPeriodCommand(reqDTO)),
        RSquaredResDTO::new, request);
  }

  @PostMapping(value = "/standard-deviation/calculation")
  public StandardDeviationResDTO getStandardDeviation(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validatePeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> standardDeviationCalculationService.perform(commandMapper.toPeriodCommand(reqDTO)),
        StandardDeviationResDTO::new, request);
  }

  @PostMapping(value = "/mean/calculation")
  public MeanResDTO getMean(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validatePeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> meanCalculationService.perform(commandMapper.toPeriodCommand(reqDTO)),
        MeanResDTO::new, request);
  }

  @PostMapping(value = "/tracking-error/calculation")
  public TrackingErrorResDTO getTrackingError(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateBenchmarkPeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> trackingErrorService.perform(commandMapper.toPeriodCommand(reqDTO)),
        TrackingErrorResDTO::new, request);
  }

  @PostMapping(value = "/max-drawdown/calculation")
  public MaxDrawdownResDTO getMaxDrawdown(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateMaxDrawdown(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> maxDrawdownService.perform(commandMapper.toPeriodCommand(reqDTO)),
        MaxDrawdownResDTO::new, request);
  }

  @PostMapping(value = "/correlation/calculation")
  public CorrelationResDTO getCorrelation(@RequestBody CorrelationReqDTO reqDTO, HttpServletRequest request) {
    validation.validateCorrelation(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> correlationService.perform(commandMapper.toCorrelationCommand(reqDTO)),
        CorrelationResDTO::new, request);
  }

  @PostMapping(value = "/sharpe-ratio/calculation")
  public SharpeRatioResDTO getSharpeRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validatePeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> sharpeRatioCalculationService.perform(commandMapper.toPeriodCommand(reqDTO)),
        SharpeRatioResDTO::new, request);
  }

  @PostMapping(value = "/fixed-income-bond-sector/calculation")
  public FixedIncomeSectorResDTO getFixedIncomeBondSector(@RequestBody PortfolioHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> fixedIncomeBondSectorCalculationService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        FixedIncomeSectorResDTO::new, request);
  }

  @PostMapping(value = "/downside-deviation/calculation")
  public DownsideDeviationResDTO getDownsideDeviation(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validatePeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> downsideDeviationService.perform(commandMapper.toPeriodCommand(reqDTO)),
        DownsideDeviationResDTO::new, request);
  }

  @PostMapping(value = "/sortino-ratio/calculation")
  public SortinoRatioResDTO getSortinoRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validatePeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> sortinoRatioService.perform(commandMapper.toPeriodCommand(reqDTO)),
        SortinoRatioResDTO::new, request);
  }

  @PostMapping(value = "/top-common-holdings/calculation")
  public TopCommonHoldingsResDTO getTopCommonHoldings(@RequestBody TopCommonHoldingsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateTopCommonHoldings(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> commonHoldingsService.perform(commandMapper.toTopCommonHoldingsCommand(reqDTO)),
        TopCommonHoldingsResDTO::new, request);
  }

  @PostMapping(value = "/rolling-standard-deviation/calculation")
  public RollingStandardDeviationResDTO getRollingStandardDeviation(@RequestBody RollingCalculationReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateRollingCalculation(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> rollingStandardDeviationCalculationService.perform(commandMapper.toRollingCalculationCommand(reqDTO)),
        RollingStandardDeviationResDTO::new, request);
  }

  @PostMapping(value = "/rolling-sharpe-ratio/calculation")
  public RollingSharpeRatioResDTO getRollingSharpeRatio(@RequestBody RollingCalculationReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateRollingCalculation(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> rollingSharpeRatioCalculationService.perform(commandMapper.toRollingCalculationCommand(reqDTO)),
        RollingSharpeRatioResDTO::new, request);
  }

  @PostMapping(value = "/distribution-of-monthly-return/calculation")
  public DistributionOfReturnsResDTO getDistributionOfMonthlyReturn(@RequestBody DistributionOfReturnsReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateDistributionOfReturns(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> distributionOfReturns.perform(commandMapper.toDistributionOfReturnsCommand(reqDTO)),
        DistributionOfReturnsResDTO::new, request);
  }

  @PostMapping(value = "/mar-ratio/calculation")
  public MARRatioResDTO getMarRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateMarRatio(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> marRationService.perform(commandMapper.toPeriodCommand(reqDTO)),
        MARRatioResDTO::new, request);
  }

  @PostMapping(value = "/treynor-ratio/calculation")
  public TreynorRatioResDTO getTreynorRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
    validation.validateBenchmarkPeriodsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> treynorRatioService.perform(commandMapper.toPeriodCommand(reqDTO)),
        TreynorRatioResDTO::new, request);
  }

  @PostMapping(value = "/rolling-correlation/calculation")
  public RollingCorrelationResDTO getRollingCorrelation(@RequestBody RollingCorrelationCalculationReqDTO reqDTO,
      HttpServletRequest request) {
    validation.validateRollingCorrelation(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> rollingCorrelationCalculationService.perform(commandMapper.toRollingCorrelationCommand(reqDTO)),
        RollingCorrelationResDTO::new, request);
  }

  @PostMapping(value = "/sales-charge/calculation")
  public SalesChargeResDtos getSalesCharge(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
    validation.validatePortfolioHoldingsRequest(reqDTO);
    return restExceptionHandler.handleWithResultMapping(
        () -> salesChargeService.perform(commandMapper.toPortfolioHoldingsCommand(reqDTO)),
        SalesChargeResDtos::new, request);
  }
}