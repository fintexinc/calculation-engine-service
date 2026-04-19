package com.fintex.ce.adapter.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntervalResDTO {

  private LocalDate key;
  private BigDecimal value;

}
