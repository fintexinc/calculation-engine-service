package com.fintex.ce.service.calculation;

import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.WarningResult;

/**
 * Port interface for breakdown calculation services. Implementations handle calculations like asset allocation, equity
 * sector, country exposure, stylebox exposure, and other portfolio breakdown metrics.
 *
 * @param <E>
 *          The result type extending WarningResult
 * @param <T>
 *          The breakdown type enum (e.g., EquitySectorAllocationType, AssetAllocationRegion)
 */
public interface BreakdownCalculationService<E extends WarningResult, T> {

  /**
   * Performs the breakdown calculation.
   *
   * @param command
   *          the command containing portfolio holdings
   * @return the calculation result with breakdown by type
   */
  E perform(PortfolioHoldingsCommand command);

}
