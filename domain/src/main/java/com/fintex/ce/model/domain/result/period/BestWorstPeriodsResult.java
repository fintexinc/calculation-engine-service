package com.fintex.ce.model.domain.result.period;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BestWorstPeriodsResult {

  private LocalDate ped;
  private LocalDate psd;
  private BestWorstPeriodData bestWorstPeriods;
}
