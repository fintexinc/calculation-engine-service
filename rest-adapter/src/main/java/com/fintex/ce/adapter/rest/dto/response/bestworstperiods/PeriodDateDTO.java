package com.fintex.ce.adapter.rest.dto.response.bestworstperiods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodDateDTO {

  private Long period;
  private IntervalDTO interval;

}
