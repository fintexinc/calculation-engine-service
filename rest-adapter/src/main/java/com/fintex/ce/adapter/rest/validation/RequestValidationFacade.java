package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.error.PceExceptionCollector;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

  public void validate(CalculationCommand command, CalculationMetric metric) {
    List<RequestValidator> validators = validatorsByMetric.getOrDefault(metric, List.of());
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
