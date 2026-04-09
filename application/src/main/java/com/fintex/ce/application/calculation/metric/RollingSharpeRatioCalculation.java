package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.RollingAbstractCalculation;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.model.result.RollingSharpeRatioResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public class RollingSharpeRatioCalculation extends RollingAbstractCalculation<RollingSharpeRatioResult> {

  private final SharpeRatioCalculation sharpeRatioCalculation;

  public RollingSharpeRatioCalculation(final CalculationDTO input,
      final Set<String> defaultPeriods,
      final SharpeRatioCalculation sharpeRatioCalculation) {
    super(input, defaultPeriods);
    this.sharpeRatioCalculation = sharpeRatioCalculation;
  }

  @Override
  public BigDecimal calculateRollingValue(final int numberOfMonths, final NavigableMap<LocalDate, BigDecimal> returns) {
    return sharpeRatioCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, returns);
  }

  @Override
  public RollingSharpeRatioResult defineResponseType(
      final Set<Pair<String, NavigableMap<LocalDate, BigDecimal>>> result) {
    final var rollingSharpeRatioResult = new RollingSharpeRatioResult();
    final var rollingIntervalResultS = getRollingIntervalResults(result);
    rollingSharpeRatioResult.setRollingSharpeRatio(rollingIntervalResultS);
    return rollingSharpeRatioResult;
  }

}
