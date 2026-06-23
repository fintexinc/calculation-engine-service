package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.MarRatioResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import static com.fintex.ce.application.util.DecimalUtils.abs;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;

public class MarRatioCalculation extends PeriodCalculationAbstract<MarRatioResult, BigDecimal> {

  private final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation;
  private final MaxDrawdownCalculation maxDrawdownCalculation;

  public MarRatioCalculation(final PeriodCalculationInput input,
      final Set<String> defaultPeriods,
      final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation,
      final MaxDrawdownCalculation maxDrawdownCalculation) {
    super(input, defaultPeriods);
    this.trailingTotalReturnsCalculation = trailingTotalReturnsCalculation;
    this.maxDrawdownCalculation = maxDrawdownCalculation;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
    if (numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final BigDecimal trailingTRValue = trailingTotalReturnsCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);
    final MaxDrawdownEntry maxDrawdown = maxDrawdownCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);
    if (Objects.isNull(trailingTRValue) || Objects.isNull(maxDrawdown) || Objects.isNull(maxDrawdown.value())
        || maxDrawdown.value().compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }
    return DecimalUtils.divide(trailingTRValue, abs(maxDrawdown.value()));
  }

  @Override
  public MarRatioResult defineResponseType(final Set<Pair<String, BigDecimal>> periodValues) {
    final var result = new MarRatioResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(periodValues);
    result.setMarRatio(timeIntervals);
    return result;
  }

}