package com.fintex.ce.rest;

import com.fintex.ce.aop.annotation.LogRequest;
import com.fintex.ce.config.enumeration.calculation.*;
import com.fintex.ce.dto.RSquaredResDTO;
import com.fintex.ce.dto.request.*;
import com.fintex.ce.dto.response.*;
import com.fintex.ce.dto.response.distributionofreturns.DistributionOfReturnsResDTO;
import com.fintex.ce.service.impl.calculation.*;
import com.fintex.ce.service.impl.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.service.impl.calculation.period.AlphaCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.BetaCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.CorrelationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.DistributionOfReturnsServiceImpl;
import com.fintex.ce.service.impl.calculation.period.DownsideCaptureCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.DownsideDeviationCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.ExcessReturnsCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.InformationRatioCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.LeadingTotalReturnsCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.MarRatioCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.MaxDrawdownServiceImpl;
import com.fintex.ce.service.impl.calculation.period.MeanCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.RollingCorrelationCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.RollingSharpeRatioCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.RollingStandardDeviationCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.RollingTotalReturnsCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.SharpeRatioCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.SortinoRatioCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.StandardDeviationCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.TrackingErrorCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.TrailingTotalReturnsCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.TreynorRatioServiceImpl;
import com.fintex.ce.service.impl.calculation.period.UpsideCaptureCalculationServiceImpl;
import com.fintex.ce.service.impl.calculation.period.core.PeriodAbstractService;
import com.fintex.ce.service.impl.calculation.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.service.interfaces.ExceptionHandlingService;
import com.fintex.ce.service.interfaces.calculation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
@LogRequest
public class PortfolioController {

    private final PeriodAbstractService<InformationRatioResDTO, PeriodsReqDTO> informationRatioCalculationService;
    private final PeriodAbstractService<TrailingTotalReturnsResDTO, PeriodsReqDTO> trailingTotalReturnCalculation;
    private final PeriodAbstractService<LeadingTotalReturnsResDTO, LeadingTotalReturnPeriodsReqDTO> leadingTotalReturnCalculationService;
    private final PeriodAbstractService<RollingTotalReturnsResDTO, RollingCalculationReqDTO> rollingTotalReturnsCalculationService;
    private final PeriodAbstractService<StandardDeviationResDTO, PeriodsReqDTO> standardDeviationCalculationService;
    private final PeriodAbstractService<MeanResDTO, PeriodsReqDTO> meanCalculationService;
    private final PeriodAbstractService<SharpeRatioResDTO, PeriodsReqDTO> sharpeRatioCalculationService;
    private final PeriodAbstractService<UpsideCaptureResDTO, PeriodsReqDTO> upsideCaptureService;
    private final PeriodAbstractService<ExcessReturnsResDTO, PeriodsReqDTO> excessReturnsService;
    private final PeriodAbstractService<DownsideCaptureResDTO, PeriodsReqDTO> downsideCaptureService;
    private final PeriodAbstractService<TrackingErrorResDTO, PeriodsReqDTO> trackingErrorService;
    private final PeriodAbstractService<MaxDrawdownResDTO, PeriodsReqDTO> maxDrawdownService;
    private final PeriodAbstractService<DownsideDeviationResDTO, PeriodsReqDTO> downsideDeviationService;
    private final PeriodAbstractService<SortinoRatioResDTO, PeriodsReqDTO> sortinoRatioService;
    private final PeriodAbstractService<BetaResDTO, PeriodsReqDTO> betaCalculationService;
    private final PeriodAbstractService<AlphaResDTO, PeriodsReqDTO> alphaCalculationService;
    private final PeriodAbstractService<RSquaredResDTO,PeriodsReqDTO> rSquaredCalculationService;
    private final PeriodAbstractService<CorrelationResDTO, PeriodsReqDTO> correlationService;
    private final AverageManagementExpenseCalculationService<AverageMerResponse> merCalculationService;
    private final AverageManagementExpenseCalculationService<ManagementFeeResponse> managementFeeCalculationService;
    private final CommonPerformanceDateService commonPerformanceDateService;
    private final AnnualReturnService annualReturnService;
    private final CreditQualityService creditQualityService;
    private final BreakdownAbstractService<EquitySectorResDTO, EquitySectorAllocationType> equitySectorCalculation;
    private final BreakdownAbstractService<EquityCountryExposureResDTO, CountryRegionType> equityCountryExposureCalculationService;
    private final BreakdownAbstractService<EquityStyleboxExposureResDto, EquityStyleboxType> equityStyleboxExposureCalculationService;
    private final IncomeForecastService incomeForecastCalculationService;
    private final YieldService yieldCalculationService;
    private final BreakdownAbstractService<MaturityAllocationResDto, MaturityAllocationType> maturityAllocationCalculationService;
    private final BreakdownAbstractService<FixedIncomeStyleboxExposureResDto, FixedIncomeStyleboxType> fixedIncomeStyleboxExposureCalculationService;
    private final BreakdownAbstractService<ClassificationAllocationResDto, ClassificationAllocationType> classificationAllocationCalculationService;
    private final BreakdownAbstractService<GeographicExposureResDTO, GeographicRegionType> equityGeographicExposureCalculationService;
    private final BreakdownAbstractService<CountryExposureResDTO, CountryRegionType> countryExposureCalculation;
    private final BreakdownAbstractService<GeographicExposureResDTO, GeographicRegionType> fixedIncomeGeographicExposureCalculation;
    private final BreakdownAbstractService<AssetAllocationResDTO, AssetAllocationRegion> assetAllocationService;
    private final BreakdownAbstractService<EquityMarketCapResDTO, EquityMarketCapType> equityMarketCapCalculationService;
    private final BreakdownAbstractService<FixedIncomeSectorResDTO, FixedIncomeSectorType> fixedIncomeBondSectorCalculationService;
    private final BreakdownAbstractService<AssetAllocationEMResDTO, AssetAllocationRegionEmType> assetAllocationEMCalculation;
    private final GrowthOf10KCalculationService growthOf10KCalculationService;
    private final BestWorstPeriodsCalculationService bestWorstPeriodsCalculationService;
    private final CommonHoldingsService commonHoldingsService;
    private final PeriodAbstractService<RollingStandardDeviationResDTO, RollingCalculationReqDTO> rollingStandardDeviationCalculationService;
    private final PeriodAbstractService<RollingSharpeRatioResDTO, RollingCalculationReqDTO> rollingSharpeRatioCalculationService;
    private final DistributionOfReturnsService distributionOfReturns;
    private final PeriodAbstractService<MARRatioResDTO, PeriodsReqDTO> marRationService;
    private final PeriodAbstractService<TreynorRatioResDTO, PeriodsReqDTO> treynorRatioService;
    private final PeriodBenchmarkAbstractService<RollingCorrelationResDTO, RollingCalculationReqDTO> rollingCorrelationCalculationService;
    private final SalesChargeService salesChargeService;
    private final DailyPerformanceCalculationServiceImpl dailyPerformanceCalculationService;
    private final DistributionServiceImpl distributionService;
    private final ExceptionHandlingService exceptionHandlingService;
    //private final InflationServiceImpl inflationService;

    public PortfolioController(TrailingTotalReturnsCalculationServiceImpl trailingTotalReturnCalculation,
                               InformationRatioCalculationServiceImpl informationRatioCalculationService,
                               LeadingTotalReturnsCalculationServiceImpl leadingTotalReturnsCalculationServiceImpl,
                               RollingTotalReturnsCalculationServiceImpl rollingTotalReturnsCalculationService,
                               StandardDeviationCalculationServiceImpl standardDeviationCalculationService,
                               MeanCalculationServiceImpl meanCalculationService,
                               DownsideDeviationCalculationServiceImpl downsideDeviationCalculationService,
                               SortinoRatioCalculationServiceImpl sortinoRatioCalculationService,
                               SharpeRatioCalculationServiceImpl sharpeRatioCalculationService,
                               CorrelationServiceImpl correlationService,
                               MaxDrawdownServiceImpl maxDrawdownService,
                               UpsideCaptureCalculationServiceImpl upsideCaptureService,
                               PeriodAbstractService<RSquaredResDTO, PeriodsReqDTO> rSquaredCalculationService, MERCalculationServiceImpl merCalculationService,
                               ManagementFeeCalculationServiceImpl managementFeeCalculationService,
                               CommonPerformanceDateService commonPerformanceDateService,
                               EquityCountryExposureCalculationServiceImpl equityCountryExposureCalculationService,
                               EquityGeographicExposureCalculationServiceImpl equityGeographicExposureCalculationService,
                               CreditQualityService creditQualityService,
                               AssetAllocationEMServiceImpl assetAllocationEMCalculation,
                               AssetAllocationServiceImpl assetAllocationService,
                               EquitySectorCalculationImpl equitySectorCalculation,
                               AnnualReturnService annualReturnService,
                               EquityMarketCapCalculationServiceImpl equityMarketCapCalculationService,
                               FixedIncomeBondSectorCalculationServiceImpl fixedIncomeBondSectorCalculationService,
                               CountryExposureCalculationImpl countryExposureCalculation,
                               FixedIncomeGeographicExposureCalculationImpl fixedIncomeGeographicExposureCalculation,
                               GrowthOf10KCalculationService growthOf10KCalculationService,
                               BestWorstPeriodsCalculationService bestWorstPeriodsCalculationService,
                               ExcessReturnsCalculationServiceImpl excessReturnsService,
                               BetaCalculationServiceImpl betaCalculationService,
                               DownsideCaptureCalculationServiceImpl downsideCaptureService,
                               TrackingErrorCalculationServiceImpl trackingErrorService,
                               AlphaCalculationServiceImpl alphaCalculationService,
                               EquityStyleboxExposureCalculationServiceImpl equityStyleboxExposureCalculationService,
                               IncomeForecastCalculationServiceImpl incomeForecastCalculationService,
                               YieldCalculationServiceImpl yieldCalculationService,
                               MaturityAllocationCalculationServiceImpl maturityAllocationCalculationService,
                               FixedIncomeStyleboxExposureCalculationServiceImpl fixedIncomeStyleboxExposureCalculationService,
                               ClassificationAllocationCalculationServiceImpl classificationAllocationCalculationService,
                               CommonHoldingsServiceImpl commonHoldingsService,
                               RollingStandardDeviationCalculationServiceImpl rollingStandardDeviationCalculationService,
                               RollingSharpeRatioCalculationServiceImpl rollingSharpeRatioCalculationService,
                               DistributionOfReturnsServiceImpl distributionOfReturnsService,
                               MarRatioCalculationServiceImpl marRatioService,
                               TreynorRatioServiceImpl treynorRatioService,
                               RollingCorrelationCalculationServiceImpl rollingCorrelationCalculationService,
                               SalesChargeServiceImpl salesChargeService,
                               DailyPerformanceCalculationServiceImpl dailyPerformanceCalculationService,
                               DistributionServiceImpl distributionService,
                               ExceptionHandlingService exceptionHandlingService
                               //InflationServiceImpl inflationService
    ) {
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
        this.dailyPerformanceCalculationService = dailyPerformanceCalculationService;
        this.distributionService = distributionService;
        this.exceptionHandlingService = exceptionHandlingService;
        //this.inflationService = inflationService;
    }

    @PostMapping(value = "/information-ratio/calculation")
    public InformationRatioResDTO getInformationRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> informationRatioCalculationService.perform(reqDTO), InformationRatioResDTO::new, request);
    }

    @PostMapping(value = "/rolling-total-returns/calculation")
    public RollingTotalReturnsResDTO getRollingReturns(@RequestBody RollingCalculationReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> rollingTotalReturnsCalculationService.perform(reqDTO), RollingTotalReturnsResDTO::new, request);
    }

    @PostMapping(value = "/mer/calculation")
    public AverageMerResponse getAverageManagementExpenseRatio(@RequestBody AverageMerRequestDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> merCalculationService.perform(reqDTO), AverageMerResponse::new, request);
    }

    @PostMapping(value = "/management-fee/calculation")
    public ManagementFeeResponse getAverageManagementFee(@RequestBody AverageMerRequestDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> managementFeeCalculationService.perform(reqDTO), ManagementFeeResponse::new, request);
    }

    @PostMapping(value = "/common-performance-dates/calculation")
    public CommonPerformanceDatesResDTO getCommonPerformanceDate(@RequestBody MultiplePortfoliosReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> commonPerformanceDateService.commonPerformanceDate(reqDTO), CommonPerformanceDatesResDTO::new, request);
    }

    @PostMapping(value = "/upside-capture/calculation")
    public UpsideCaptureResDTO getUpsideCapture(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> upsideCaptureService.perform(reqDTO), UpsideCaptureResDTO::new, request);
    }

    @PostMapping(value = "/trailing-total-return/calculation")
    public TrailingTotalReturnsResDTO getTrailingTotalReturns(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> trailingTotalReturnCalculation.perform(reqDTO), TrailingTotalReturnsResDTO::new, request);
    }

    @PostMapping(value = "/leading-total-return/calculation")
    public LeadingTotalReturnsResDTO getLeadingReturns(@RequestBody LeadingTotalReturnPeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> leadingTotalReturnCalculationService.perform(reqDTO), LeadingTotalReturnsResDTO::new, request);
    }

    @PostMapping(value = "/equity-country-exposure/calculation")
    public EquityCountryExposureResDTO getEquityCountryExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> equityCountryExposureCalculationService.perform(reqDTO), EquityCountryExposureResDTO::new, request);
    }

    @PostMapping(value = "/equity-stylebox-exposure/calculation")
    public EquityStyleboxExposureResDto getEquityStyleboxExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> equityStyleboxExposureCalculationService.perform(reqDTO), EquityStyleboxExposureResDto::new, request);
    }

    @PostMapping(value = "/income-forecast/calculation")
    public IncomeForecastResDto getIncomeForecast(@RequestBody IncomeForecastReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> incomeForecastCalculationService.perform(reqDTO), IncomeForecastResDto::new, request);
    }

    @PostMapping(value = "/yield/calculation")
    public YieldResDto getYield(@RequestBody YieldReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> yieldCalculationService.perform(reqDTO), YieldResDto::new, request);
    }

    @PostMapping(value = "/maturity-allocation/calculation")
    public MaturityAllocationResDto getMaturityAllocation(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> maturityAllocationCalculationService.perform(reqDTO), MaturityAllocationResDto::new, request);
    }

    @PostMapping(value = "/fixed-income-stylebox-exposure/calculation")
    public FixedIncomeStyleboxExposureResDto getFixedIncomeStyleboxExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> fixedIncomeStyleboxExposureCalculationService.perform(reqDTO), FixedIncomeStyleboxExposureResDto::new, request);
    }

    @PostMapping(value = "/classification-allocation/calculation")
    public ClassificationAllocationResDto getClassificationAllocation(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> classificationAllocationCalculationService.perform(reqDTO), ClassificationAllocationResDto::new, request);
    }

    @PostMapping(value = "/equity-geographic-exposure/calculation")
    public GeographicExposureResDTO getEquityGeographicExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> equityGeographicExposureCalculationService.perform(reqDTO), GeographicExposureResDTO::new, request);
    }

    @PostMapping(value = "/asset-allocations/calculation")
    public AssetAllocationResDTO getAssetAllocations(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> assetAllocationService.perform(reqDTO), AssetAllocationResDTO::new, request);
    }

    @PostMapping(value = "/asset-allocations-em/calculation")
    public AssetAllocationEMResDTO getAssetAllocationsEm(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> assetAllocationEMCalculation.perform(reqDTO), AssetAllocationEMResDTO::new, request);
    }

    @PostMapping(value = "/fixed-income-credit-quality/calculation")
    public CreditQualityResDTO getFixedIncomeCreditQuality(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> creditQualityService.perform(reqDTO), CreditQualityResDTO::new, request);
    }

    @PostMapping(value = "/equity-sector/calculation")
    public EquitySectorResDTO getEquitySector(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> equitySectorCalculation.perform(reqDTO), EquitySectorResDTO::new, request);
    }

    @PostMapping(value = "/fixed-income-country-exposure/calculation")
    public CountryExposureResDTO getFixedIncomeCountryExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> countryExposureCalculation.perform(reqDTO), CountryExposureResDTO::new, request);
    }

    @PostMapping(value = "/fixed-income-geographic-exposure/calculation")
    public GeographicExposureResDTO getFixedIncomeGeographyExposure(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> fixedIncomeGeographicExposureCalculation.perform(reqDTO), GeographicExposureResDTO::new, request);
    }

    @PostMapping(value = "/annual-return/calculation")
    public AnnualReturnResDTO<Integer> getAnnualReturn(@RequestBody ReturnReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> annualReturnService.perform(reqDTO), AnnualReturnResDTO::new, request);
    }

    @PostMapping(value = "/equity-market-capitalization/calculation")
    public EquityMarketCapResDTO getEquityMarketCapCalculation(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> equityMarketCapCalculationService.perform(reqDTO), EquityMarketCapResDTO::new, request);
    }

    @PostMapping(value = "/growth-of-10k/calculation")
    public Growth10KResDTO getGrowthOf10KCalculation(@RequestBody GrowthOf10KReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> growthOf10KCalculationService.perform(reqDTO), Growth10KResDTO::new, request);
    }

    @PostMapping(value = "/best-worst-periods/calculation")
    public BestWorstPeriodsResponseDTO getBestWorstPeriodsCalculation(@RequestBody BestWorstPeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> bestWorstPeriodsCalculationService.perform(reqDTO), BestWorstPeriodsResponseDTO::new, request);
    }

    @PostMapping(value = "/excess-returns/calculation")
    public ExcessReturnsResDTO getExcessReturns(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> excessReturnsService.perform(reqDTO), ExcessReturnsResDTO::new, request);
    }

    @PostMapping(value = "/downside-capture/calculation")
    public DownsideCaptureResDTO getDownsideCapture(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> downsideCaptureService.perform(reqDTO), DownsideCaptureResDTO::new, request);
    }

    @PostMapping(value = "/beta/calculation")
    public BetaResDTO getBeta(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> betaCalculationService.perform(reqDTO), BetaResDTO::new, request);
    }

    @PostMapping(value = "/alpha/calculation")
    public AlphaResDTO getAlpha(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> alphaCalculationService.perform(reqDTO), AlphaResDTO::new, request);
    }

    @PostMapping(value = "/rsquared/calculation")
    public RSquaredResDTO getRSquared(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> rSquaredCalculationService.perform(reqDTO), RSquaredResDTO::new, request);
    }


    @PostMapping(value = "/standard-deviation/calculation")
    public StandardDeviationResDTO getStandardDeviation(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> standardDeviationCalculationService.perform(reqDTO), StandardDeviationResDTO::new, request);
    }

    @PostMapping(value = "/mean/calculation")
    public MeanResDTO getMean(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> meanCalculationService.perform(reqDTO), MeanResDTO::new, request);
    }

    @PostMapping(value = "/tracking-error/calculation")
    public TrackingErrorResDTO getTrackingError(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> trackingErrorService.perform(reqDTO), TrackingErrorResDTO::new, request);
    }

    @PostMapping(value = "/max-drawdown/calculation")
    public MaxDrawdownResDTO getMaxDrawdown(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> maxDrawdownService.perform(reqDTO), MaxDrawdownResDTO::new, request);
    }

    @PostMapping(value = "/correlation/calculation")
    public CorrelationResDTO getCorrelation(@RequestBody CorrelationReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> correlationService.perform(reqDTO), CorrelationResDTO::new, request);
    }

    @PostMapping(value = "/sharpe-ratio/calculation")
    public SharpeRatioResDTO getSharpeRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> sharpeRatioCalculationService.perform(reqDTO), SharpeRatioResDTO::new, request);
    }

    @PostMapping(value = "/fixed-income-bond-sector/calculation")
    public FixedIncomeSectorResDTO getFixedIncomeBondSector(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> fixedIncomeBondSectorCalculationService.perform(reqDTO), FixedIncomeSectorResDTO::new, request);
    }

    @PostMapping(value = "/downside-deviation/calculation")
    public DownsideDeviationResDTO getDownsideDeviation(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> downsideDeviationService.perform(reqDTO), DownsideDeviationResDTO::new, request);
    }

    @PostMapping(value = "/sortino-ratio/calculation")
    public SortinoRatioResDTO getSortinoRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> sortinoRatioService.perform(reqDTO), SortinoRatioResDTO::new, request);
    }

    @PostMapping(value = "/top-common-holdings/calculation")
    public TopCommonHoldingsResDTO getTopCommonHoldings(@RequestBody TopCommonHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> commonHoldingsService.perform(reqDTO), TopCommonHoldingsResDTO::new, request);
    }

    @PostMapping(value = "/rolling-standard-deviation/calculation")
    public RollingStandardDeviationResDTO getRollingStandardDeviation(@RequestBody RollingCalculationReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> rollingStandardDeviationCalculationService.perform(reqDTO), RollingStandardDeviationResDTO::new, request);
    }

    @PostMapping(value = "/rolling-sharpe-ratio/calculation")
    public RollingSharpeRatioResDTO getRollingSharpeRatio(@RequestBody RollingCalculationReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> rollingSharpeRatioCalculationService.perform(reqDTO), RollingSharpeRatioResDTO::new, request);
    }

    @PostMapping(value = "/distribution-of-monthly-return/calculation")
    public DistributionOfReturnsResDTO getDistributionOfMonthlyReturn(@RequestBody DistributionOfReturnsReqDTO reqDTO,
                                                                      HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> distributionOfReturns.perform(reqDTO), DistributionOfReturnsResDTO::new, request);
    }

    @PostMapping(value = "/mar-ratio/calculation")
    public MARRatioResDTO getMarRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> marRationService.perform(reqDTO), MARRatioResDTO::new, request);
    }

    @PostMapping(value = "/treynor-ratio/calculation")
    public TreynorRatioResDTO getTreynorRatio(@RequestBody PeriodsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> treynorRatioService.perform(reqDTO), TreynorRatioResDTO::new, request);
    }

    @PostMapping(value = "/rolling-correlation/calculation")
    public RollingCorrelationResDTO getRollingCorrelation(@RequestBody RollingCorrelationCalculationReqDTO reqDTO,
                                                          HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> rollingCorrelationCalculationService.perform(reqDTO), RollingCorrelationResDTO::new, request);
    }

    @PostMapping(value = "/sales-charge/calculation")
    public SalesChargeResDtos getSalesCharge(@RequestBody PortfolioHoldingsReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> salesChargeService.perform(reqDTO), SalesChargeResDtos::new, request);
    }

    @PostMapping(value = "/daily-performance/calculation")
    public DailyPerformanceResDTO getDailyPerformance(@RequestBody DailyPerformanceReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> dailyPerformanceCalculationService.perform(reqDTO), DailyPerformanceResDTO::new, request);
    }

    @PostMapping(value = "/distribution/calculation")
    public DistributionResDTO getDistribution(@RequestBody DailyPerformanceReqDTO reqDTO, HttpServletRequest request) {
        return exceptionHandlingService.returnObjectWithListOfErrors(() -> distributionService.perform(reqDTO), DistributionResDTO::new, request);
    }

//    @PostMapping(value = "/inflation/calculation")
//    public InflationResDTO getInflation(@RequestBody DailyPerformanceReqDTO reqDTO, HttpServletRequest request) {
//        return exceptionHandlingService.returnObjectWithListOfErrors(() -> inflationService.perform(reqDTO), InflationResDTO::new, request);
//    }
}
