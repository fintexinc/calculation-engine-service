package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.dto.calculation.HoldingForDailyCalculationDTO;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DailyPerformanceCommand {
  private LocalDate startDate;
  private LocalDate endDate;
  private List<HoldingForDailyCalculationDTO> dailyHoldings;
}