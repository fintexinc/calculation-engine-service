package ca.tangerine.pce.calculation;

import ca.tangerine.pce.model.domain.result.PeriodResult;
import ca.tangerine.pce.model.dto.command.PeriodCommand;

/**
 * Port interface for period-based calculation services. Implementations handle calculations like trailing returns,
 * standard deviation, sharpe ratio, beta, alpha, and other time-period based metrics. All of them consume monthly
 * returns with the portfolio and benchmark sides kept separate.
 *
 * @param <C>
 *          The command type extending PeriodCommand
 * @param <R>
 *          The result type extending PeriodResult
 */
public interface PeriodCalculationService<C extends PeriodCommand, R extends PeriodResult>
    extends
      ReturnsBasedCalculationService<C, R> {

}
