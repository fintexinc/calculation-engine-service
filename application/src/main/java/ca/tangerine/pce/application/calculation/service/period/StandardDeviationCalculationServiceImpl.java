package ca.tangerine.pce.application.calculation.service.period;

import ca.tangerine.pce.application.calculation.metric.StandardDeviationCalculation;
import ca.tangerine.pce.application.calculation.service.period.core.WeightedAverageWithCpedAbstractService;
import ca.tangerine.pce.application.config.PeriodProperties;
import ca.tangerine.pce.application.returns.PortfolioMonthlyReturnsContextProvider;
import ca.tangerine.pce.application.returns.ReturnsSnapshot;
import ca.tangerine.pce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import ca.tangerine.pce.application.util.ReturnFactorScale;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.risk.StandardDeviationResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.model.error.exceptions.CalculationException;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static ca.tangerine.pce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.OUTPUT_SCALE;

@Service
public class StandardDeviationCalculationServiceImpl
    extends
      WeightedAverageWithCpedAbstractService<PeriodCommand, StandardDeviationResult> {

  public StandardDeviationCalculationServiceImpl(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      PeriodProperties periods) {
    super(portfolioMonthlyReturnsContextProvider, portfolioWeightedAverageWithCped, periods.getRiskCalculations());
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
    var portfolioReturns = result.weightedAverage();
    validateCipsdWithinPortfolioPerformanceRange(command.getCustomIntervalPsd(), result.snapshot());
    var context = new PeriodCalculationInput(command.getCustomIntervalPsd(), portfolioReturns);
    return StandardDeviationCalculation.<StandardDeviationResult>builder()
        .input(context)
        .defaultPeriods(defaultPeriods)
        .scale(OUTPUT_SCALE)
        .build()
        .calculate(command.getPeriods());
  }

  private void validateCipsdWithinPortfolioPerformanceRange(LocalDate cipsd,
      ReturnsSnapshot<?> snapshot) {
    if (cipsd == null || snapshot.performanceStartDate() == null || snapshot.performanceEndDate() == null) {
      return;
    }
    if (cipsd.isBefore(snapshot.performanceStartDate()) || cipsd.isAfter(snapshot.performanceEndDate())) {
      throw ErrorCode.CIPSD_OUTSIDE_DATA_RANGE_ERROR.toException(
          cipsd, snapshot.performanceStartDate(), snapshot.performanceEndDate());
    }
  }
}
