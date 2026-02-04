package com.fintex.ce.service;

import com.fintex.ce.port.input.command.DailyPerformanceCommand;
import com.fintex.ce.port.input.result.DistributionResult;

public interface DistributionService {

  DistributionResult perform(final DailyPerformanceCommand command);
}