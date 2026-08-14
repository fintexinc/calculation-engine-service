package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.util.Map;

/**
 * Typed data consumed by the consolidated sector exposure service: the per-holding distribution Security Master
 * publishes for a composite security, and the single sector it publishes for an individual company instead, already
 * translated onto the consolidated taxonomy.
 *
 * <p>
 * Two maps rather than one merged one because the two attributes state different things about a holding, and which of
 * them applies is decided per holding at the point of use — a security can appear in {@code distributions} carrying
 * only its currency, which is not the same as carrying a distribution.
 */
public record SectorExposureData(
    Map<PortfolioHolding, HoldingSectorAllocation> distributions,
    Map<PortfolioHolding, HoldingSectorAllocation> scalarSectors) {
}
