package com.fintex.ce.application.dto.calculation;

import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;

@Data
@Accessors(chain = true)
public class BenchmarkCalculationDTO extends CalculationDTO {

  private NavigableMap<LocalDate, BigDecimal> weightedAverageBenchmarkReturns;

}
