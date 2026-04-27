package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsIntervalResult;
import com.fintex.ce.model.domain.result.distribution.DistributionOfReturnsResult;
import com.fintex.ce.model.domain.result.distribution.DistributionRangeResult;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.application.util.DecimalUtils.getMaxValue;
import static com.fintex.ce.application.util.DecimalUtils.getMinValue;
import static com.fintex.ce.application.util.DecimalUtils.setInternalScale;
import static com.fintex.ce.application.util.DecimalUtils.squareRoot;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.isNull;

public class DistributionOfReturnsCalculation {

  public RollingTotalReturnsCalculation rollingTotalReturnsCalculation;
  private final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns;

  public DistributionOfReturnsCalculation(final RollingTotalReturnsCalculation rollingTotalReturnsCalculation,
      final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns) {
    this.rollingTotalReturnsCalculation = rollingTotalReturnsCalculation;
    this.portfolioTotalReturns = portfolioTotalReturns;
  }

  public DistributionOfReturnsResult calculate(final DistributionOfReturnsCommand reqDTO) {
    final NavigableMap<LocalDate, BigDecimal> annualReturns = rollingTotalReturnsCalculation
        .calculatePeriodForNumberOfMonths(TWELVE.intValue());
    final DistributionOfReturnsIntervalResult calculatedMonthlyReturns = calculateDistributionOfReturnsFor(
        portfolioTotalReturns, reqDTO);

    if (annualReturns == null) {
      return initializeResponseDTO(calculatedMonthlyReturns, null, portfolioTotalReturns);
    }

    final DistributionOfReturnsIntervalResult calculatedAnnualReturns = calculateDistributionOfReturnsFor(annualReturns,
        reqDTO);
    return initializeResponseDTO(calculatedMonthlyReturns, calculatedAnnualReturns, portfolioTotalReturns);
  }

  /**
   * Performs the whole logic for Distribution Of Returns calculation.
   *
   * @param returns
   *          monthly/annual returns.
   * @param reqDTO
   *          request dto.
   * @return DistributionOfReturnsIntervalResult for monthly/annual returns.
   */
  public DistributionOfReturnsIntervalResult calculateDistributionOfReturnsFor(
      final NavigableMap<LocalDate, BigDecimal> returns,
      final DistributionOfReturnsCommand reqDTO) {
    final BigDecimal returnsMin = getMinValue(returns);
    final BigDecimal returnsMax = getMaxValue(returns);

    final Integer numberOfBins = calculateNumberOfBins(returns, reqDTO.getCustomNumberOfBins());
    final BigDecimal binWidthIncrements = calculateBinWidthIncrements(returnsMin, returnsMax, numberOfBins);

    final List<DistributionRangeResult> distributionRange = calculateDistributionOfReturns(returns, returnsMin,
        numberOfBins, binWidthIncrements);
    return new DistributionOfReturnsIntervalResult()
        .setDistributionMin(toUserScale(returnsMin))
        .setDistributionMax(toUserScale(returnsMax))
        .setDistributionBin(numberOfBins)
        .setDistributionIncrement(toUserScale(binWidthIncrements))
        .setDistributionRange(distributionRange);
  }

  /**
   * Calculates number of bins.
   * <p>
   * Number of bins can be default and custom.
   * <p>
   * If a customNumberOfBins param was specified (custom) then the number of bins can be determined by taking the square
   * root of the number of data points between the performance start date and performance end date and round down,
   * otherwise takes just a user's specified number. In fact it should be 5 < customNumberOfBins < 30.
   *
   * @param returns
   *          monthly/annual returns.
   * @param customNumberOfBins
   *          custom number of bins.
   * @return number of bins.
   */
  public Integer calculateNumberOfBins(final NavigableMap<LocalDate, BigDecimal> returns,
      final Integer customNumberOfBins) {
    return isNull(customNumberOfBins)
        ? setInternalScale(squareRoot(BigDecimal.valueOf(returns.size())), RoundingMode.FLOOR).intValue()
        : customNumberOfBins;
  }

  /**
   * Calculates the bin width increments for the distribution: In an attempt to group the individual monthly or annual
   * return into bins it is important to objectively determine what the bin size is.
   * <p>
   * Formula: MAX(value of returns) - MIN(value of returns) / Number of Bins.
   *
   * @param min
   *          MIN value of returns.
   * @param max
   *          MAX value of returns.
   * @param numberOfBins
   *          calculated number of bins.
   * @return Bin Width Increments value.
   */
  public BigDecimal calculateBinWidthIncrements(final BigDecimal min, final BigDecimal max,
      final Integer numberOfBins) {
    return divide(max.subtract(min), numberOfBins);
  }

  /**
   * Calculates Distribution of Returns. Calculates step-by-step bin interval and frequency of returns for each bin(MAX
   * - numberOfBins).
   *
   * @param returns
   *          monthly/annual returns.
   * @param returnsMin
   *          returnsMIN value of returns.
   * @param numberOfBins
   *          calculated numberOfBins.
   * @param binWidthIncrements
   *          calculated BinWidthIncrements.
   * @return list of DistributionRangeResults.
   */
  public List<DistributionRangeResult> calculateDistributionOfReturns(final NavigableMap<LocalDate, BigDecimal> returns,
      final BigDecimal returnsMin,
      final long numberOfBins,
      final BigDecimal binWidthIncrements) {
    final List<DistributionRangeResult> result = new ArrayList<>();
    if (Objects.nonNull(returns) && Objects.nonNull(returnsMin) && Objects.nonNull(binWidthIncrements)
        && numberOfBins > 0) {
      for (int binIndex = 1; binIndex <= numberOfBins; binIndex++) {
        final BigDecimal binInterval = calculateBinInterval(returnsMin, binIndex, binWidthIncrements);
        final long frequencyOfReturns = calculateFrequencyOfReturns(returns, returnsMin, binInterval, result);
        result.add(initializeDistributionRangeDTO(binIndex, binInterval, frequencyOfReturns));
      }
    }
    return result;
  }

  /**
   * Calculates the BinInterval value . BinInterval is the returnsMIN value determined plus the Bin number * the Bin
   * Width Increment.
   * <p>
   * Formula: BinInterval = returnsMIN + binNumber * binWidthIncrements.
   *
   * @param returnsMIN
   *          returnsMIN value of returns.
   * @param binIndex
   *          bin index.
   * @param returnsBinWidthIncrements
   *          calculated Bin Width Increment.
   * @return Bin Interval Value.
   */
  public BigDecimal calculateBinInterval(final BigDecimal returnsMIN,
      final int binIndex,
      final BigDecimal returnsBinWidthIncrements) {
    return toUserScale(returnsMIN.add(BigDecimal.valueOf(binIndex).multiply(returnsBinWidthIncrements)));
  }

  /**
   * Calculates the frequency of returns. The distribution of returns(frequency of returns) determined how many times
   * the monthly returns or annual returns fall into each Bin interval calculated.
   *
   * @param returns
   *          monthly/annual returns.
   * @param returnsMin
   *          MIN value of returns.
   * @param binInterval
   *          calculated bin Interval.
   * @param rangeResDTOS
   *          result.
   * @return calculated frequency of returns.
   */
  public long calculateFrequencyOfReturns(final NavigableMap<LocalDate, BigDecimal> returns,
      final BigDecimal returnsMin,
      final BigDecimal binInterval,
      final List<DistributionRangeResult> rangeResDTOS) {
    return binInterval.compareTo(toUserScale(returnsMin)) == ZERO.intValue()
        ? calculateFrequencyIfBinIntervalEqualsMin(returns, returnsMin)
        : calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeResDTOS);
  }

  public DistributionRangeResult initializeDistributionRangeDTO(final int binIndex, final BigDecimal binInterval,
      final long frequencyOfReturns) {
    return new DistributionRangeResult()
        .setBin(binIndex)
        .setRange(binInterval)
        .setValue(frequencyOfReturns);
  }

  public DistributionOfReturnsResult initializeResponseDTO(
      final DistributionOfReturnsIntervalResult calculatedMonthlyReturns,
      final DistributionOfReturnsIntervalResult calculatedAnnualReturns,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    return (DistributionOfReturnsResult) new DistributionOfReturnsResult()
        .setMonthlyReturns(calculatedMonthlyReturns)
        .setYearlyReturns(calculatedAnnualReturns)
        .setPerformanceStartDate(returns.firstKey())
        .setPerformanceEndDate(returns.lastKey());
  }

  /**
   * Calculates frequency of returns if bin Interval equals min. Here we count values which are less or equal to the MIN
   * value.
   *
   * @param returns
   *          monthly/annual returns.
   * @param returnsMin
   *          MIN value of returns.
   * @return calculated value
   */
  public long calculateFrequencyIfBinIntervalEqualsMin(final NavigableMap<LocalDate, BigDecimal> returns,
      final BigDecimal returnsMin) {
    return returns.values().stream().filter(value -> returnsMin.compareTo(value) >= ZERO.intValue()).count();
  }

  /**
   * Calculates frequency of returns if bin Interval is NOT equals MIN. Here we count values which are less or equal to
   * the current value.
   *
   * @param returns
   *          monthly/annual returns.
   * @param binInterval
   *          calculated bin Interval.
   * @param rangeResDTOS
   *          list of DistributionRangeResults.
   * @return calculated value.
   */
  public long calculateFrequencyIfBinIntervalNotEqualsMin(final NavigableMap<LocalDate, BigDecimal> returns,
      final BigDecimal binInterval,
      final List<DistributionRangeResult> rangeResDTOS) {
    if (rangeResDTOS.isEmpty()) {
      return calculateFrequencyIfBinIntervalEqualsMin(returns, binInterval);
    } else {
      final BigDecimal rangeOfPreviousBin = rangeResDTOS.get(rangeResDTOS.size() - 1).getRange();
      return calculateFrequencyIfBinIntervalNotEqualsMin(returns, binInterval, rangeOfPreviousBin);
    }
  }

  /**
   * Calculates frequency of returns if bin Interval is NOT equals MIN. Here we count values which are LESS or EQUAL to
   * the current range and bigger than range of the previous bin.
   *
   * @param returns
   *          monthly/annual returns.
   * @param binInterval
   *          calculated bin Interval.
   * @return calculated value.
   */
  public long calculateFrequencyIfBinIntervalNotEqualsMin(final NavigableMap<LocalDate, BigDecimal> returns,
      final BigDecimal binInterval,
      final BigDecimal rangeOfPreviousBin) {
    return returns.values().stream()
        .filter(value -> binInterval.compareTo(value) >= ZERO.intValue()
            && rangeOfPreviousBin.compareTo(value) < ZERO.intValue())
        .count();
  }

}
