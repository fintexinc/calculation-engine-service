package com.fintex.ce.domain.model.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

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