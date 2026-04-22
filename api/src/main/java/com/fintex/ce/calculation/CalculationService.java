package com.fintex.ce.calculation;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.CalculationCommand;

/**
 * Base interface for all calculation services. Used for services that follow the pattern: perform(command) -> result
 *
 * @param <C>
 *          The command type extending CalculationCommand
 * @param <R>
 *          The result type
 */
public interface CalculationService<C extends CalculationCommand, R extends BaseCalculationResult> {

  R perform(C command);

  CalculationMetric getMetric();

}
