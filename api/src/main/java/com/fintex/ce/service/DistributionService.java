package com.fintex.ce.service;

import com.fintex.ce.domain.dto.command.DailyPerformanceCommand;
import com.fintex.ce.domain.model.result.DistributionResult;

public interface DistributionService {

  DistributionResult perform(final DailyPerformanceCommand command);
}