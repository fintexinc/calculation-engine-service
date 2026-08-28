package ca.tangerine.pce.rest.controller;

import ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.result.CommonPerformanceDatesResult;
import ca.tangerine.pce.model.domain.result.PeriodResult;
import ca.tangerine.pce.model.domain.result.TimeIntervalResult;
import ca.tangerine.pce.model.domain.result.allocation.AssetAllocationEMResult;
import ca.tangerine.pce.model.domain.result.allocation.AssetAllocationResult;
import ca.tangerine.pce.model.domain.result.allocation.ConsolidatedSectorExposureResult;
import ca.tangerine.pce.model.domain.result.allocation.EquitySectorResult;
import ca.tangerine.pce.model.domain.result.allocation.FixedIncomeSectorResult;
import ca.tangerine.pce.model.domain.result.exposure.ConsolidatedGeographicExposureResult;
import ca.tangerine.pce.model.domain.result.exposure.CountryExposureResult;
import ca.tangerine.pce.model.domain.result.exposure.EquityCountryExposureResult;
import ca.tangerine.pce.model.domain.result.exposure.EquityGeographicExposureResult;
import ca.tangerine.pce.model.domain.result.exposure.FixedIncomeGeographicExposureResult;
import ca.tangerine.pce.model.domain.result.exposure.GeographicExposureResult;
import ca.tangerine.pce.model.domain.result.fee.AverageMerResult;
import ca.tangerine.pce.model.domain.result.fee.FeesResult;
import ca.tangerine.pce.model.domain.result.fee.ManagementFeeResult;
import ca.tangerine.pce.model.domain.result.holding.TopCommonHoldingsResult;
import ca.tangerine.pce.model.domain.result.returns.AnnualReturnResult;
import ca.tangerine.pce.model.domain.result.returns.Growth10KResult;
import ca.tangerine.pce.model.domain.result.returns.TrailingTotalReturnsResult;
import ca.tangerine.pce.model.domain.result.risk.MaxDrawdownResult;
import ca.tangerine.pce.model.domain.result.risk.SharpeRatioResult;
import ca.tangerine.pce.model.domain.result.risk.StandardDeviationResult;
import ca.tangerine.pce.model.dto.command.AverageMerCommand;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.MultiplePortfoliosCommand;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.pce.model.dto.command.ReturnCommand;
import ca.tangerine.pce.model.dto.command.TopCommonHoldingsCommand;
import ca.tangerine.wm.commons.domain.allocation.AssetAllocationRegionType;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationType;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.params.provider.Arguments;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;

@UtilityClass
class CalculationTestDataProvider {

  static final LocalDate PED = LocalDate.of(2024, 12, 31);
  static final LocalDate PSD = LocalDate.of(2024, 1, 1);
  static final LocalDate CUSTOM_IPSD = LocalDate.of(2024, 6, 1);

  static final PortfolioHolding DUMMY_HOLDING = holding(
      new SecurityIdentifier("DUMMY", FiIdentifierType.TICKER), FinancialInstrumentType.MUTUAL_FUND, Country.CANADA,
      BigDecimal.ONE);

  static final Set<TimeIntervalResult> TIME_INTERVALS = Set.of(
      new TimeIntervalResult("12M", BigDecimal.valueOf(8.56)),
      new TimeIntervalResult("36M", BigDecimal.valueOf(6.23)));

  static Stream<Arguments> calculationMetricArguments() {
    return Stream.of(
        period(CalculationMetric.TRAILING_TOTAL_RETURNS, init(new TrailingTotalReturnsResult(), r -> r
            .setTrailingTotalReturn(List.copyOf(TIME_INTERVALS))), TrailingTotalReturnsResult.class),
        period(CalculationMetric.STANDARD_DEVIATION, init(new StandardDeviationResult(), r -> r.setStandardDeviation(
            TIME_INTERVALS)),
            StandardDeviationResult.class),
        period(CalculationMetric.SHARPE_RATIO, init(new SharpeRatioResult(), r -> r.setSharpeRatio(TIME_INTERVALS)),
            SharpeRatioResult.class),
        period(CalculationMetric.MAX_DRAWDOWN, init(new MaxDrawdownResult(), r -> r.setMaxDrawdown(List.of())),
            MaxDrawdownResult.class),
        breakdown(CalculationMetric.ASSET_ALLOCATIONS, init(new AssetAllocationResult(), r -> r.setAssetAllocation(Map
            .of(
                AssetAllocationRegionType.CASH, BigDecimal.valueOf(45.5)))), AssetAllocationResult.class),
        breakdown(CalculationMetric.ASSET_ALLOCATIONS_EM, init(new AssetAllocationEMResult(), r -> r
            .setAssetAllocationEmergingMarkets(Map.of(AssetAllocationRegionType.CASH, BigDecimal.valueOf(12.3)))),
            AssetAllocationEMResult.class),
        breakdown(CalculationMetric.EQUITY_SECTOR, init(new EquitySectorResult(), r -> r.setEquitySector(Map.of(
            EquitySectorAllocationType.TECHNOLOGY, BigDecimal.valueOf(30.0)))), EquitySectorResult.class),
        breakdown(CalculationMetric.SECTOR_EXPOSURE, init(new ConsolidatedSectorExposureResult(),
            r -> r.setSectorExposure(Map.of(SectorAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(0.25)))),
            ConsolidatedSectorExposureResult.class),
        breakdown(CalculationMetric.EQUITY_COUNTRY_EXPOSURE, init(new EquityCountryExposureResult(), r -> r
            .setEquityCountryExposure(Map.of(CountryRegionType.CANADA, BigDecimal.valueOf(60.0)))),
            EquityCountryExposureResult.class),
        breakdown(CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE, init(new EquityGeographicExposureResult(), r -> r
            .setGeographicExposure(Map.of(GeographicRegionType.OTHER, BigDecimal.valueOf(70.0)))),
            GeographicExposureResult.class),
        breakdown(CalculationMetric.GEOGRAPHIC_EXPOSURE, init(new ConsolidatedGeographicExposureResult(), r -> r
            .setGeographicExposure(Map.of(GeographicRegionType.CANADA, BigDecimal.valueOf(45.0)))),
            GeographicExposureResult.class),
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
    cmd.setPeriods(Set.of(TimePeriod.ONE_YR, TimePeriod.THREE_YR));
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

  private static AnnualReturnResult annualReturnResult() {
    AnnualReturnResult result = new AnnualReturnResult();
    result.setPerformanceEndDate(PED);
    result.setPerformanceStartDate(PSD);
    return result;
  }

  private static Growth10KResult growth10kResult() {
    return Growth10KResult.builder()
        .growth10k(List.of())
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
