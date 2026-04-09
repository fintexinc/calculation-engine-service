package com.fintex.ce.adapter.rest.dto.response.bestworstperiods;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BestWorstPeriodDTO {

  private List<PeriodValueDTO> bestPeriodPct = new ArrayList<>();
  private List<PeriodValueDTO> worstPeriodPct = new ArrayList<>();
  private List<PeriodValueDTO> average = new ArrayList<>();
  private List<PeriodValueDTO> numberOfPeriods = new ArrayList<>();
  private List<PeriodValueDTO> pctPositive = new ArrayList<>();
  private List<PeriodDateDTO> bestPeriodDate = new ArrayList<>();
  private List<PeriodDateDTO> worstPeriodDate = new ArrayList<>();

}
