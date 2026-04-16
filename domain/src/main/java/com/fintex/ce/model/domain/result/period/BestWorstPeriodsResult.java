package com.fintex.ce.model.domain.result.period;

import com.fintex.ce.model.domain.result.ErrorResult;

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
public class BestWorstPeriodsResult extends ErrorResult {

  private LocalDate ped;
  private LocalDate psd;
  private BestWorstPeriodData bestWorstPeriods;
}
