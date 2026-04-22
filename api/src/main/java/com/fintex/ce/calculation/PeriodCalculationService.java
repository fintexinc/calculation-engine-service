package com.fintex.ce.calculation;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

/**
 * Port interface for period-based calculation services. Implementations handle calculations like trailing returns,
 * standard deviation, sharpe ratio, beta, alpha, and other time-period based metrics.
 *
 * @param <C>
 *          The command type extending PeriodCommand
 * @param <R>
 *          The result type extending PeriodResult
 */
public interface PeriodCalculationService<C extends PeriodCommand, R extends PeriodResult>
    extends
      CalculationService<C, R> {

}
