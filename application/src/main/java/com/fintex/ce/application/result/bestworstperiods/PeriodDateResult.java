package com.fintex.ce.application.result.bestworstperiods;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PeriodDateResult {

  private Long period;
  private IntervalResult interval;
}
