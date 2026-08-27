package ca.tangerine.pce.rest.validation;

import java.util.List;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.dto.command.CalculationCommand;

public interface RequestValidator {

  List<CalculationMetric> supportedMetrics();

  void validate(CalculationCommand command);
}
