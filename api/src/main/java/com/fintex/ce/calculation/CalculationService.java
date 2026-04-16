package com.fintex.ce.calculation;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.result.ErrorResult;
import com.fintex.ce.model.dto.command.CalculationCommand;

/**
 * Base interface for all calculation services. Used for services that follow the pattern: perform(command) -> result
 *
 * @param <E>
 *          The result type extending ErrorResult
 * @param <R>
 *          The command type extending CalculationCommand
 */
public interface CalculationService<E extends ErrorResult, R extends CalculationCommand> {

  E perform(R command);

  CalculationMetric getMetric();

}
