package com.fintex.ce.adapter.rest.dto.response.bestworstperiods;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodValueDTO {

  private Long period;
  private BigDecimal value;

}
