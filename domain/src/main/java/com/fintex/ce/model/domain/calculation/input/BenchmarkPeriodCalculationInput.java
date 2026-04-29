package com.fintex.ce.model.domain.calculation.input;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BenchmarkPeriodCalculationInput extends PeriodCalculationInput {

  private NavigableMap<LocalDate, BigDecimal> weightedAverageBenchmarkReturns;

}
