package com.fintex.ce.application.dto.calculation;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class BenchmarkWeightedAverageDTO extends WeightedAverageInputDTO {

  private Map<Holding, Map<LocalDate, BigDecimal>> benchmarkMonthlyReturns;
  private Map<Holding, Currency> benchmarks;

}
