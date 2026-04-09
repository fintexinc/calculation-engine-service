package com.fintex.ce.domain.model.result.core;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TimeIntervalResult {

  private String timeIntervalPeriod;
  private BigDecimal value;
}
