package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.BenchmarkWeightedAverageCalculation;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.InformationRatioResult;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.model.util.BigDecimalConstants;
import com.fintex.ce.util.DecimalUtils;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Set;

public class InformationRatioCalculation
    extends
      BenchmarkWeightedAverageCalculation<InformationRatioResult, BigDecimal> {

  private final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation;
  private final TrackingErrorCalculation trackingErrorCalculation;

  public InformationRatioCalculation(final BenchmarkCalculationDTO input,
      final Set<String> defaultPeriods,
      final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation,
      final TrackingErrorCalculation trackingErrorCalculation) {
    super(input, defaultPeriods);
    this.trailingTotalReturnsCalculation = trailingTotalReturnsCalculation;
    this.trackingErrorCalculation = trackingErrorCalculation;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths > getBenchmarkTotalReturns().size()
        || numberOfMonths > getPortfolioTotalReturns().size()
        || numberOfMonths < BigDecimalConstants.TWELVE.intValue()) {
      return null;
    }

    final BigDecimal portfolioReturn = trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths,
        getPortfolioTotalReturns());
    final BigDecimal benchmarkReturn = trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths,
        getBenchmarkTotalReturns());
    final BigDecimal trackingError = trackingErrorCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);

    return DecimalUtils.divide(portfolioReturn.subtract(benchmarkReturn), trackingError);
  }

  @Override
  public InformationRatioResult defineResponseType(final Set<Pair<String, BigDecimal>> periodAndInformationRatio) {
    final var result = new InformationRatioResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(periodAndInformationRatio);
    result.setTimeIntervalResultS(timeIntervals);
    return result;
  }
}
