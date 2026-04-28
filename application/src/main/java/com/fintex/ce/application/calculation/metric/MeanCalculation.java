package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.returns.MeanResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.fintex.ce.application.util.DecimalUtils.toScale;
import static com.fintex.ce.model.util.BigDecimalConstants.INTERNAL_SCALE;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;

@Accessors(chain = true)
@EqualsAndHashCode
public class MeanCalculation<T extends PeriodResult> extends PeriodCalculationAbstract<T, BigDecimal> {

  @Setter
  private int scale = INTERNAL_SCALE;

  public MeanCalculation(final PeriodCalculationInput input,
      final Set<String> defaultPeriods) {
    super(input, defaultPeriods);
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    return calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
  }

  private BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths,
      final NavigableMap<LocalDate, BigDecimal> returns) {
    if (numberOfMonths > returns.size() || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, returns);
    final SortedMap<LocalDate, BigDecimal> portfolioTotalReturnsByPeriod = getSubMapByPeriodStartDate(periodStartDate,
        returns);

    return calculateMean(portfolioTotalReturnsByPeriod);
  }

  /**
   * calculates mean
   *
   * @param portfolioTotalReturnsByPeriod
   *          portfolio total return values from start of period to the end
   * @return mean value
   */
  private BigDecimal calculateMean(final SortedMap<LocalDate, BigDecimal> portfolioTotalReturnsByPeriod) {
    final BigDecimal mean = CalculationUtils.average(portfolioTotalReturnsByPeriod);
    return toScale(mean, scale);
  }

  @Override
  public T defineResponseType(final Set<Pair<String, BigDecimal>> periodValues) {
    final MeanResult result = new MeanResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(periodValues);
    result.setMean(timeIntervals);
    return (T) result;
  }

}
