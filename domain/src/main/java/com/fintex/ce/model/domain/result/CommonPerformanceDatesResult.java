package com.fintex.ce.model.domain.result;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CommonPerformanceDatesResult extends WarningResult {

  private LocalDate commonPerformanceStartDatePf;
  private LocalDate commonPerformanceEndDatePf;
  private LocalDate commonPerformanceStartDateBm;
  private LocalDate commonPerformanceEndDateBm;
}
