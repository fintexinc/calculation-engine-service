package ca.tangerine.pce.model.domain.calculation.allocation;

import java.util.Map;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.financial.Geography;

/**
 * Typed data consumed by the asset-allocation breakdown services: per-holding asset allocations (funds) and geographies
 * (stocks), prepared from the fetched {@code ASSET_ALLOCATION} and {@code GEOGRAPHY} attributes.
 */
public record AssetAllocationData(
    Map<PortfolioHolding, HoldingAssetAllocation> allocations,
    Map<PortfolioHolding, Geography> geographies) {
}
