package ca.tangerine.pce.application.calculation.metric;

import ca.tangerine.pce.application.calculation.metric.core.PeriodCalculationAbstract;
import ca.tangerine.pce.application.util.CalculationUtils;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.result.PeriodResult;
import ca.tangerine.pce.model.domain.result.risk.StandardDeviationResult;
import ca.tangerine.pce.model.util.BigDecimalConstants;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import static ca.tangerine.pce.application.util.DecimalUtils.divide;
import static ca.tangerine.pce.application.util.DecimalUtils.pow;
import static ca.tangerine.pce.application.util.DecimalUtils.squareRoot;
import static ca.tangerine.pce.application.util.DecimalUtils.toScale;
import static ca.tangerine.pce.model.util.BigDecimalConstants.INTERNAL_SCALE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.TWELVE;
import static java.math.BigDecimal.ZERO;

@EqualsAndHashCode
public class StandardDeviationCalculation<T extends PeriodResult> extends PeriodCalculationAbstract<T, BigDecimal> {

  @Setter
  private int scale = INTERNAL_SCALE;

  public StandardDeviationCalculation(final PeriodCalculationInput input,
      final Set<TimePeriod> defaultPeriods) {
    super(input, defaultPeriods);
  }

  @Builder
  public StandardDeviationCalculation(final PeriodCalculationInput input,
      final Set<TimePeriod> defaultPeriods,
      final Integer scale) {
    super(input, defaultPeriods);
    this.scale = Optional.ofNullable(scale).orElse(INTERNAL_SCALE);
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
  public T defineResponseType(final Set<Pair<String, BigDecimal>> periodValues) {
    return (T) new StandardDeviationResult(formTimeIntervalResult(periodValues));
  }

}
