package ca.tangerine.pce.model.domain.calculation.allocation;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

import java.util.Map;

/**
 * Typed data consumed by the equity sector exposure service: the per-holding distribution over equity sectors a fund
 * publishes, and the single sector an individual company publishes instead. Both arrive as {@link EquitySector} — the
 * scalar one as a distribution with a single bucket — so only which of the two applies to a holding has to be decided,
 * not how to read it.
 */
public record EquitySectorData(
    Map<PortfolioHolding, EquitySector> distributions,
    Map<PortfolioHolding, EquitySector> scalarSectors) {
}
