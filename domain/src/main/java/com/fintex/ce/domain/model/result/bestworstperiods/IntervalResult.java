package com.fintex.ce.domain.model.result.bestworstperiods;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class IntervalResult {

  private LocalDate startDate;
  private LocalDate endDate;
}
