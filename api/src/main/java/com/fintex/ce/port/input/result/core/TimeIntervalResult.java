package com.fintex.ce.port.input.result.core;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TimeIntervalResult {

  private String timeIntervalPeriod;
  private BigDecimal value;
}
