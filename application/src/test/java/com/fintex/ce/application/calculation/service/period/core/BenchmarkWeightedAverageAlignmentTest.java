package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.FxContext;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.ReturnsRole;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.returns.pipeline.CpedScaleParams;
import com.fintex.ce.application.returns.pipeline.CpsdCpedScaleParams;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BenchmarkWeightedAverageAlignmentTest {

  private static final LocalDate JAN_2020 = LocalDate.parse("2020-01-31");
  private static final LocalDate FEB_2020 = LocalDate.parse("2020-02-29");
  private static final LocalDate MAR_2020 = LocalDate.parse("2020-03-31");
  private static final LocalDate APR_2020 = LocalDate.parse("2020-04-30");
  private static final LocalDate MAY_2020 = LocalDate.parse("2020-05-31");
  private static final PortfolioHolding PORTFOLIO_HOLDING = holding("PORTFOLIO");
  private static final PortfolioHolding BENCHMARK_HOLDING = holding("BENCHMARK");

  @Test
  void shouldAlignPortfolioAndBenchmarkToCommonWindow_whenBuildingCpedBenchmarkInput() {
    PortfolioMonthlyReturnsContextProvider portfolioProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    BenchmarkMonthlyReturnsContextProvider benchmarkProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
    PortfolioWeightedAverageWithCpedPipeline portfolioPipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    BenchmarkWeightedAverageWithCpedPipeline benchmarkPipeline = mock(BenchmarkWeightedAverageWithCpedPipeline.class);
    CpedTestService service = new CpedTestService(portfolioProvider, benchmarkProvider, portfolioPipeline,
        benchmarkPipeline);
    PeriodCommand command = periodCommand();
    PortfolioBenchmarkReturns returnsData = new PortfolioBenchmarkReturns(Map.of(), Map.of());

    when(portfolioProvider.get(command.getHoldings(), command.getCurrency(), returnsData.portfolio()))
        .thenReturn(context(ReturnsRole.PORTFOLIO, PORTFOLIO_HOLDING, JAN_2020, FEB_2020, MAR_2020, APR_2020));
    when(benchmarkProvider.get(command.getBenchmarkHoldings(), command.getCurrency(), returnsData.benchmark()))
        .thenReturn(context(ReturnsRole.BENCHMARK, BENCHMARK_HOLDING, FEB_2020, MAR_2020, APR_2020, MAY_2020));
    when(portfolioPipeline.run(any(), any())).thenReturn(weightedAverageResult(FEB_2020, MAR_2020, APR_2020));
    when(benchmarkPipeline.run(any(), any())).thenReturn(weightedAverageResult(FEB_2020, MAR_2020, APR_2020));

    BenchmarkPeriodCalculationInput result = service.buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO, returnsData);

    ArgumentCaptor<MonthlyReturnsContext<HoldingMonthlyReturns>> portfolioContextCaptor = monthlyContextCaptor();
    ArgumentCaptor<MonthlyReturnsContext<HoldingMonthlyReturns>> benchmarkContextCaptor = monthlyContextCaptor();
    ArgumentCaptor<CpedScaleParams> portfolioParamsCaptor = ArgumentCaptor.forClass(CpedScaleParams.class);
    ArgumentCaptor<CpedScaleParams> benchmarkParamsCaptor = ArgumentCaptor.forClass(CpedScaleParams.class);
    verify(portfolioPipeline).run(portfolioContextCaptor.capture(), portfolioParamsCaptor.capture());
    verify(benchmarkPipeline).run(benchmarkContextCaptor.capture(), benchmarkParamsCaptor.capture());

    assertContextWindow(portfolioContextCaptor.getValue(), PORTFOLIO_HOLDING, FEB_2020, MAR_2020, APR_2020);
    assertContextWindow(benchmarkContextCaptor.getValue(), BENCHMARK_HOLDING, FEB_2020, MAR_2020, APR_2020);
    assertThat(portfolioParamsCaptor.getValue()).isEqualTo(new CpedScaleParams(command.getCustomPed(),
        ReturnFactorScale.SCALE_OF_TWO));
    assertThat(benchmarkParamsCaptor.getValue()).isEqualTo(new CpedScaleParams(command.getCustomPed(),
        ReturnFactorScale.SCALE_OF_TWO));
    assertThat(result.getWeightedAveragePortfolioReturns()).containsOnlyKeys(FEB_2020, MAR_2020, APR_2020);
    assertThat(result.getWeightedAveragePortfolioReturns().values()).containsOnly(BigDecimal.ONE);
    assertThat(result.getWeightedAverageBenchmarkReturns()).containsOnlyKeys(FEB_2020, MAR_2020, APR_2020);
    assertThat(result.getWeightedAverageBenchmarkReturns().values()).containsOnly(BigDecimal.ONE);
    assertThat(result.getCipsd()).isEqualTo(command.getCustomIntervalPsd());
  }

  @Test
  void shouldAlignPortfolioAndBenchmarkToCommonWindow_whenBuildingCpsdAndCpedBenchmarkInput() {
    PortfolioMonthlyReturnsContextProvider portfolioProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    BenchmarkMonthlyReturnsContextProvider benchmarkProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
    PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioPipeline = mock(
        PortfolioWeightedAverageWithCpsdAndCpedPipeline.class);
    BenchmarkWeightedAverageWithCpsdAndCpedPipeline benchmarkPipeline = mock(
        BenchmarkWeightedAverageWithCpsdAndCpedPipeline.class);
    CpsdCpedTestService service = new CpsdCpedTestService(portfolioProvider, benchmarkProvider, portfolioPipeline,
        benchmarkPipeline);
    RollingCalculationCommand command = rollingCommand();
    PortfolioBenchmarkReturns returnsData = new PortfolioBenchmarkReturns(Map.of(), Map.of());

    when(portfolioProvider.get(command.getHoldings(), command.getCurrency(), returnsData.portfolio()))
        .thenReturn(context(ReturnsRole.PORTFOLIO, PORTFOLIO_HOLDING, JAN_2020, FEB_2020, MAR_2020, APR_2020));
    when(benchmarkProvider.get(command.getBenchmarkHoldings(), command.getCurrency(), returnsData.benchmark()))
        .thenReturn(context(ReturnsRole.BENCHMARK, BENCHMARK_HOLDING, FEB_2020, MAR_2020, APR_2020, MAY_2020));
    when(portfolioPipeline.run(any(), any())).thenReturn(weightedAverageResult(MAR_2020, APR_2020));
    when(benchmarkPipeline.run(any(), any())).thenReturn(weightedAverageResult(MAR_2020, APR_2020));

    BenchmarkPeriodCalculationInput result = service.buildBenchmarkInput(command, ReturnFactorScale.AS_IS, returnsData);

    ArgumentCaptor<MonthlyReturnsContext<HoldingMonthlyReturns>> portfolioContextCaptor = monthlyContextCaptor();
    ArgumentCaptor<MonthlyReturnsContext<HoldingMonthlyReturns>> benchmarkContextCaptor = monthlyContextCaptor();
    ArgumentCaptor<CpsdCpedScaleParams> portfolioParamsCaptor = ArgumentCaptor.forClass(CpsdCpedScaleParams.class);
    ArgumentCaptor<CpsdCpedScaleParams> benchmarkParamsCaptor = ArgumentCaptor.forClass(CpsdCpedScaleParams.class);
    verify(portfolioPipeline).run(portfolioContextCaptor.capture(), portfolioParamsCaptor.capture());
    verify(benchmarkPipeline).run(benchmarkContextCaptor.capture(), benchmarkParamsCaptor.capture());

    assertContextWindow(portfolioContextCaptor.getValue(), PORTFOLIO_HOLDING, FEB_2020, MAR_2020, APR_2020);
    assertContextWindow(benchmarkContextCaptor.getValue(), BENCHMARK_HOLDING, FEB_2020, MAR_2020, APR_2020);
    assertThat(portfolioParamsCaptor.getValue()).isEqualTo(new CpsdCpedScaleParams(command.getCustomPsd(),
        command.getCustomPed(), ReturnFactorScale.AS_IS));
    assertThat(benchmarkParamsCaptor.getValue()).isEqualTo(new CpsdCpedScaleParams(command.getCustomPsd(),
        command.getCustomPed(), ReturnFactorScale.AS_IS));
    assertThat(result.getWeightedAveragePortfolioReturns()).containsOnlyKeys(MAR_2020, APR_2020);
    assertThat(result.getWeightedAveragePortfolioReturns().values()).containsOnly(BigDecimal.ONE);
    assertThat(result.getWeightedAverageBenchmarkReturns()).containsOnlyKeys(MAR_2020, APR_2020);
    assertThat(result.getWeightedAverageBenchmarkReturns().values()).containsOnly(BigDecimal.ONE);
    assertThat(result.getCipsd()).isEqualTo(command.getCustomIntervalPsd());
  }

  @Test
  void shouldCapBenchmarkCped_whenRequestedCpedIsAfterPortfolioPed() {
    PortfolioMonthlyReturnsContextProvider portfolioProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    BenchmarkMonthlyReturnsContextProvider benchmarkProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
    PortfolioWeightedAverageWithCpedPipeline portfolioPipeline = mock(PortfolioWeightedAverageWithCpedPipeline.class);
    BenchmarkWeightedAverageWithCpedPipeline benchmarkPipeline = mock(BenchmarkWeightedAverageWithCpedPipeline.class);
    CpedTestService service = new CpedTestService(portfolioProvider, benchmarkProvider, portfolioPipeline,
        benchmarkPipeline);
    PeriodCommand command = periodCommand();
    command.setCustomPed(MAY_2020);
    PortfolioBenchmarkReturns returnsData = new PortfolioBenchmarkReturns(Map.of(), Map.of());

    when(portfolioProvider.get(command.getHoldings(), command.getCurrency(), returnsData.portfolio()))
        .thenReturn(context(ReturnsRole.PORTFOLIO, PORTFOLIO_HOLDING, JAN_2020, FEB_2020, MAR_2020, APR_2020));
    when(benchmarkProvider.get(command.getBenchmarkHoldings(), command.getCurrency(), returnsData.benchmark()))
        .thenReturn(context(ReturnsRole.BENCHMARK, BENCHMARK_HOLDING, FEB_2020, MAR_2020, APR_2020, MAY_2020));
    when(portfolioPipeline.run(any(), any())).thenReturn(weightedAverageResult(FEB_2020, MAR_2020, APR_2020));
    when(benchmarkPipeline.run(any(), any())).thenReturn(weightedAverageResult(FEB_2020, MAR_2020, APR_2020));

    BenchmarkPeriodCalculationInput result = service.buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO,
        returnsData);

    ArgumentCaptor<CpedScaleParams> portfolioParamsCaptor = ArgumentCaptor.forClass(CpedScaleParams.class);
    ArgumentCaptor<CpedScaleParams> benchmarkParamsCaptor = ArgumentCaptor.forClass(CpedScaleParams.class);
    verify(portfolioPipeline).run(any(), portfolioParamsCaptor.capture());
    verify(benchmarkPipeline).run(any(), benchmarkParamsCaptor.capture());

    assertThat(portfolioParamsCaptor.getValue()).isEqualTo(new CpedScaleParams(MAY_2020,
        ReturnFactorScale.SCALE_OF_TWO));
    assertThat(benchmarkParamsCaptor.getValue()).isEqualTo(new CpedScaleParams(APR_2020,
        ReturnFactorScale.SCALE_OF_TWO));
    assertThat(result.getWeightedAveragePortfolioReturns()).containsOnlyKeys(FEB_2020, MAR_2020, APR_2020);
    assertThat(result.getWeightedAverageBenchmarkReturns()).containsOnlyKeys(FEB_2020, MAR_2020, APR_2020);
  }

  @Test
  void shouldCapBenchmarkCpedForCpsdPipeline_whenRequestedCpedIsAfterPortfolioPed() {
    PortfolioMonthlyReturnsContextProvider portfolioProvider = mock(PortfolioMonthlyReturnsContextProvider.class);
    BenchmarkMonthlyReturnsContextProvider benchmarkProvider = mock(BenchmarkMonthlyReturnsContextProvider.class);
    PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioPipeline = mock(
        PortfolioWeightedAverageWithCpsdAndCpedPipeline.class);
    BenchmarkWeightedAverageWithCpsdAndCpedPipeline benchmarkPipeline = mock(
        BenchmarkWeightedAverageWithCpsdAndCpedPipeline.class);
    CpsdCpedTestService service = new CpsdCpedTestService(portfolioProvider, benchmarkProvider, portfolioPipeline,
        benchmarkPipeline);
    RollingCalculationCommand command = rollingCommand();
    command.setCustomPed(MAY_2020);
    PortfolioBenchmarkReturns returnsData = new PortfolioBenchmarkReturns(Map.of(), Map.of());

    when(portfolioProvider.get(command.getHoldings(), command.getCurrency(), returnsData.portfolio()))
        .thenReturn(context(ReturnsRole.PORTFOLIO, PORTFOLIO_HOLDING, JAN_2020, FEB_2020, MAR_2020, APR_2020));
    when(benchmarkProvider.get(command.getBenchmarkHoldings(), command.getCurrency(), returnsData.benchmark()))
        .thenReturn(context(ReturnsRole.BENCHMARK, BENCHMARK_HOLDING, FEB_2020, MAR_2020, APR_2020, MAY_2020));
    when(portfolioPipeline.run(any(), any())).thenReturn(weightedAverageResult(MAR_2020, APR_2020));
    when(benchmarkPipeline.run(any(), any())).thenReturn(weightedAverageResult(MAR_2020, APR_2020));

    BenchmarkPeriodCalculationInput result = service.buildBenchmarkInput(command, ReturnFactorScale.AS_IS, returnsData);

    ArgumentCaptor<CpsdCpedScaleParams> portfolioParamsCaptor = ArgumentCaptor.forClass(CpsdCpedScaleParams.class);
    ArgumentCaptor<CpsdCpedScaleParams> benchmarkParamsCaptor = ArgumentCaptor.forClass(CpsdCpedScaleParams.class);
    verify(portfolioPipeline).run(any(), portfolioParamsCaptor.capture());
    verify(benchmarkPipeline).run(any(), benchmarkParamsCaptor.capture());

    assertThat(portfolioParamsCaptor.getValue()).isEqualTo(new CpsdCpedScaleParams(MAR_2020, MAY_2020,
        ReturnFactorScale.AS_IS));
    assertThat(benchmarkParamsCaptor.getValue()).isEqualTo(new CpsdCpedScaleParams(MAR_2020, APR_2020,
        ReturnFactorScale.AS_IS));
    assertThat(result.getWeightedAveragePortfolioReturns()).containsOnlyKeys(MAR_2020, APR_2020);
    assertThat(result.getWeightedAverageBenchmarkReturns()).containsOnlyKeys(MAR_2020, APR_2020);
  }

  private static PeriodCommand periodCommand() {
    PeriodCommand command = new PeriodCommand();
    command.setHoldings(List.of(PORTFOLIO_HOLDING));
    command.setBenchmarkHoldings(List.of(BENCHMARK_HOLDING));
    command.setCurrency(Currency.CAD);
    command.setCustomPed(APR_2020);
    command.setCustomIntervalPsd(FEB_2020);
    return command;
  }

  private static RollingCalculationCommand rollingCommand() {
    RollingCalculationCommand command = new RollingCalculationCommand();
    command.setHoldings(List.of(PORTFOLIO_HOLDING));
    command.setBenchmarkHoldings(List.of(BENCHMARK_HOLDING));
    command.setCurrency(Currency.CAD);
    command.setCustomPsd(MAR_2020);
    command.setCustomPed(APR_2020);
    command.setCustomIntervalPsd(FEB_2020);
    return command;
  }

  private static MonthlyReturnsContext<HoldingMonthlyReturns> context(ReturnsRole role, PortfolioHolding holding,
      LocalDate... dates) {
    TreeMap<LocalDate, BigDecimal> series = series(dates);
    ReturnsSnapshot<HoldingMonthlyReturns> snapshot = new ReturnsSnapshot<>(Map.of(), Map.of(holding, series),
        series.firstKey(), series.lastKey(), List.of());
    return new MonthlyReturnsContext<>(snapshot, FxContext.empty(), role);
  }

  private static WeightedAverageResult<HoldingMonthlyReturns> weightedAverageResult(LocalDate... dates) {
    return new WeightedAverageResult<>(series(dates), ReturnsSnapshot.empty());
  }

  private static void assertContextWindow(MonthlyReturnsContext<HoldingMonthlyReturns> context,
      PortfolioHolding holding, LocalDate... expectedDates) {
    assertThat(context.snapshot().performanceStartDate()).isEqualTo(expectedDates[0]);
    assertThat(context.snapshot().performanceEndDate()).isEqualTo(expectedDates[expectedDates.length - 1]);
    assertThat(context.snapshot().returnsMap()).containsOnlyKeys(holding);
    assertThat(context.snapshot().returnsMap().get(holding)).containsOnlyKeys(expectedDates);
    assertThat(context.snapshot().returnsMap().get(holding).values()).containsOnly(BigDecimal.ONE);
  }

  private static TreeMap<LocalDate, BigDecimal> series(LocalDate... dates) {
    return Arrays.stream(dates)
        .collect(Collectors.toMap(Function.identity(), date -> BigDecimal.ONE, (left, right) -> left, TreeMap::new));
  }

  private static PortfolioHolding holding(String id) {
    return new PortfolioHolding(BigDecimal.TEN, FinancialInstrumentType.ETF, Country.USA,
        new SecurityIdentifier(id, FiIdentifierType.TICKER));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static ArgumentCaptor<MonthlyReturnsContext<HoldingMonthlyReturns>> monthlyContextCaptor() {
    return ArgumentCaptor.forClass((Class) MonthlyReturnsContext.class);
  }

  private static final class CpedTestService
      extends
        BenchmarkWeightedAverageWithCpedAbstractService<PeriodCommand, PeriodResult> {

    private CpedTestService(PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
        BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider,
        PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
        BenchmarkWeightedAverageWithCpedPipeline benchmarkWeightedAverageWithCped) {
      super(portfolioMonthlyReturnsContextProvider, benchmarkMonthlyReturnsContextProvider,
          portfolioWeightedAverageWithCped, benchmarkWeightedAverageWithCped, Set.of(ONE_YR));
    }

    @Override
    public PeriodResult perform(PeriodCommand command, PortfolioBenchmarkReturns returnsData) {
      return new PeriodResult();
    }

    @Override
    public CalculationMetric getMetric() {
      return CalculationMetric.INFORMATION_RATIO;
    }
  }

  private static final class CpsdCpedTestService
      extends
        BenchmarkWeightedAverageWithCpsdAndCpedAbstractService<RollingCalculationCommand, PeriodResult> {

    private CpsdCpedTestService(PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
        BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider,
        PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
        BenchmarkWeightedAverageWithCpsdAndCpedPipeline benchmarkWeightedAverageWithCpsdAndCped) {
      super(portfolioMonthlyReturnsContextProvider, benchmarkMonthlyReturnsContextProvider,
          portfolioWeightedAverageWithCpsdAndCped, benchmarkWeightedAverageWithCpsdAndCped);
    }

    @Override
    public PeriodResult perform(RollingCalculationCommand command, PortfolioBenchmarkReturns returnsData) {
      return new PeriodResult();
    }

    @Override
    public CalculationMetric getMetric() {
      return CalculationMetric.ROLLING_CORRELATION;
    }
  }
}
