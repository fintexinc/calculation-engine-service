package com.fintex.ce.model.domain.result;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class CommonPerformanceDatesResult extends ErrorResult {

  private LocalDate commonPerformanceStartDatePf;
  private LocalDate commonPerformanceEndDatePf;
  private LocalDate commonPerformanceStartDateBm;
  private LocalDate commonPerformanceEndDateBm;
}