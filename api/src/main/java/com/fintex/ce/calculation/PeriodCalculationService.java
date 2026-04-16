package com.fintex.ce.calculation;

import com.fintex.ce.model.domain.result.PeriodResult;
import com.fintex.ce.model.dto.command.PeriodCommand;

/**
 * Port interface for period-based calculation services. Implementations handle calculations like trailing returns,
 * standard deviation, sharpe ratio, beta, alpha, and other time-period based metrics.
 *
 * @param <E>
 *          The result type extending PeriodResult
 * @param <R>
 *          The command type extending PeriodCommand
 */
public interface PeriodCalculationService<E extends PeriodResult, R extends PeriodCommand>
    extends
      CalculationService<E, R> {

}
