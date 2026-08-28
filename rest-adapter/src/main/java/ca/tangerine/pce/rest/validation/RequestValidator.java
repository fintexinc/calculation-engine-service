package ca.tangerine.pce.rest.validation;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.dto.command.CalculationCommand;

import java.util.List;

public interface RequestValidator {

  List<CalculationMetric> supportedMetrics();

  void validate(CalculationCommand command);
}
