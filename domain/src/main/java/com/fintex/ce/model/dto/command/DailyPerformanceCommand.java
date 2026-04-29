package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.calculation.input.HoldingForDailyCalculation;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DailyPerformanceCommand {
  private LocalDate startDate;
  private LocalDate endDate;
  private List<HoldingForDailyCalculation> dailyHoldings;
}