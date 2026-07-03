package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.DownsideDeviationCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.application.util.TBillsValidator;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.DownsideDeviationResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class DownsideDeviationCalculationServiceImpl
    extends
      WeightedAverageWithCpedAbstractService<PeriodCommand, DownsideDeviationResult> {

  private final TreasuryBillsFetcher treasuryBillsFetcher;

  public DownsideDeviationCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      @Autowired final TreasuryBillsFetcher treasuryBillsFetcher,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, defaultPeriods);
    this.treasuryBillsFetcher = treasuryBillsFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.DOWNSIDE_DEVIATION;
  }

  public DownsideDeviationCalculation<DownsideDeviationResult> defineCalculationMethod(final PeriodCommand command) {
    final PeriodCalculationInput context = buildPeriodCalculationInput(command, ReturnFactorScale.SCALE_OF_ONE);
    final var tBills = TBillsValidator.requireNonEmpty(
        treasuryBillsFetcher.fetch(command.getCurrency()), command.getCurrency());
    return new DownsideDeviationCalculation<>(context, defaultPeriods, tBills);
  }
}
