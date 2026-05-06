package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.BetaCalculation;
import com.fintex.ce.application.calculation.metric.TreynorRatioCalculation;
import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.calculation.service.period.core.PeriodBenchmarkAbstractService;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.TreynorRatioResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

import static com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract.calculateExcessReturn;

@Service
public class TreynorRatioServiceImpl extends PeriodBenchmarkAbstractService<TreynorRatioResult, PeriodCommand> {

  private final TBillsFetcher tBillsProvider;

  public TreynorRatioServiceImpl(
      MonthlyReturnsService monthlyReturnsService,
      TBillsFetcher tBillsProvider,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") Set<String> defaultPeriods) {
    super(monthlyReturnsService, defaultPeriods);
    this.tBillsProvider = tBillsProvider;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TREYNOR_RATIO;
  }

  @Override
  public PeriodCalculationAbstract<TreynorRatioResult, ?> defineCalculationMethod(PeriodCommand command) {
    BenchmarkPeriodCalculationInput betaInput = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_TWO);
    BenchmarkPeriodCalculationInput treynorRatioInput = buildPeriodCalculationInput(command,
        ReturnFactorScale.SCALE_OF_ONE);
    var tBills = TBillsValidator.requireNonEmpty(

        tBillsProvider.fetch().get(command.getCurrency()), command.getCurrency());
    NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn = calculateExcessReturn(betaInput
        .getWeightedAveragePortfolioReturns(), tBills);
    NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn = calculateExcessReturn(betaInput
        .getWeightedAverageBenchmarkReturns(), tBills);
    var betaCalculation = new BetaCalculation(betaInput, defaultPeriods, portfolioExcessReturn,
        benchmarkExcessReturn);
    return new TreynorRatioCalculation(treynorRatioInput, defaultPeriods, tBills, betaCalculation);
  }

}
