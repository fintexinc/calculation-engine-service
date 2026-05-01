package com.fintex.ce.model.domain.result;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class DatesResult extends BaseCalculationResult {

  protected LocalDate performanceEndDate;
  protected LocalDate performanceStartDate;
}
