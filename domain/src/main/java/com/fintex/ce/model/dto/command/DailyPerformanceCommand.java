package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.calculation.input.HoldingForDailyCalculation;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPerformanceCommand {
  private LocalDate startDate;
  private LocalDate endDate;
  private List<HoldingForDailyCalculation> dailyHoldings;
}