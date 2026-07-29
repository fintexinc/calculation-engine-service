package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.RollingAbstractCalculation;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.rolling.RollingSharpeRatioResult;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

public class RollingSharpeRatioCalculation extends RollingAbstractCalculation<RollingSharpeRatioResult> {

  private final SharpeRatioCalculation sharpeRatioCalculation;

  public RollingSharpeRatioCalculation(final PeriodCalculationInput input,
      final Set<TimePeriod> defaultPeriods,
      final SharpeRatioCalculation sharpeRatioCalculation) {
    super(input, defaultPeriods);
    this.sharpeRatioCalculation = sharpeRatioCalculation;
  }

  /**
   * Per-window Sharpe ratio. A missing T-Bill rate inside one window must NOT poison sibling windows: the non-rolling
   * Sharpe spec treats {@link ErrorCode#MISSING_TBILL_RATE} as a request-terminating 400, but the rolling spec treats
   * each window as independent (null on per-window failure, request continues), so we catch the per-window throw and
   * degrade to {@code null} here. Other calculation errors still propagate.
   */
  @Override
  public BigDecimal calculateRollingValue(final int numberOfMonths, final NavigableMap<LocalDate, BigDecimal> returns) {
    try {
      return sharpeRatioCalculation.calculatePeriodForNumberOfMonths(numberOfMonths, returns);
    } catch (CalculationException e) {
      if (ErrorCode.MISSING_TBILL_RATE.equals(e.getErrorCode())) {
        return null;
      }
      throw e;
    }
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
