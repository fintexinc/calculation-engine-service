package com.fintex.ce.application.calculation.metric.core;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.calculation.BenchmarkCalculationDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class BenchmarkWeightedAverageCalculation<T extends PeriodResult, V>
    extends
      PeriodCalculationAbstract<T, V> {

  @Setter
  protected NavigableMap<LocalDate, BigDecimal> benchmarkTotalReturns;

  protected BenchmarkWeightedAverageCalculation(final BenchmarkCalculationDTO input,
      final Set<String> periods) {
    super(input, periods);
    this.benchmarkTotalReturns = input.getWeightedAverageBenchmarkReturns();
  }

}
