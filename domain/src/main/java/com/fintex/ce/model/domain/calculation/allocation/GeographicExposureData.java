package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.financial.Geography;

import java.util.Map;

/**
 * Typed data consumed by the geographic exposure services: per-holding geographic allocations (funds / ETFs / bonds)
 * and geographies (stocks), prepared from the service-specific geographic-allocation attribute and {@code GEOGRAPHY}.
 */
public record GeographicExposureData(
    Map<PortfolioHolding, HoldingGeographicAllocation> allocations,
    Map<PortfolioHolding, Geography> geographies) {
}
