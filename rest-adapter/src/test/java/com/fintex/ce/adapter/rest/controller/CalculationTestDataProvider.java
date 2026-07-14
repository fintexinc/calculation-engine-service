package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocationType;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeCreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocationType;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.CommonPerformanceDatesResult;
import com.fintex.ce.model.domain.result.IntervalResult;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.RollingIntervalResult;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
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
import com.fintex.ce.model.domain.result.exposure.EquityGeographicExposureResult;
import com.fintex.ce.model.domain.result.exposure.EquityStyleboxExposureResult;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeGeographicExposureResult;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.model.domain.result.exposure.GeographicExposureResult;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.domain.result.fee.FeesResult;
import com.fintex.ce.model.domain.result.fee.ManagementFeeResult;
import com.fintex.ce.model.domain.result.fee.SalesChargeResult;
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
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.IncomeForecastCommand;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.dto.command.RollingCorrelationCommand;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.dto.command.YieldCommand;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;
import com.fintex.wm.commons.domain.rating.StyleBoxType;

import org.junit.jupiter.params.provider.Arguments;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
class CalculationTestDataProvider {

  static final LocalDate PED = LocalDate.of(2024, 12, 31);
  static final LocalDate PSD = LocalDate.of(2024, 1, 1);
  static final LocalDate CUSTOM_IPSD = LocalDate.of(2024, 6, 1);

  static final PortfolioHolding DUMMY_HOLDING = new PortfolioHolding(
      BigDecimal.ONE, FinancialInstrumentType.MUTUAL_FUND_CANADA,
      new SecurityIdentifier("DUMMY", FiIdentifierType.TICKER));

  static final Set<TimeIntervalResult> TIME_INTERVALS = Set.of(
      new TimeIntervalResult("12M", BigDecimal.valueOf(8.56)),
      new TimeIntervalResult("36M", BigDecimal.valueOf(6.23)));

  static final Set<RollingIntervalResult> ROLLING_INTERVALS = Set.of(
      new RollingIntervalResult("12M", Set.of(
          new IntervalResult(LocalDate.of(2024, 6, 30), BigDecimal.valueOf(5.12)),
          new IntervalResult(LocalDate.of(2024, 12, 31), BigDecimal.valueOf(7.89)))));

  static Stream<Arguments> calculationMetricArguments() {
    return Stream.of(
        period(CalculationMetric.TRAILING_TOTAL_RETURNS, init(new TrailingTotalReturnsResult(), r -> r
            .setTrailingTotalReturn(TIME_INTERVALS)), TrailingTotalReturnsResult.class),
        period(CalculationMetric.LEADING_TOTAL_RETURNS, init(new LeadingTotalReturnsResult(), r -> r
            .setLeadingTotalReturn(TIME_INTERVALS)), LeadingTotalReturnsResult.class),
        period(CalculationMetric.EXCESS_RETURNS, init(new ExcessReturnsResult(), r -> r.setExcessReturns(
            TIME_INTERVALS)),
            ExcessReturnsResult.class),
        period(CalculationMetric.STANDARD_DEVIATION, init(new StandardDeviationResult(), r -> r.setStandardDeviation(
            TIME_INTERVALS)),
            StandardDeviationResult.class),
        period(CalculationMetric.MEAN, init(new MeanResult(), r -> r.setMean(TIME_INTERVALS)), MeanResult.class),
        period(CalculationMetric.SHARPE_RATIO, init(new SharpeRatioResult(), r -> r.setSharpeRatio(TIME_INTERVALS)),
            SharpeRatioResult.class),
        period(CalculationMetric.SORTINO_RATIO, init(new SortinoRatioResult(), r -> r.setSortinoRatio(TIME_INTERVALS)),
            SortinoRatioResult.class),
        period(CalculationMetric.DOWNSIDE_DEVIATION, init(new DownsideDeviationResult(), r -> r.setDownsideDeviation(
            TIME_INTERVALS)),
            DownsideDeviationResult.class),
        period(CalculationMetric.MAR_RATIO, init(new MarRatioResult(), r -> r.setMarRatio(TIME_INTERVALS)),
            MarRatioResult.class),
        period(CalculationMetric.TREYNOR_RATIO, init(new TreynorRatioResult(), r -> r.setTreynorRatio(TIME_INTERVALS)),
            TreynorRatioResult.class),
        period(CalculationMetric.INFORMATION_RATIO, init(new InformationRatioResult(), r -> r.setInformationRatio(
            TIME_INTERVALS)),
            InformationRatioResult.class),
        period(CalculationMetric.TRACKING_ERROR, init(new TrackingErrorResult(), r -> r.setTrackingError(
            TIME_INTERVALS)),
            TrackingErrorResult.class),
        period(CalculationMetric.ALPHA, init(new AlphaResult(), r -> r.setAlpha(TIME_INTERVALS)), AlphaResult.class),
        period(CalculationMetric.BETA, init(new BetaResult(), r -> r.setBeta(TIME_INTERVALS)), BetaResult.class),
        period(CalculationMetric.R_SQUARED, init(new RSquaredResult(), r -> r.setRSquared(TIME_INTERVALS)),
            RSquaredResult.class),
        period(CalculationMetric.UPSIDE_CAPTURE, init(new UpsideCaptureResult(), r -> r.setUpsideCapture(
            TIME_INTERVALS)),
            UpsideCaptureResult.class),
        period(CalculationMetric.DOWNSIDE_CAPTURE, init(new DownsideCaptureResult(), r -> r.setDownsideCapture(
            TIME_INTERVALS)),
            DownsideCaptureResult.class),
        period(CalculationMetric.MAX_DRAWDOWN, init(new MaxDrawdownResult(), r -> r.setMaxDrawdown(List.of())),
            MaxDrawdownResult.class),
        period(CalculationMetric.CORRELATION, init(new CorrelationResult(), r -> {
          r.setHoldingsKey(List.of());
          r.setCorrelationPeriods(List.of());
        }), CorrelationResult.class),
        period(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS, new DistributionOfReturnsResult(),
            DistributionOfReturnsResult.class),

        rolling(CalculationMetric.ROLLING_TOTAL_RETURNS, init(new RollingTotalReturnsResult(), r -> r
            .setRollingTotalReturns(ROLLING_INTERVALS)), RollingTotalReturnsResult.class),
        rolling(CalculationMetric.ROLLING_STANDARD_DEVIATION, init(new RollingStandardDeviationResult(), r -> r
            .setRollingStandardDeviation(ROLLING_INTERVALS)), RollingStandardDeviationResult.class),
        rolling(CalculationMetric.ROLLING_SHARPE_RATIO, init(new RollingSharpeRatioResult(), r -> r
            .setRollingSharpeRatio(ROLLING_INTERVALS)), RollingSharpeRatioResult.class),
        entry(CalculationMetric.ROLLING_CORRELATION, rollingCorrelationCommand(), init(new RollingCorrelationResult(),
            r -> {
              r.setRollingCorrelation(ROLLING_INTERVALS);
              r.setPerformanceEndDate(PED);
              r.setPerformanceStartDate(PSD);
            }),
            RollingCorrelationResult.class),

        breakdown(CalculationMetric.ASSET_ALLOCATIONS, init(new AssetAllocationResult(), r -> r.setAssetAllocation(Map
            .of(
                AssetAllocationRegionType.CASH, BigDecimal.valueOf(45.5)))), AssetAllocationResult.class),
        breakdown(CalculationMetric.ASSET_ALLOCATIONS_EM, init(new AssetAllocationEMResult(), r -> r
            .setAssetAllocationEmergingMarkets(Map.of(AssetAllocationRegionType.CASH, BigDecimal.valueOf(12.3)))),
            AssetAllocationEMResult.class),
        breakdown(CalculationMetric.EQUITY_SECTOR, init(new EquitySectorResult(), r -> r.setEquitySector(Map.of(
            EquitySectorAllocationType.TECHNOLOGY, BigDecimal.valueOf(30.0)))), EquitySectorResult.class),
        breakdown(CalculationMetric.EQUITY_COUNTRY_EXPOSURE, init(new EquityCountryExposureResult(), r -> r
            .setEquityCountryExposure(Map.of(CountryRegionType.CANADA, BigDecimal.valueOf(60.0)))),
            EquityCountryExposureResult.class),
        breakdown(CalculationMetric.EQUITY_STYLEBOX_EXPOSURE, init(new EquityStyleboxExposureResult(), r -> r
            .setEquityStyleboxExposure(Map.of(StyleBoxType.LARGE_CORE, BigDecimal.valueOf(40.0)))),
            EquityStyleboxExposureResult.class),
        breakdown(CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE, init(new EquityGeographicExposureResult(), r -> r
            .setGeographicExposure(Map.of(GeographicRegionType.OTHER, BigDecimal.valueOf(70.0)))),
            GeographicExposureResult.class),
        breakdown(CalculationMetric.EQUITY_MARKET_CAPITALIZATION, init(new EquityMarketCapResult(), r -> r
            .setEquityMarketCapitalization(Map.of(EquityMarketCapitalizationType.GIANT, BigDecimal.valueOf(55.0)))),
            EquityMarketCapResult.class),
        breakdown(CalculationMetric.FIXED_INCOME_COUNTRY_EXPOSURE, init(new CountryExposureResult(), r -> r
            .setCountryExposure(Map
                .of(CountryRegionType.CANADA, BigDecimal.valueOf(80.0)))), CountryExposureResult.class),
        breakdown(CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE, init(new FixedIncomeGeographicExposureResult(),
            r -> r.setGeographicExposure(Map.of(GeographicRegionType.OTHER, BigDecimal.valueOf(25.0)))),
            GeographicExposureResult.class),
        breakdown(CalculationMetric.FIXED_INCOME_BOND_SECTOR, init(new FixedIncomeSectorResult(), r -> r
            .setFixedIncomeSector(Map.of(
                FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(35.0)))),
            FixedIncomeSectorResult.class),
        breakdown(CalculationMetric.FIXED_INCOME_STYLEBOX_EXPOSURE, init(new FixedIncomeStyleboxExposureResult(), r -> r
            .setFixedIncomeStyleboxExposure(Map.of(FixedIncomeStyleBoxType.HIGH_LIMITED, BigDecimal.valueOf(20.0)))),
            FixedIncomeStyleboxExposureResult.class),
        breakdown(CalculationMetric.MATURITY_ALLOCATION, init(new MaturityAllocationResult(), r -> r
            .setMaturityAllocation(Map.of(
                MaturityAllocationType.MORE_THAN_TWENTY_YEARS, BigDecimal.valueOf(15.0)))),
            MaturityAllocationResult.class),
        breakdown(CalculationMetric.CLASSIFICATION_ALLOCATION, init(new ClassificationAllocationResult(), r -> r
            .setClassificationAllocation(Map.of(ClassificationAllocationType.UNCLASSIFIED__UNCLASSIFIED, BigDecimal
                .valueOf(30.0)))), ClassificationAllocationResult.class),
        breakdown(CalculationMetric.SALES_CHARGE, new SalesChargeResult(), SalesChargeResult.class),
        breakdown(CalculationMetric.FIXED_INCOME_CREDIT_QUALITY, init(new CreditQualityResult(), r -> r
            .setCreditQuality(Map.of(
                FixedIncomeCreditQuality.AAA, BigDecimal.valueOf(25.0)))), CreditQualityResult.class),

        fee(CalculationMetric.MER, init(new AverageMerResult(), r -> r.setManagementExpenseRatio(Map.of(
            FeeAggregationMode.FUNDS_ONLY,
            BigDecimal.valueOf(1.25)))), AverageMerResult.class),
        fee(CalculationMetric.MANAGEMENT_FEE, init(new ManagementFeeResult(), r -> r.setManagementFee(Map.of(
            FeeAggregationMode.WHOLE_PORTFOLIO,
            BigDecimal.valueOf(0.85)))), ManagementFeeResult.class),
        fee(CalculationMetric.FEES, init(new FeesResult(), r -> {
          r.setAnnualFee(Map.of(FeeAggregationMode.FUNDS_ONLY, BigDecimal.valueOf(125.0)));
          r.setMonthlyFee(Map.of(FeeAggregationMode.FUNDS_ONLY, BigDecimal.valueOf(125.0 / 12.0)));
        }), FeesResult.class),

        entry(CalculationMetric.ANNUAL_RETURNS, returnCommand(), annualReturnResult(), AnnualReturnResult.class),
        entry(CalculationMetric.GROWTH_OF_10K, returnCommand(), growth10kResult(), Growth10KResult.class),
        entry(CalculationMetric.BEST_WORST_PERIODS, bestWorstPeriodsCommand(), bestWorstPeriodsResult(),
            BestWorstPeriodsResult.class),
        entry(CalculationMetric.INCOME_FORECAST, incomeForecastCommand(), init(new IncomeForecastResult(), r -> r
            .setIncomeForecast(List.of())), IncomeForecastResult.class),
        entry(CalculationMetric.YIELD, yieldCommand(), init(new YieldResult(), r -> r.setYield(BigDecimal.valueOf(
            3.45))),
            YieldResult.class),
        entry(CalculationMetric.COMMON_PERFORMANCE_DATES, multiplePortfoliosCommand(), commonPerformanceDatesResult(),
            CommonPerformanceDatesResult.class),
        entry(CalculationMetric.TOP_COMMON_HOLDINGS, topCommonHoldingsCommand(), init(new TopCommonHoldingsResult(),
            r -> r.setCommonHoldings(List.of())), TopCommonHoldingsResult.class));
  }

  private static Arguments period(CalculationMetric metric, PeriodResult result,
      Class<? extends BaseCalculationResult> responseType) {
    result.setPerformanceEndDate(PED);
    result.setPerformanceStartDate(PSD);
    result.setCustomIntervalPerformanceStartDate(CUSTOM_IPSD);
    return Arguments.of(metric, periodCommand(), result, responseType);
  }

  private static Arguments rolling(CalculationMetric metric, PeriodResult result,
      Class<? extends BaseCalculationResult> responseType) {
    result.setPerformanceEndDate(PED);
    result.setPerformanceStartDate(PSD);
    return Arguments.of(metric, rollingCommand(), result, responseType);
  }

  private static Arguments breakdown(CalculationMetric metric, Object result,
      Class<? extends BaseCalculationResult> responseType) {
    return Arguments.of(metric, portfolioHoldingsCommand(), result, responseType);
  }

  private static Arguments fee(CalculationMetric metric, Object result,
      Class<? extends BaseCalculationResult> responseType) {
    return Arguments.of(metric, averageMerCommand(), result, responseType);
  }

  private static Arguments entry(CalculationMetric metric, CalculationCommand command,
      Object result, Class<? extends BaseCalculationResult> responseType) {
    return Arguments.of(metric, command, result, responseType);
  }

  static PeriodCommand periodCommand() {
    PeriodCommand cmd = new PeriodCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    cmd.setCurrency(Currency.CAD);
    cmd.setPeriods(Set.of("12M", "36M"));
    return cmd;
  }

  private static RollingCalculationCommand rollingCommand() {
    RollingCalculationCommand cmd = new RollingCalculationCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    cmd.setCurrency(Currency.CAD);
    cmd.setPeriods(Set.of("12M"));
    cmd.setRollingPeriods(Set.of("12M"));
    return cmd;
  }

  private static RollingCorrelationCommand rollingCorrelationCommand() {
    RollingCorrelationCommand cmd = new RollingCorrelationCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    cmd.setBenchmarkHoldings(List.of(DUMMY_HOLDING));
    cmd.setCurrency(Currency.CAD);
    cmd.setPeriods(Set.of("12M"));
    cmd.setRollingPeriods(Set.of("12M"));
    return cmd;
  }

  private static PortfolioHoldingsCommand portfolioHoldingsCommand() {
    PortfolioHoldingsCommand cmd = new PortfolioHoldingsCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    return cmd;
  }

  private static AverageMerCommand averageMerCommand() {
    AverageMerCommand cmd = new AverageMerCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    cmd.setParameterTypes(List.of(FeeAggregationMode.FUNDS_ONLY, FeeAggregationMode.WHOLE_PORTFOLIO));
    return cmd;
  }

  private static ReturnCommand returnCommand() {
    ReturnCommand cmd = new ReturnCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    cmd.setCurrency(Currency.CAD);
    cmd.setCustomPsd(PSD);
    cmd.setCustomPed(PED);
    return cmd;
  }

  private static BestWorstPeriodsCommand bestWorstPeriodsCommand() {
    BestWorstPeriodsCommand cmd = new BestWorstPeriodsCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    cmd.setCurrency(Currency.CAD);
    cmd.setCustomPsd(PSD);
    cmd.setCustomPed(PED);
    cmd.setBestWorstTimeIntervalPeriods(Set.of(12L, 36L));
    return cmd;
  }

  private static IncomeForecastCommand incomeForecastCommand() {
    IncomeForecastCommand cmd = new IncomeForecastCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    cmd.setTimeIntervalPeriods(12);
    return cmd;
  }

  private static YieldCommand yieldCommand() {
    YieldCommand cmd = new YieldCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    cmd.setTimeIntervalPeriods(12);
    return cmd;
  }

  private static MultiplePortfoliosCommand multiplePortfoliosCommand() {
    MultiplePortfoliosCommand cmd = new MultiplePortfoliosCommand();
    cmd.setPortfolios(Set.of(new MultiplePortfoliosCommand.Portfolio(List.of(DUMMY_HOLDING))));
    cmd.setBenchmarkHoldings(List.of(DUMMY_HOLDING));
    return cmd;
  }

  private static TopCommonHoldingsCommand topCommonHoldingsCommand() {
    TopCommonHoldingsCommand cmd = new TopCommonHoldingsCommand();
    cmd.setHoldings(List.of(DUMMY_HOLDING));
    cmd.setNumOfTopCommonHoldings(10);
    return cmd;
  }

  private static AnnualReturnResult<Integer> annualReturnResult() {
    AnnualReturnResult<Integer> result = new AnnualReturnResult<>();
    result.setPerformanceEndDate(PED);
    result.setPerformanceStartDate(PSD);
    return result;
  }

  private static Growth10KResult growth10kResult() {
    return Growth10KResult.builder()
        .growth10k(List.of())
        .build();
  }

  private static BestWorstPeriodsResult bestWorstPeriodsResult() {
    return BestWorstPeriodsResult.builder()
        .performanceEndDate(PED)
        .performanceStartDate(PSD)
        .build();
  }

  private static CommonPerformanceDatesResult commonPerformanceDatesResult() {
    return CommonPerformanceDatesResult.builder()
        .commonPerformanceStartDatePf(LocalDate.of(2020, 1, 1))
        .commonPerformanceEndDatePf(PED)
        .commonPerformanceStartDateBm(LocalDate.of(2019, 1, 1))
        .commonPerformanceEndDateBm(PED)
        .build();
  }

  private static <T> T init(final T obj, final Consumer<T> action) {
    action.accept(obj);
    return obj;
  }

}
