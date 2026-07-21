package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.CorrelationCalculation;
import com.fintex.ce.application.calculation.metric.RollingCorrelationCalculation;
import com.fintex.ce.application.calculation.service.period.core.BenchmarkWeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.returns.pipeline.CpedParams;
import com.fintex.ce.application.returns.pipeline.PortfolioValidateCutAndFxPipeline;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.rolling.RollingCorrelationResult;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class RollingCorrelationCalculationServiceImpl
    extends
      BenchmarkWeightedAverageWithCpsdAndCpedAbstractService<RollingCalculationCommand, RollingCorrelationResult> {

  private final PortfolioValidateCutAndFxPipeline portfolioValidateCutAndFx;
  private final Set<String> defaultPeriods;

  public RollingCorrelationCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      BenchmarkWeightedAverageWithCpsdAndCpedPipeline benchmarkWeightedAverageWithCpsdAndCped,
      PortfolioValidateCutAndFxPipeline portfolioValidateCutAndFx,
      @Value("#{'${default.periods.rolling-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, benchmarkMonthlyReturnsContextProvider,
        portfolioWeightedAverageWithCpsdAndCped, benchmarkWeightedAverageWithCpsdAndCped);
    this.portfolioValidateCutAndFx = portfolioValidateCutAndFx;
    this.defaultPeriods = defaultPeriods;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ROLLING_CORRELATION;
  }

  @Override
  public RollingCorrelationResult perform(RollingCalculationCommand command,
      PortfolioBenchmarkReturns returnsData) {
    BenchmarkPeriodCalculationInput context = buildBenchmarkInput(command, ReturnFactorScale.SCALE_OF_TWO,
        returnsData);
    Map<PortfolioHolding, Map<LocalDate, BigDecimal>> baseTotalReturn = getBaseTotalReturns(command, returnsData);
    CorrelationCalculation correlationCalculation = new CorrelationCalculation(context, baseTotalReturn,
        defaultPeriods);
    return new RollingCorrelationCalculation(context, defaultPeriods, correlationCalculation,
        context.getWeightedAverageBenchmarkReturns()).calculate(command.getRollingPeriods());
  }

  public Map<PortfolioHolding, Map<LocalDate, BigDecimal>> getBaseTotalReturns(RollingCalculationCommand command,
      PortfolioBenchmarkReturns returnsData) {
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = portfolioMonthlyReturnsContextProvider.get(
        command.getHoldings(), command.getCurrency(), returnsData.portfolioReturns());
    ReturnsSnapshot<HoldingMonthlyReturns> postFx = portfolioValidateCutAndFx.run(portfolioContext,
        new CpedParams(command.getCustomPed()));
    return new HashMap<>(postFx.returnsMap());
  }
}
