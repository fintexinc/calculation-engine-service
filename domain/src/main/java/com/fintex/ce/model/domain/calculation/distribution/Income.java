package com.fintex.ce.model.domain.calculation.distribution;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Income {

  private String date;
  private BigDecimal amount;
}
