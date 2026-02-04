package com.fintex.ce.service.calculation;

import com.fintex.ce.port.input.command.DailyPerformanceCommand;
import com.fintex.ce.port.input.result.InflationResult;

public interface InflationService {
  InflationResult perform(DailyPerformanceCommand command);
}