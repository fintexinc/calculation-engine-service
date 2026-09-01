package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.AlphaBetaCalculationAbstract;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.risk.BetaResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import lombok.Getter;

@Getter
public class BetaCalculation extends AlphaBetaCalculationAbstract<BetaResult> {

  public BetaCalculation(final BenchmarkPeriodCalculationInput input,
      final Set<TimePeriod> periods,
      final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
      final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
    super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
  }

  @Override
  public BetaResult defineResponseType(final Map<String, BigDecimal> periodValues) {
    return new BetaResult(periodValues);
  }

}
