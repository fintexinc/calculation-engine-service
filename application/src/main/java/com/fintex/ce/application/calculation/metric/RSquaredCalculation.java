package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.RSquaredCalculationAbstract;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.RSquaredResult;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import lombok.Getter;

@Getter
public class RSquaredCalculation extends RSquaredCalculationAbstract<RSquaredResult> {

  public RSquaredCalculation(final BenchmarkCalculationDTO input,
      final Set<String> periods,
      final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
      final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
    super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
  }

  @Override
  public RSquaredResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final RSquaredResult resDTO = new RSquaredResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    resDTO.setRSquared(timeIntervals);
    return resDTO;
  }

}
