package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.LeadingTotalReturnsCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpsdAndCpedAbstractService;
import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.CpsdCpedScaleParams;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.returns.LeadingTotalReturnsResult;
import com.fintex.ce.model.dto.command.LeadingTotalReturnCommand;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class LeadingTotalReturnsCalculationServiceImpl
    extends
      WeightedAverageWithCpsdAndCpedAbstractService<LeadingTotalReturnCommand, LeadingTotalReturnsResult> {

  private final Set<String> defaultPeriods;

  public LeadingTotalReturnsCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped,
      @Value("#{'${default.periods.leading-total-returns}'.split(',')}") Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCpsdAndCped);
    this.defaultPeriods = defaultPeriods;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.LEADING_TOTAL_RETURNS;
  }

  @Override
  public LeadingTotalReturnsResult perform(LeadingTotalReturnCommand command,
      PortfolioBenchmarkReturns returnsData) {
    // Leading-returns is open-ended forward: it intentionally ignores the command's CPED and passes null to the
    // pipeline so no end-date trim is applied. That's why we don't reuse the inherited buildPeriodCalculationInput.
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = portfolioMonthlyReturnsContextProvider.get(
        command.getHoldings(), command.getCurrency(), returnsData.portfolio());
    WeightedAverageResult<HoldingMonthlyReturns> result = portfolioWeightedAverageWithCpsdAndCped.run(portfolioContext,
        new CpsdCpedScaleParams(command.getCustomPsd(), null, ReturnFactorScale.SCALE_OF_TWO));
    PeriodCalculationInput input = new PeriodCalculationInput(result.weightedAverage());
    return new LeadingTotalReturnsCalculation(input, defaultPeriods).calculate(command.getPeriods());
  }
}