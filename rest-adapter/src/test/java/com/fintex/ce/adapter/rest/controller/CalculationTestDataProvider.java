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
import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.dto.command.IncomeForecastCommand;
import com.fintex.ce.domain.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.dto.command.ReturnCommand;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.dto.command.RollingCorrelationCommand;
import com.fintex.ce.domain.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.domain.dto.command.YieldCommand;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegionEmType;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.calculation.ClassificationAllocationType;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.calculation.FixedIncomeCreditQuality;
import com.fintex.sm.model.domain.enumeration.FixedIncomeSecuritiesAllocationType;
import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.calculation.MaturityAllocationType;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.enumeration.ParameterType;
import com.fintex.ce.domain.model.result.AlphaResult;
import com.fintex.ce.domain.model.result.AnnualReturnResult;
import com.fintex.ce.domain.model.result.AssetAllocationEMResult;
import com.fintex.ce.domain.model.result.AssetAllocationResult;
import com.fintex.ce.domain.model.result.AverageMerResult;
import com.fintex.ce.domain.model.result.BestWorstPeriodsResult;
import com.fintex.ce.domain.model.result.BetaResult;
import com.fintex.ce.domain.model.result.ClassificationAllocationResult;
import com.fintex.ce.domain.model.result.CommonPerformanceDatesResult;
import com.fintex.ce.domain.model.result.CorrelationResult;
import com.fintex.ce.domain.model.result.CountryExposureResult;
import com.fintex.ce.domain.model.result.CreditQualityResult;
import com.fintex.ce.domain.model.result.DistributionOfReturnsResult;
import com.fintex.ce.domain.model.result.DownsideCaptureResult;
import com.fintex.ce.domain.model.result.DownsideDeviationResult;
import com.fintex.ce.domain.model.result.EquityCountryExposureResult;
import com.fintex.ce.domain.model.result.EquityGeographicExposureResult;
import com.fintex.ce.domain.model.result.EquityMarketCapResult;
import com.fintex.ce.domain.model.result.EquitySectorResult;
import com.fintex.ce.domain.model.result.EquityStyleboxExposureResult;
import com.fintex.ce.domain.model.result.ErrorResult;
import com.fintex.ce.domain.model.result.ExcessReturnsResult;
import com.fintex.ce.domain.model.result.FixedIncomeGeographicExposureResult;
import com.fintex.ce.domain.model.result.FixedIncomeSectorResult;
import com.fintex.ce.domain.model.result.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.domain.model.result.Growth10KResult;
import com.fintex.ce.domain.model.result.IncomeForecastResult;
import com.fintex.ce.domain.model.result.InformationRatioResult;
import com.fintex.ce.domain.model.result.LeadingTotalReturnsResult;
import com.fintex.ce.domain.model.result.MARRatioResult;
import com.fintex.ce.domain.model.result.ManagementFeeResult;
import com.fintex.ce.domain.model.result.MaturityAllocationResult;
import com.fintex.ce.domain.model.result.MaxDrawdownResult;
import com.fintex.ce.domain.model.result.MeanResult;
import com.fintex.ce.domain.model.result.PeriodResult;
import com.fintex.ce.domain.model.result.RSquaredResult;
import com.fintex.ce.domain.model.result.RollingCorrelationResult;
import com.fintex.ce.domain.model.result.RollingSharpeRatioResult;
import com.fintex.ce.domain.model.result.RollingStandardDeviationResult;
import com.fintex.ce.domain.model.result.RollingTotalReturnsResult;
import com.fintex.ce.domain.model.result.SalesChargeResult;
import com.fintex.ce.domain.model.result.SharpeRatioResult;
import com.fintex.ce.domain.model.result.SortinoRatioResult;
import com.fintex.ce.domain.model.result.StandardDeviationResult;
import com.fintex.ce.domain.model.result.TopCommonHoldingsResult;
import com.fintex.ce.domain.model.result.TrackingErrorResult;
import com.fintex.ce.domain.model.result.TrailingTotalReturnsResult;
import com.fintex.ce.domain.model.result.TreynorRatioResult;
import com.fintex.ce.domain.model.result.UpsideCaptureResult;
import com.fintex.ce.domain.model.result.YieldResult;
import com.fintex.ce.domain.model.result.core.IntervalResult;
import com.fintex.ce.domain.model.result.core.RollingIntervalResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import com.fintex.sm.model.domain.enumeration.EquityMarketCapitalizationType;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
import com.fintex.sm.model.domain.enumeration.FixedIncomeStyleBoxType;
import com.fintex.sm.model.domain.enumeration.StyleBoxType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.params.provider.Arguments;

@UtilityClass
class CalculationTestDataProvider {

  static final LocalDate PED = LocalDate.of(2024, 12, 31);
  static final LocalDate PSD = LocalDate.of(2024, 1, 1);
  static final LocalDate CUSTOM_IPSD = LocalDate.of(2024, 6, 1);

  static final Set<TimeIntervalResult> TIME_INTERVALS = Set.of(
      new TimeIntervalResult().setTimeIntervalPeriod("12M").setValue(BigDecimal.valueOf(8.56)),
      new TimeIntervalResult().setTimeIntervalPeriod("36M").setValue(BigDecimal.valueOf(6.23)));

  static final Set<RollingIntervalResult> ROLLING_INTERVALS = Set.of(
      new RollingIntervalResult()
          .setTimeIntervalPeriod("12M")
          .setValues(Set.of(
              new IntervalResult().setKey(LocalDate.of(2024, 6, 30)).setValue(BigDecimal.valueOf(5.12)),
              new IntervalResult().setKey(LocalDate.of(2024, 12, 31)).setValue(BigDecimal.valueOf(7.89)))));

  static Stream<Arguments> calculationMetricArguments() {
    return Stream.of(
        period(CalculationMetric.TRAILING_TOTAL_RETURNS, new TrailingTotalReturnsResult().setTrailingTotalReturn(TIME_INTERVALS), TrailingTotalReturnsResDTO.class),
        period(CalculationMetric.LEADING_TOTAL_RETURNS, new LeadingTotalReturnsResult().setLeadingTotalReturn(TIME_INTERVALS), LeadingTotalReturnsResDTO.class),
        period(CalculationMetric.EXCESS_RETURNS, new ExcessReturnsResult().setExcessReturns(TIME_INTERVALS), ExcessReturnsResDTO.class),
        period(CalculationMetric.STANDARD_DEVIATION, new StandardDeviationResult().setStandardDeviation(TIME_INTERVALS), StandardDeviationResDTO.class),
        period(CalculationMetric.MEAN, new MeanResult().setMean(TIME_INTERVALS), MeanResDTO.class),
        period(CalculationMetric.SHARPE_RATIO, new SharpeRatioResult().setSharpeRatio(TIME_INTERVALS), SharpeRatioResDTO.class),
        period(CalculationMetric.SORTINO_RATIO, new SortinoRatioResult().setSortinoRatio(TIME_INTERVALS), SortinoRatioResDTO.class),
        period(CalculationMetric.DOWNSIDE_DEVIATION, new DownsideDeviationResult().setDownsideDeviation(TIME_INTERVALS), DownsideDeviationResDTO.class),
        period(CalculationMetric.MAR_RATIO, new MARRatioResult().setMarRatio(TIME_INTERVALS), MARRatioResDTO.class),
        period(CalculationMetric.TREYNOR_RATIO, new TreynorRatioResult().setTreynorRatio(TIME_INTERVALS), TreynorRatioResDTO.class),
        period(CalculationMetric.INFORMATION_RATIO, new InformationRatioResult().setTimeIntervalResultS(TIME_INTERVALS), InformationRatioResDTO.class),
        period(CalculationMetric.TRACKING_ERROR, new TrackingErrorResult().setTrackingError(TIME_INTERVALS), TrackingErrorResDTO.class),
        period(CalculationMetric.ALPHA, new AlphaResult().setAlpha(TIME_INTERVALS), AlphaResDTO.class),
        period(CalculationMetric.BETA, new BetaResult().setBeta(TIME_INTERVALS), BetaResDTO.class),
        period(CalculationMetric.R_SQUARED, new RSquaredResult().setRSquared(TIME_INTERVALS), RSquaredResDTO.class),
        period(CalculationMetric.UPSIDE_CAPTURE, new UpsideCaptureResult().setUpsideCapture(TIME_INTERVALS), UpsideCaptureResDTO.class),
        period(CalculationMetric.DOWNSIDE_CAPTURE, new DownsideCaptureResult().setDownsideCapture(TIME_INTERVALS), DownsideCaptureResDTO.class),
        period(CalculationMetric.MAX_DRAWDOWN, new MaxDrawdownResult().setMaxDrawdown(List.of()), MaxDrawdownResDTO.class),
        period(CalculationMetric.CORRELATION, new CorrelationResult().setHoldingsKey(List.of()).setCorrelationPeriods(List.of()), CorrelationResDTO.class),
        period(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS, new DistributionOfReturnsResult(), DistributionOfReturnsResDTO.class),

        rolling(CalculationMetric.ROLLING_TOTAL_RETURNS, new RollingTotalReturnsResult().setRollingTotalReturns(ROLLING_INTERVALS), RollingTotalReturnsResDTO.class),
        rolling(CalculationMetric.ROLLING_STANDARD_DEVIATION, new RollingStandardDeviationResult().setRollingStandardDeviation(ROLLING_INTERVALS), RollingStandardDeviationResDTO.class),
        rolling(CalculationMetric.ROLLING_SHARPE_RATIO, new RollingSharpeRatioResult().setRollingSharpeRatio(ROLLING_INTERVALS), RollingSharpeRatioResDTO.class),
        entry(CalculationMetric.ROLLING_CORRELATION, rollingCorrelationCommand(), new RollingCorrelationResult().setRollingCorrelation(ROLLING_INTERVALS).setPed(PED).setPsd(PSD), RollingCorrelationResDTO.class),

        breakdown(CalculationMetric.ASSET_ALLOCATIONS, new AssetAllocationResult().setAssetAllocation(Map.of(AssetAllocationRegionType.CASH, BigDecimal.valueOf(45.5))), AssetAllocationResDTO.class),
        breakdown(CalculationMetric.ASSET_ALLOCATIONS_EM, new AssetAllocationEMResult().setAssetAllocationEmergingMarkets(Map.of(AssetAllocationRegionEmType.CASH, BigDecimal.valueOf(12.3))), AssetAllocationEMResDTO.class),
        breakdown(CalculationMetric.EQUITY_SECTOR, new EquitySectorResult().setEquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, BigDecimal.valueOf(30.0))), EquitySectorResDTO.class),
        breakdown(CalculationMetric.EQUITY_COUNTRY_EXPOSURE, new EquityCountryExposureResult().setEquityCountryExposure(Map.of(CountryRegionType.CANADA, BigDecimal.valueOf(60.0))), EquityCountryExposureResDTO.class),
        breakdown(CalculationMetric.EQUITY_STYLEBOX_EXPOSURE, new EquityStyleboxExposureResult().setEquityStyleboxExposure(Map.of(StyleBoxType.LARGE_CORE, BigDecimal.valueOf(40.0))), EquityStyleboxExposureResDto.class),
        breakdown(CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE, new EquityGeographicExposureResult().setEquityGeographicExposure(Map.of(GeographicRegionType.OTHER, BigDecimal.valueOf(70.0))), GeographicExposureResDTO.class),
        breakdown(CalculationMetric.EQUITY_MARKET_CAPITALIZATION, new EquityMarketCapResult().setEquityMarketCapitalization(Map.of(EquityMarketCapitalizationType.GIANT, BigDecimal.valueOf(55.0))), EquityMarketCapResDTO.class),
        breakdown(CalculationMetric.FIXED_INCOME_COUNTRY_EXPOSURE, new CountryExposureResult().setCountryExposure(Map.of(CountryRegionType.CANADA, BigDecimal.valueOf(80.0))), CountryExposureResDTO.class),
        breakdown(CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE, new FixedIncomeGeographicExposureResult().setEquityGeographicExposure(Map.of(GeographicRegionType.OTHER, BigDecimal.valueOf(25.0))), GeographicExposureResDTO.class),
        breakdown(CalculationMetric.FIXED_INCOME_BOND_SECTOR, new FixedIncomeSectorResult().setFixedIncomeSector(Map.of(FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(35.0))), FixedIncomeSectorResDTO.class),
        breakdown(CalculationMetric.FIXED_INCOME_STYLEBOX_EXPOSURE, new FixedIncomeStyleboxExposureResult().setFixedIncomeStyleboxExposure(Map.of(FixedIncomeStyleBoxType.HIGH_LIMITED, BigDecimal.valueOf(20.0))), FixedIncomeStyleboxExposureResDto.class),
        breakdown(CalculationMetric.MATURITY_ALLOCATION, new MaturityAllocationResult().setMaturityAllocation(Map.of(MaturityAllocationType.MORE_THAN_TWENTY_YEARS, BigDecimal.valueOf(15.0))), MaturityAllocationResDto.class),
        breakdown(CalculationMetric.CLASSIFICATION_ALLOCATION, new ClassificationAllocationResult().setClassificationAllocation(Map.of(ClassificationAllocationType.UNCLASSIFIED__UNCLASSIFIED, BigDecimal.valueOf(30.0))), ClassificationAllocationResDto.class),
        breakdown(CalculationMetric.SALES_CHARGE, new SalesChargeResult(), SalesChargeResDtos.class),
        breakdown(CalculationMetric.FIXED_INCOME_CREDIT_QUALITY, new CreditQualityResult().setCreditQuality(Map.of(FixedIncomeCreditQuality.AAA, BigDecimal.valueOf(25.0))), CreditQualityResDTO.class),

        fee(CalculationMetric.MER, new AverageMerResult().setManagementExpenseRatio(Map.of(ParameterType.SCALED, BigDecimal.valueOf(1.25))), AverageMerResponse.class),
        fee(CalculationMetric.MANAGEMENT_FEE, new ManagementFeeResult().setManagementFee(Map.of(ParameterType.ABSOLUTE, BigDecimal.valueOf(0.85))), ManagementFeeResponse.class),

        entry(CalculationMetric.ANNUAL_RETURNS, returnCommand(), annualReturnResult(), AnnualReturnResDTO.class),
        entry(CalculationMetric.GROWTH_OF_10K, returnCommand(), growth10kResult(), Growth10KResDTO.class),
        entry(CalculationMetric.BEST_WORST_PERIODS, bestWorstPeriodsCommand(), bestWorstPeriodsResult(), BestWorstPeriodsResponseDTO.class),
        entry(CalculationMetric.INCOME_FORECAST, incomeForecastCommand(), new IncomeForecastResult().setIncomeForecast(List.of()), IncomeForecastResDto.class),
        entry(CalculationMetric.YIELD, yieldCommand(), new YieldResult().setYield(BigDecimal.valueOf(3.45)), YieldResDto.class),
        entry(CalculationMetric.COMMON_PERFORMANCE_DATES, multiplePortfoliosCommand(), commonPerformanceDatesResult(), CommonPerformanceDatesResDTO.class),
        entry(CalculationMetric.TOP_COMMON_HOLDINGS, topCommonHoldingsCommand(), new TopCommonHoldingsResult().setCommonHoldings(List.of()), TopCommonHoldingsResDTO.class));
  }

  private static Arguments period(CalculationMetric metric, PeriodResult result,
      Class<? extends ErrorDTO> responseType) {
    result.setPed(PED);
    result.setPsd(PSD);
    result.setCustomIpsd(CUSTOM_IPSD);
    return Arguments.of(metric, periodCommand(), result, responseType);
  }

  private static Arguments rolling(CalculationMetric metric, PeriodResult result,
      Class<? extends ErrorDTO> responseType) {
    result.setPed(PED);
    result.setPsd(PSD);
    return Arguments.of(metric, rollingCommand(), result, responseType);
  }

  private static Arguments breakdown(CalculationMetric metric, ErrorResult result,
      Class<? extends ErrorDTO> responseType) {
    return Arguments.of(metric, portfolioHoldingsCommand(), result, responseType);
  }

  private static Arguments fee(CalculationMetric metric, ErrorResult result,
      Class<? extends ErrorDTO> responseType) {
    return Arguments.of(metric, averageMerCommand(), result, responseType);
  }

  private static Arguments entry(CalculationMetric metric, CalculationCommand command,
      ErrorResult result, Class<? extends ErrorDTO> responseType) {
    return Arguments.of(metric, command, result, responseType);
  }

  static PeriodCommand periodCommand() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setHoldings(List.of());
    cmd.setPeriods(Set.of("12M", "36M"));
    return cmd;
  }

  private static RollingCalculationCommand rollingCommand() {
    RollingCalculationCommand cmd = new RollingCalculationCommand();
    cmd.setHoldings(List.of());
    cmd.setPeriods(Set.of("12M"));
    cmd.setRollingPeriods(Set.of("12M"));
    return cmd;
  }

  private static RollingCorrelationCommand rollingCorrelationCommand() {
    RollingCorrelationCommand cmd = new RollingCorrelationCommand();
    cmd.setHoldings(List.of());
    cmd.setBenchmarkHoldings(List.of());
    cmd.setPeriods(Set.of("12M"));
    cmd.setRollingPeriods(Set.of("12M"));
    return cmd;
  }

  private static PortfolioHoldingsCommand portfolioHoldingsCommand() {
    PortfolioHoldingsCommand cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(List.of());
    return cmd;
  }

  private static AverageMerCommand averageMerCommand() {
    AverageMerCommand cmd = new AverageMerCommand();
    cmd.setHoldings(List.of());
    cmd.setParameterTypes(List.of(ParameterType.SCALED, ParameterType.ABSOLUTE));
    return cmd;
  }

  private static ReturnCommand returnCommand() {
    ReturnCommand cmd = new ReturnCommand();
    cmd.setHoldings(List.of());
    cmd.setCustomPerformanceStartDate(PSD);
    cmd.setCustomPerformanceEndDate(PED);
    return cmd;
  }

  private static BestWorstPeriodsCommand bestWorstPeriodsCommand() {
    BestWorstPeriodsCommand cmd = new BestWorstPeriodsCommand();
    cmd.setHoldings(List.of());
    cmd.setCustomPerformanceStartDate(PSD);
    cmd.setCustomPerformanceEndDate(PED);
    cmd.setBestWorstTimeIntervalPeriods(Set.of(12L, 36L));
    return cmd;
  }

  private static IncomeForecastCommand incomeForecastCommand() {
    IncomeForecastCommand cmd = new IncomeForecastCommand();
    cmd.setHoldings(List.of());
    cmd.setTimeIntervalPeriods(12);
    return cmd;
  }

  private static YieldCommand yieldCommand() {
    YieldCommand cmd = new YieldCommand();
    cmd.setHoldings(List.of());
    cmd.setTimeIntervalPeriods(12);
    return cmd;
  }

  private static MultiplePortfoliosCommand multiplePortfoliosCommand() {
    MultiplePortfoliosCommand cmd = new MultiplePortfoliosCommand();
    cmd.setPortfolios(Set.of());
    cmd.setBenchmarkHoldings(List.of());
    return cmd;
  }

  private static TopCommonHoldingsCommand topCommonHoldingsCommand() {
    TopCommonHoldingsCommand cmd = new TopCommonHoldingsCommand();
    cmd.setHoldings(List.of());
    cmd.setNumOfFundsMin(2);
    cmd.setNumOfTopCommonHoldings(10);
    return cmd;
  }

  private static AnnualReturnResult<Integer> annualReturnResult() {
    AnnualReturnResult<Integer> result = new AnnualReturnResult<>();
    result.setPed(PED);
    result.setPsd(PSD);
    return result;
  }

  private static Growth10KResult growth10kResult() {
    Growth10KResult result = new Growth10KResult();
    result.setPed(PED);
    result.setPsd(PSD);
    return result;
  }

  private static BestWorstPeriodsResult bestWorstPeriodsResult() {
    BestWorstPeriodsResult result = new BestWorstPeriodsResult();
    result.setPed(PED);
    result.setPsd(PSD);
    return result;
  }

  private static CommonPerformanceDatesResult commonPerformanceDatesResult() {
    CommonPerformanceDatesResult result = new CommonPerformanceDatesResult();
    result.setCommonPerformanceStartDatePf(LocalDate.of(2020, 1, 1));
    result.setCommonPerformanceEndDatePf(PED);
    result.setCommonPerformanceStartDateBm(LocalDate.of(2019, 1, 1));
    result.setCommonPerformanceEndDateBm(PED);
    return result;
  }
}
