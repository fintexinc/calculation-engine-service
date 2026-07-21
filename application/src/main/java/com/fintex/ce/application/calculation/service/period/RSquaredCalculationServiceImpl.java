package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.RSquaredCalculation;
import com.fintex.ce.application.calculation.service.period.core.BenchmarkWeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.BenchmarkMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.BenchmarkWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.RSquaredResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract.calculateExcessReturn;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class RSquaredCalculationServiceImpl
    extends
      BenchmarkWeightedAverageWithCpedAbstractService<PeriodCommand, RSquaredResult> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;

  public RSquaredCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      BenchmarkMonthlyReturnsContextProvider benchmarkMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      BenchmarkWeightedAverageWithCpedPipeline benchmarkWeightedAverageWithCped,
      TreasuryBillsFetcher treasuryBillsFetcher,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, benchmarkMonthlyReturnsContextProvider,
        portfolioWeightedAverageWithCped, benchmarkWeightedAverageWithCped, defaultPeriods);
    this.treasuryBillsFetcher = treasuryBillsFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.R_SQUARED;
  }

  @Override
  public RSquaredResult perform(final PeriodCommand command,
      final PortfolioBenchmarkReturns returnsData) {
    final BenchmarkPeriodCalculationInput context = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO, returnsData);
    final var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    final NavigableMap<LocalDate, BigDecimal> portfolioExccessReturn = calculateExcessReturn(context
        .getWeightedAveragePortfolioReturns(), tBills);
    final NavigableMap<LocalDate, BigDecimal> benchmarkExccessReturn = calculateExcessReturn(context
        .getWeightedAverageBenchmarkReturns(), tBills);
    return new RSquaredCalculation(context, defaultPeriods, portfolioExccessReturn, benchmarkExccessReturn)
        .calculate(command.getPeriods());
  }

}
