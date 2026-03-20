package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.result.bestworstperiods.BestWorstPeriodData;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

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
