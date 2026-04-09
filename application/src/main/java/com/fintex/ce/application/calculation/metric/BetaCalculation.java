package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.AlphaBetaCalculationAbstract;
import com.fintex.ce.domain.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.domain.model.result.BetaResult;
import com.fintex.ce.domain.model.result.core.TimeIntervalResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import lombok.Getter;

@Getter
public class BetaCalculation extends AlphaBetaCalculationAbstract<BetaResult> {

  public BetaCalculation(final BenchmarkCalculationDTO input,
      final Set<String> periods,
      final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
      final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
    super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
  }

  @Override
  public BetaResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final BetaResult resDTO = new BetaResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    resDTO.setBeta(timeIntervals);
    return resDTO;
  }

}
