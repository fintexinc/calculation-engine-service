package com.fintex.ce.service.calculation;

import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.port.input.result.PeriodResult;

/**
 * Port interface for period-based calculation services. Implementations handle calculations like trailing returns,
 * standard deviation, sharpe ratio, beta, alpha, and other time-period based metrics.
 *
 * @param <E>
 *          The result type extending PeriodResult
 * @param <R>
 *          The command type extending PeriodCommand
 */
public interface PeriodCalculationService<E extends PeriodResult, R extends PeriodCommand> {

  /**
   * Performs the period-based calculation.
   *
   * @param command
   *          the command containing holdings, periods, and other parameters
   * @return the calculation result
   */
  E perform(R command);

}
