package com.fintex.ce.calculation;

import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;

/**
 * Port interface for breakdown calculation services. Implementations handle calculations like asset allocation, equity
 * sector, country exposure, stylebox exposure, and other portfolio breakdown metrics.
 *
 * @param <R>
 *          The result type extending BaseCalculationResult
 * @param <T>
 *          The breakdown type enum (e.g., EquitySectorAllocationType, AssetAllocationRegion)
 */
public interface BreakdownCalculationService<R extends BaseCalculationResult, T>
    extends
      CalculationService<PortfolioHoldingsCommand, R> {

}
