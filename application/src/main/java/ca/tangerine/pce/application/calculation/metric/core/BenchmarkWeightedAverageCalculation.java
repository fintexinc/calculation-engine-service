package ca.tangerine.pce.application.calculation.metric.core;

import ca.tangerine.pce.application.util.ReturnSeriesAlignmentValidator;
import ca.tangerine.pce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import ca.tangerine.pce.model.domain.result.PeriodResult;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class BenchmarkWeightedAverageCalculation<T extends PeriodResult, V>
    extends
      PeriodCalculationAbstract<T, V> {

  @Setter
  protected NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns;

  protected BenchmarkWeightedAverageCalculation(final BenchmarkPeriodCalculationInput input,
      final Set<TimePeriod> periods) {
    super(input, periods);
    this.benchmarkTotalReturns = input.getWeightedAverageBenchmarkReturns();
  }

  @Override
  public int availableMonths() {
    return Math.min(super.availableMonths(), benchmarkTotalReturns.size());
  }

  protected void validatePortfolioBenchmarkCoverage(int numberOfMonths) {
    ReturnSeriesAlignmentValidator.requirePortfolioBenchmarkCoverage(portfolioTotalReturns, benchmarkTotalReturns,
        numberOfMonths);
  }

}
