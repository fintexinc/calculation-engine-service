package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.AlphaBetaCalculationAbstract;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.BetaResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import lombok.Getter;

@Getter
public class BetaCalculation extends AlphaBetaCalculationAbstract<BetaResult> {

  public BetaCalculation(final BenchmarkPeriodCalculationInput input,
      final Set<String> periods,
      final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
      final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
    super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
  }

  @Override
  public BetaResult defineResponseType(final Set<Pair<String, BigDecimal>> periodValues) {
    final BetaResult result = new BetaResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(periodValues);
    result.setBeta(timeIntervals);
    return result;
  }

}
