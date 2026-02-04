package com.fintex.ce.adapter.rest.dto.response.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeIntervalResDTO {

  private String timeIntervalPeriod;
  private BigDecimal value;
}
