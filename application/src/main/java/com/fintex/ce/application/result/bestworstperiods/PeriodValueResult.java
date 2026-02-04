package com.fintex.ce.application.result.bestworstperiods;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PeriodValueResult {

  private Long period;
  private BigDecimal value;
}
