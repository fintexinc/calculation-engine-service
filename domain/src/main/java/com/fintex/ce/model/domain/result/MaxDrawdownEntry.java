package com.fintex.ce.model.domain.result;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
