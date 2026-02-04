package com.fintex.ce.port.input.command;

import com.fintex.ce.domain.model.calculation.HoldingForDailyCalculationDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class DailyPerformanceCommand {
  private LocalDate startDate;
  private LocalDate endDate;
  private List<HoldingForDailyCalculationDTO> dailyHoldings;
}