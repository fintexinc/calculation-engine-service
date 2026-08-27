package ca.tangerine.pce.calculation;

import ca.tangerine.pce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.pce.model.dto.command.CalculationCommand;

/**
 * Calculation service whose input is per-holding return series — every returns-derived metric (trailing/annual returns,
 * growth-of-10k, risk ratios, correlations, common performance dates, ...) implements this contract. The data arrives
 * as {@link PortfolioBenchmarkReturns} with the portfolio and benchmark sides in separate maps, prepared from the two
 * sections of the fetched {@link SecurityData}; portfolio-only services simply ignore the benchmark side.
 *
 * @param <C>
 *          The command type extending CalculationCommand
 * @param <R>
 *          The result type
 */
public interface ReturnsBasedCalculationService<C extends CalculationCommand, R extends BaseCalculationResult>
    extends
      CalculationService<C, PortfolioBenchmarkReturns, R> {

}
