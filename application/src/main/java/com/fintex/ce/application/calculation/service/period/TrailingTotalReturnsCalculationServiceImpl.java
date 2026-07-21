package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.TrailingTotalReturnsResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TrailingTotalReturnsCalculationServiceImpl
    extends
      WeightedAverageWithCpedAbstractService<PeriodCommand, TrailingTotalReturnsResult> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;

  public TrailingTotalReturnsCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      TreasuryBillsFetcher treasuryBillsFetcher,
      @Value("#{'${default.periods.trailing-total-returns}'.split(',')}") final Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, defaultPeriods);
    this.treasuryBillsFetcher = treasuryBillsFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TRAILING_TOTAL_RETURNS;
  }

  @Override
  public TrailingTotalReturnsResult perform(PeriodCommand command,
      PortfolioBenchmarkReturns returnsData) {
    PeriodCalculationInput input = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_TWO,
        returnsData);
    var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    return TrailingTotalReturnsCalculation.withTBillPrecondition(input, defaultPeriods, tBills)
        .calculate(command.getPeriods());
  }

}
