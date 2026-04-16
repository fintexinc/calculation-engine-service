package com.fintex.ce.model.dto.calculation;

import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class BenchmarkWeightedAverageDTO extends WeightedAverageInputDTO {

  private Map<Holding, Map<LocalDate, BigDecimal>> benchmarkMonthlyReturns;
  private Map<Holding, Currency> benchmarks;

}
