package com.fintex.ce.adapter.rest.batch;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.dto.command.BatchCalculationCommand;
import com.fintex.ce.model.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.model.dto.command.IncomeForecastCommand;
import com.fintex.ce.model.dto.command.LeadingTotalReturnCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.dto.command.YieldCommand;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BatchCommandFactoryTest {

  private BatchCommandFactory factory;
  private BatchCalculationCommand batch;

  private static final List<PortfolioHolding> HOLDINGS = List.of(holding("XBAL"));
  private static final List<PortfolioHolding> BENCHMARK = List.of(holding("XIU"));
  private static final List<DataProvider> PROVIDERS = List.of(DataProvider.MORNINGSTAR);

  @BeforeEach
  void setUp() {
    factory = new BatchCommandFactory();

    batch = BatchCalculationCommand.builder()
        .metrics(List.of(CalculationMetric.TRAILING_TOTAL_RETURNS))
        .holdings(HOLDINGS)
        .benchmarkHoldings(BENCHMARK)
        .currency(Currency.CAD)
        .dataProviders(PROVIDERS)
        .periods(Set.of("12", "36"))
        .customIntervalPsd(LocalDate.of(2023, 1, 31))
        .customPed(LocalDate.of(2024, 12, 31))
        .customPsd(LocalDate.of(2022, 1, 31))
        .rollingPeriods(Set.of("12", "36"))
        .forecastTimeIntervalPeriods(24)
        .bestWorstTimeIntervalPeriods(Set.of(12L, 60L))
        .customNumberOfBins(10)
        .numOfTopCommonHoldings(5)
        .build();
  }

  @ParameterizedTest
  @EnumSource(value = CalculationMetric.class, names = {"TRAILING_TOTAL_RETURNS", "STANDARD_DEVIATION", "SHARPE_RATIO",
      "ALPHA",
      "BETA", "INFORMATION_RATIO", "TRACKING_ERROR", "UPSIDE_CAPTURE", "DOWNSIDE_CAPTURE"})
  void shouldBuildPeriodCommand_forPeriodBasedMetrics(CalculationMetric metric) {
    var cmd = factory.buildCommand(metric, batch);

    assertThat(cmd).isInstanceOf(PeriodCommand.class);
    PeriodCommand period = (PeriodCommand) cmd;
    assertThat(period.getMetric()).isEqualTo(metric);
    assertThat(period.getHoldings()).isSameAs(HOLDINGS);
    assertThat(period.getBenchmarkHoldings()).isSameAs(BENCHMARK);
    assertThat(period.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(period.getPeriods()).containsExactlyInAnyOrder("12", "36");
    assertThat(period.getCustomIntervalPsd()).isEqualTo(LocalDate.of(2023, 1, 31));
    assertThat(period.getCustomPed()).isEqualTo(LocalDate.of(2024, 12, 31));
  }

  @ParameterizedTest
  @EnumSource(value = CalculationMetric.class, names = {"ROLLING_TOTAL_RETURNS", "ROLLING_STANDARD_DEVIATION",
      "ROLLING_SHARPE_RATIO"})
  void shouldBuildRollingCommand_forRollingMetrics(CalculationMetric metric) {
    var cmd = factory.buildCommand(metric, batch);

    assertThat(cmd).isInstanceOf(RollingCalculationCommand.class);
    RollingCalculationCommand rolling = (RollingCalculationCommand) cmd;
    assertThat(rolling.getMetric()).isEqualTo(metric);
    assertThat(rolling.getHoldings()).isSameAs(HOLDINGS);
    assertThat(rolling.getBenchmarkHoldings()).isSameAs(BENCHMARK);
    assertThat(rolling.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(rolling.getRollingPeriods()).containsExactlyInAnyOrder("12", "36");
    assertThat(rolling.getCustomPsd()).isEqualTo(LocalDate.of(2022, 1, 31));
  }

  @ParameterizedTest
  @EnumSource(value = CalculationMetric.class, names = {"ANNUAL_RETURNS", "GROWTH_OF_10K"})
  void shouldBuildReturnCommand_forReturnMetrics(CalculationMetric metric) {
    var cmd = factory.buildCommand(metric, batch);

    assertThat(cmd).isInstanceOf(ReturnCommand.class);
    ReturnCommand ret = (ReturnCommand) cmd;
    assertThat(ret.getMetric()).isEqualTo(metric);
    assertThat(ret.getHoldings()).isSameAs(HOLDINGS);
    assertThat(ret.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(ret.getCustomPsd()).isEqualTo(LocalDate.of(2022, 1, 31));
    assertThat(ret.getCustomPed()).isEqualTo(LocalDate.of(2024, 12, 31));
  }

  @ParameterizedTest
  @EnumSource(value = CalculationMetric.class, names = {"ASSET_ALLOCATIONS", "ASSET_ALLOCATIONS_EM", "EQUITY_SECTOR",
      "EQUITY_COUNTRY_EXPOSURE", "FIXED_INCOME_COUNTRY_EXPOSURE", "FIXED_INCOME_BOND_SECTOR",
      "MATURITY_ALLOCATION", "CLASSIFICATION_ALLOCATION", "FIXED_INCOME_CREDIT_QUALITY",
      "NUMBER_OF_UNIQUE_HOLDINGS"})
  void shouldBuildHoldingsCommand_forAllocationAndExposureMetrics(CalculationMetric metric) {
    var cmd = factory.buildCommand(metric, batch);

    assertThat(cmd).isInstanceOf(PortfolioHoldingsCommand.class);
    PortfolioHoldingsCommand holdings = (PortfolioHoldingsCommand) cmd;
    assertThat(holdings.getMetric()).isEqualTo(metric);
    assertThat(holdings.getHoldings()).isSameAs(HOLDINGS);
    assertThat(holdings.getDataProviders()).isSameAs(PROVIDERS);
  }

  @ParameterizedTest
  @EnumSource(value = CalculationMetric.class, names = {"MER", "MANAGEMENT_FEE", "FEES"})
  void shouldBuildAverageMerCommand_forFeeMetrics(CalculationMetric metric) {
    var cmd = factory.buildCommand(metric, batch);

    assertThat(cmd).isInstanceOf(AverageMerCommand.class);
    AverageMerCommand mer = (AverageMerCommand) cmd;
    assertThat(mer.getMetric()).isEqualTo(metric);
    assertThat(mer.getHoldings()).isSameAs(HOLDINGS);
    assertThat(mer.getDataProviders()).isSameAs(PROVIDERS);
  }

  @Test
  void shouldBuildBestWorstPeriodsCommand_withAllFields() {
    var cmd = factory.buildCommand(CalculationMetric.BEST_WORST_PERIODS, batch);

    assertThat(cmd).isInstanceOf(BestWorstPeriodsCommand.class);
    BestWorstPeriodsCommand bwp = (BestWorstPeriodsCommand) cmd;
    assertThat(bwp.getMetric()).isEqualTo(CalculationMetric.BEST_WORST_PERIODS);
    assertThat(bwp.getHoldings()).isSameAs(HOLDINGS);
    assertThat(bwp.getBenchmarkHoldings()).isSameAs(BENCHMARK);
    assertThat(bwp.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(bwp.getCustomPsd()).isEqualTo(LocalDate.of(2022, 1, 31));
    assertThat(bwp.getCustomPed()).isEqualTo(LocalDate.of(2024, 12, 31));
    assertThat(bwp.getBestWorstTimeIntervalPeriods()).containsExactlyInAnyOrderElementsOf(List.of(12L, 60L));
  }

  @Test
  void shouldBuildLeadingTotalReturnCommand_withCustomPsd() {
    var cmd = factory.buildCommand(CalculationMetric.LEADING_TOTAL_RETURNS, batch);

    assertThat(cmd).isInstanceOf(LeadingTotalReturnCommand.class);
    LeadingTotalReturnCommand ltrc = (LeadingTotalReturnCommand) cmd;
    assertThat(ltrc.getMetric()).isEqualTo(CalculationMetric.LEADING_TOTAL_RETURNS);
    assertThat(ltrc.getHoldings()).isSameAs(HOLDINGS);
    assertThat(ltrc.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(ltrc.getPeriods()).containsExactlyInAnyOrder("12", "36");
    assertThat(ltrc.getCustomPsd()).isEqualTo(LocalDate.of(2022, 1, 31));
  }

  @Test
  void shouldBuildDistributionOfReturnsCommand_withBinCount() {
    var cmd = factory.buildCommand(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS, batch);

    assertThat(cmd).isInstanceOf(DistributionOfReturnsCommand.class);
    DistributionOfReturnsCommand dist = (DistributionOfReturnsCommand) cmd;
    assertThat(dist.getMetric()).isEqualTo(CalculationMetric.DISTRIBUTION_OF_MONTHLY_RETURNS);
    assertThat(dist.getHoldings()).isSameAs(HOLDINGS);
    assertThat(dist.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(dist.getPeriods()).containsExactlyInAnyOrder("12", "36");
    assertThat(dist.getCustomPsd()).isEqualTo(LocalDate.of(2022, 1, 31));
    assertThat(dist.getCustomNumberOfBins()).isEqualTo(10);
  }

  @Test
  void shouldBuildIncomeForecastCommand_withTimeIntervalPeriods() {
    var cmd = factory.buildCommand(CalculationMetric.INCOME_FORECAST, batch);

    assertThat(cmd).isInstanceOf(IncomeForecastCommand.class);
    IncomeForecastCommand income = (IncomeForecastCommand) cmd;
    assertThat(income.getMetric()).isEqualTo(CalculationMetric.INCOME_FORECAST);
    assertThat(income.getHoldings()).isSameAs(HOLDINGS);
    assertThat(income.getTimeIntervalPeriods()).isEqualTo(24);
  }

  @Test
  void shouldBuildYieldCommand_withTimeIntervalPeriods() {
    var cmd = factory.buildCommand(CalculationMetric.YIELD, batch);

    assertThat(cmd).isInstanceOf(YieldCommand.class);
    YieldCommand command = (YieldCommand) cmd;
    assertThat(command.getMetric()).isEqualTo(CalculationMetric.YIELD);
    assertThat(command.getHoldings()).isSameAs(HOLDINGS);
    assertThat(command.getTimeIntervalPeriods()).isEqualTo(24);
  }

  @Test
  void shouldBuildTopCommonHoldingsCommand_withCountAndTypes() {
    var cmd = factory.buildCommand(CalculationMetric.TOP_COMMON_HOLDINGS, batch);

    assertThat(cmd).isInstanceOf(TopCommonHoldingsCommand.class);
    TopCommonHoldingsCommand tch = (TopCommonHoldingsCommand) cmd;
    assertThat(tch.getMetric()).isEqualTo(CalculationMetric.TOP_COMMON_HOLDINGS);
    assertThat(tch.getHoldings()).isSameAs(HOLDINGS);
    assertThat(tch.getNumOfTopCommonHoldings()).isEqualTo(5);
  }

  @Test
  void shouldThrowBasePceException_whenCommonPerformanceDatesRequested() {
    assertThatThrownBy(() -> factory.buildCommand(CalculationMetric.COMMON_PERFORMANCE_DATES, batch))
        .isInstanceOf(BasePceException.class);
  }

  private static PortfolioHolding holding(String ticker) {
    var identifier = new SecurityIdentifier();
    identifier.setId(ticker);
    identifier.setIdType(FiIdentifierType.TICKER);
    return new PortfolioHolding(BigDecimal.valueOf(10_000), FinancialInstrumentType.ETF_CANADA, identifier);
  }
}
