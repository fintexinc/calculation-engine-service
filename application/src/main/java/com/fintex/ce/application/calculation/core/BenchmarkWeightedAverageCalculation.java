package com.fintex.ce.application.calculation.core;

import com.fintex.ce.application.dto.calculation.BenchmarkCalculationDTO;
import com.fintex.ce.port.input.result.PeriodResult;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

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
