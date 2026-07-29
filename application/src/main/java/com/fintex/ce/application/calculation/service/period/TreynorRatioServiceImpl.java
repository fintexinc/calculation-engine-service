package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.BetaCalculation;
import com.fintex.ce.application.calculation.metric.TreynorRatioCalculation;
import com.fintex.ce.application.calculation.service.period.core.BenchmarkWeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.TreynorRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

import static com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract.calculateExcessReturn;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class TreynorRatioServiceImpl
    extends
      BenchmarkWeightedAverageWithCpedAbstractService<PeriodCommand, TreynorRatioResult> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;

  public TreynorRatioServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      BenchmarkWeightedAverageWithCpedPipeline benchmarkWeightedAverageWithCped,
      TreasuryBillsFetcher treasuryBillsFetcher,
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, benchmarkMonthlyReturnsContextProvider,
        portfolioWeightedAverageWithCped, benchmarkWeightedAverageWithCped, periods.getRiskCalculations());
    this.treasuryBillsFetcher = treasuryBillsFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TREYNOR_RATIO;
  }

  @Override
  public TreynorRatioResult perform(PeriodCommand command,
      PortfolioBenchmarkReturns returnsData) {
    BenchmarkPeriodCalculationInput betaInput = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO, returnsData);
    BenchmarkPeriodCalculationInput treynorRatioInput = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_ONE, returnsData);
    var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn = calculateExcessReturn(betaInput
        .getWeightedAveragePortfolioReturns(), tBills);
    NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn = calculateExcessReturn(betaInput
        .getWeightedAverageBenchmarkReturns(), tBills);
    var betaCalculation = new BetaCalculation(betaInput, defaultPeriods, portfolioExcessReturn,
        benchmarkExcessReturn);
    return new TreynorRatioCalculation(treynorRatioInput, defaultPeriods, tBills, betaCalculation)
        .calculate(command.getPeriods());
  }

}
