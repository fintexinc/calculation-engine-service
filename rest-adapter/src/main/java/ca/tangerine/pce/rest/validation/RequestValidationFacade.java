package ca.tangerine.pce.rest.validation;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.error.PceExceptionCollector;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates all {@link RequestValidator}s by supported metric. All violations found by the metric's validators are
 * collected and thrown together.
 */
@Component
public class RequestValidationFacade {

  private final Map<CalculationMetric, List<RequestValidator>> validatorsByMetric;

  public RequestValidationFacade(List<RequestValidator> validators) {
    this.validatorsByMetric = new EnumMap<>(CalculationMetric.class);
    for (RequestValidator validator : validators) {
      for (CalculationMetric metric : validator.supportedMetrics()) {
        validatorsByMetric.computeIfAbsent(metric, k -> new ArrayList<>()).add(validator);
      }
    }
  }

  public void validate(CalculationCommand command) {
    List<RequestValidator> validators = validatorsByMetric.getOrDefault(command.getMetric(), List.of());
    PceExceptionCollector collector = new PceExceptionCollector();
    for (RequestValidator validator : validators) {
      collector.tryCatch(() -> {
        validator.validate(command);
        return null;
      });
    }
    collector.throwIfAny();
  }
}
