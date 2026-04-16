package com.fintex.ce.model.domain.result.period;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class IntervalResult {

  private LocalDate startDate;
  private LocalDate endDate;
}
