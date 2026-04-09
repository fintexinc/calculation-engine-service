package com.fintex.ce.domain.model.result.bestworstperiods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PeriodDateResult {

  private Long period;
  private IntervalResult interval;
}
