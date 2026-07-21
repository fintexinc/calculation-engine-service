package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.util.Map;

/**
 * Typed data consumed by the fixed-income credit-quality service: per-holding credit-quality ratings and asset
 * allocations, prepared from the fetched {@code CREDIT_QUALITY_RATINGS} and {@code ASSET_ALLOCATION} attributes.
 */
public record CreditQualityData(
    Map<PortfolioHolding, CreditQuality> creditQuality,
    Map<PortfolioHolding, HoldingAssetAllocation> assetAllocations) {
}
