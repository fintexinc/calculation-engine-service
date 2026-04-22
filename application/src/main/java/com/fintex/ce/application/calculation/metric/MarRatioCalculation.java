package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.MarRatioResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;
import com.fintex.ce.util.DecimalUtils;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DecimalUtils.abs;

public class MarRatioCalculation extends PeriodCalculationAbstract<MarRatioResult, BigDecimal> {

  private final TrailingTotalReturnsCalculation trailingTotalReturnsCalculation;
  private final MaxDrawdownCalculation maxDrawdownCalculation;

  public MarRatioCalculation(final CalculationDTO input,
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
    final MaxDrawdownEntry maxDrawdownDTO = maxDrawdownCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);
    if (Objects.isNull(maxDrawdownDTO.getValue()) || maxDrawdownDTO.getValue().compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }
    return DecimalUtils.divide(trailingTRValue, abs(maxDrawdownDTO.getValue()));
  }

  @Override
  public MarRatioResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final var marRatioResDTO = new MarRatioResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    marRatioResDTO.setMarRatio(timeIntervals);
    return marRatioResDTO;
  }

}