package com.fintex.ce.domain.model.result.core;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MaxDrawdownEntry {

  private String timeIntervalPeriod;
  private BigDecimal value;
  private LocalDate drawdownStartDate;
  private LocalDate drawdownTroughDate;
  private Integer recoveryTime;
}
