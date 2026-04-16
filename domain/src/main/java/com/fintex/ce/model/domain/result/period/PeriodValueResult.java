package com.fintex.ce.model.domain.result.period;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PeriodValueResult {

  private Long period;
  private BigDecimal value;
}
