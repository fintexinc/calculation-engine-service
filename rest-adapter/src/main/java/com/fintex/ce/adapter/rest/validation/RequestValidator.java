package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.CalculationCommand;

import java.util.List;

public interface RequestValidator {

  List<CalculationMetric> supportedMetrics();

  void validate(CalculationCommand command);
}
