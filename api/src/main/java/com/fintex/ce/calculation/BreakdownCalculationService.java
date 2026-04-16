package com.fintex.ce.calculation;

import com.fintex.ce.model.domain.result.WarningResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;

/**
 * Port interface for breakdown calculation services. Implementations handle calculations like asset allocation, equity
 * sector, country exposure, stylebox exposure, and other portfolio breakdown metrics.
 *
 * @param <E>
 *          The result type extending WarningResult
 * @param <T>
 *          The breakdown type enum (e.g., EquitySectorAllocationType, AssetAllocationRegion)
 */
public interface BreakdownCalculationService<E extends WarningResult, T>
    extends
      CalculationService<E, PortfolioHoldingsCommand> {

}
