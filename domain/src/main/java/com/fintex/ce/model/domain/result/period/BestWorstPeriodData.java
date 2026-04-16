package com.fintex.ce.model.domain.result.period;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BestWorstPeriodData {

  private List<PeriodValueResult> bestPeriodPct = new ArrayList<>();
  private List<PeriodValueResult> worstPeriodPct = new ArrayList<>();
  private List<PeriodValueResult> average = new ArrayList<>();
  private List<PeriodValueResult> numberOfPeriods = new ArrayList<>();
  private List<PeriodValueResult> pctPositive = new ArrayList<>();
  private List<PeriodDateResult> bestPeriodDate = new ArrayList<>();
  private List<PeriodDateResult> worstPeriodDate = new ArrayList<>();
}
