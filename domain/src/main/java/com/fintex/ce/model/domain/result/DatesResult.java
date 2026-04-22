package com.fintex.ce.model.domain.result;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class DatesResult extends BaseCalculationResult {

  protected LocalDate performanceEndDate;
  protected LocalDate performanceStartDate;
}
