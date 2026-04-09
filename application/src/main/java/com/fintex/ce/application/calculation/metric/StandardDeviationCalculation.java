package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.domain.constant.BigDecimalConstants;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.model.result.PeriodResult;
import com.fintex.ce.domain.model.result.StandardDeviationResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;
import com.fintex.ce.util.CalculationUtils;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.fintex.ce.domain.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DecimalUtils.INTERNAL_SCALE;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.pow;
import static com.fintex.ce.util.DecimalUtils.squareRoot;
import static com.fintex.ce.util.DecimalUtils.toScale;
import static java.math.BigDecimal.ZERO;

@Accessors(chain = true)
@EqualsAndHashCode
public class StandardDeviationCalculation<T extends PeriodResult> extends PeriodCalculationAbstract<T, BigDecimal> {

  @Setter
  private int scale = INTERNAL_SCALE;

  public StandardDeviationCalculation(final CalculationDTO input,
      final Set<String> defaultPeriods) {
    super(input, defaultPeriods);
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    return calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
  }

  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    if (numberOfMonths > returns.size() || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, returns);
    final SortedMap<LocalDate, BigDecimal> portfolioTotalReturnsByPeriod = getSubMapByPeriodStartDate(periodStartDate,
        returns);

    return calculateStandardDeviation(portfolioTotalReturnsByPeriod, numberOfMonths);
  }

  /**
   * calculates standardDeviation
   *
   * @param portfolioTotalReturnsByPeriod
   *          portfolio total return values from start of period to the end
   * @param numberOfMonths
   *          number of month in period
   * @return standardDeviation value
   */
  public BigDecimal calculateStandardDeviation(final SortedMap<LocalDate, BigDecimal> portfolioTotalReturnsByPeriod,
      final int numberOfMonths) {
    final BigDecimal average = CalculationUtils.average(portfolioTotalReturnsByPeriod);
    final BigDecimal numerator = calculateNumerator(portfolioTotalReturnsByPeriod, average);
    final BigDecimal standardDeviation = squareRoot(divide(numerator, numberOfMonths - 1)).multiply(squareRoot(TWELVE));
    return toScale(standardDeviation, scale);
  }

  /**
   * calculates numerator for standardDeviation formula
   *
   * @param portfolioTotalReturnsByPeriod
   *          portfolio total return values from start of period to the end
   * @param average
   *          average value of portfolioTotalReturnsByPeriod
   * @return calculated numerator
   */
  public BigDecimal calculateNumerator(final SortedMap<LocalDate, BigDecimal> portfolioTotalReturnsByPeriod,
      final BigDecimal average) {
    return portfolioTotalReturnsByPeriod.values().stream().map(value -> pow(value.subtract(average),
        BigDecimalConstants.TWO))
        .reduce(ZERO, BigDecimal::add);
  }

  @Override
  public T defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final StandardDeviationResult standardDeviationResDTO = new StandardDeviationResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    standardDeviationResDTO.setStandardDeviation(timeIntervals);
    return (T) standardDeviationResDTO;
  }

}
