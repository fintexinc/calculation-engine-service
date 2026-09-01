package com.fintex.ce.model.domain.result.period;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BestWorstPeriodData {

  private Map<Long, BigDecimal> bestPeriodPct = new LinkedHashMap<>();
  private Map<Long, BigDecimal> worstPeriodPct = new LinkedHashMap<>();
  private Map<Long, BigDecimal> average = new LinkedHashMap<>();
  private Map<Long, BigDecimal> numberOfPeriods = new LinkedHashMap<>();
  private Map<Long, BigDecimal> pctPositive = new LinkedHashMap<>();
  private List<PeriodDateResult> bestPeriodDate = new ArrayList<>();
  private List<PeriodDateResult> worstPeriodDate = new ArrayList<>();
}
