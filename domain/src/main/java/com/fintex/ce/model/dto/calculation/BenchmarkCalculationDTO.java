package com.fintex.ce.model.dto.calculation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BenchmarkCalculationDTO extends CalculationDTO {

  private NavigableMap<LocalDate, BigDecimal> weightedAverageBenchmarkReturns;

}
