package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.RSquaredCalculationAbstract;
import com.fintex.ce.model.domain.calculation.input.BenchmarkPeriodCalculationInput;
import com.fintex.ce.model.domain.result.risk.RSquaredResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import lombok.Getter;

@Getter
public class RSquaredCalculation extends RSquaredCalculationAbstract<RSquaredResult> {

  public RSquaredCalculation(final BenchmarkPeriodCalculationInput input,
      final Set<TimePeriod> periods,
      final NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn,
      final NavigableMap<LocalDate, BigDecimal> benchmarkExcessReturn) {
    super(input, periods, portfolioExcessReturn, benchmarkExcessReturn);
  }

  @Override
  public RSquaredResult defineResponseType(final Set<Pair<String, BigDecimal>> periodValues) {
    return new RSquaredResult(formTimeIntervalResult(periodValues));
  }

}
