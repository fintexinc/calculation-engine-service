package com.fintex.ce.adapter.rest.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeIntervalResDTO {

  private String timeIntervalPeriod;
  private BigDecimal value;
}
