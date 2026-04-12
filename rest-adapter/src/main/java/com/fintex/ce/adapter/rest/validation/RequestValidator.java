package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;

import java.util.List;

public interface RequestValidator {

  List<CalculationMetric> supportedMetrics();

  void validate(CalculationCommand command);
}
