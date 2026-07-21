package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.StandardDeviationCalculation;
import com.fintex.ce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.risk.StandardDeviationResult;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static com.fintex.ce.model.util.BigDecimalConstants.OUTPUT_SCALE;

@Service
public class StandardDeviationCalculationServiceImpl
    extends
      WeightedAverageWithCpedAbstractService<PeriodCommand, StandardDeviationResult> {

  public StandardDeviationCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      @Value("#{'${default.periods.risk-calculations}'.split(',')}") final Set<String> defaultPeriods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, defaultPeriods);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.STANDARD_DEVIATION;
  }

  @Override
  public StandardDeviationResult perform(final PeriodCommand command,
      final PortfolioBenchmarkReturns returnsData) {
    var result = buildWeightedAverageResult(command, ReturnFactorScale.SCALE_OF_TWO, returnsData);
    result.snapshot().warnings().stream()
        .filter(w -> FX_RATES_UNAVAILABLE.getCode().equals(w.getCode()))
        .findFirst()
        .ifPresent(w -> {
          throw new CalculationException(ErrorCode.FX_RATES_UNAVAILABLE, w.getMetadata());
        });
    var context = new PeriodCalculationInput(command.getCustomIntervalPsd(), result.weightedAverage());
    return StandardDeviationCalculation.<StandardDeviationResult>builder()
        .input(context)
        .defaultPeriods(defaultPeriods)
        .scale(OUTPUT_SCALE)
        .build()
        .calculate(command.getPeriods());
  }
}
