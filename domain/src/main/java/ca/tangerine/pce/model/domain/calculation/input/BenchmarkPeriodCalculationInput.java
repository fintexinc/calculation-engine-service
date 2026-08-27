package ca.tangerine.pce.model.domain.calculation.input;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import lombok.Data;
@Data
public class BenchmarkPeriodCalculationInput extends PeriodCalculationInput {

  private NavigableMap<LocalDate, BigDecimal> weightedAverageBenchmarkReturns;

}
