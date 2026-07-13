package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.BenchmarkWeightedAverageCalculation;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.InformationRatioResult;
import com.fintex.ce.model.util.BigDecimalConstants;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public class InformationRatioCalculation
    extends
      BenchmarkWeightedAverageCalculation<InformationRatioResult, BigDecimal> {

  private final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation;
  private final TrackingErrorCalculation trackingErrorCalculation;

  public InformationRatioCalculation(final BenchmarkPeriodCalculationInput input,
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
    validatePortfolioBenchmarkCoverage(numberOfMonths);

    BigDecimal portfolioReturn = trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths,
        getPortfolioTotalReturns());
    LocalDate portfolioPeriodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
    LocalDate portfolioPeriodEndDate = getPortfolioTotalReturns().lastKey();
    NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturnsByPortfolioPeriod = getBenchmarkTotalReturns()
        .subMap(portfolioPeriodStartDate, true, portfolioPeriodEndDate, true);
    BigDecimal benchmarkReturn = trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths,
        benchmarkTotalReturnsByPortfolioPeriod);
    BigDecimal trackingError = trackingErrorCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);

    return DecimalUtils.divide(portfolioReturn.subtract(benchmarkReturn), trackingError);
  }

  @Override
  public InformationRatioResult defineResponseType(final Set<Pair<String, BigDecimal>> periodAndInformationRatio) {
    var result = new InformationRatioResult();
    Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(periodAndInformationRatio);
    result.setInformationRatio(timeIntervals);
    return result;
  }
}
