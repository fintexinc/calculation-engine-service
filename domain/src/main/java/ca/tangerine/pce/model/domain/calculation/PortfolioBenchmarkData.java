package ca.tangerine.pce.model.domain.calculation;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;

import java.util.Map;

/**
 * Prepared per-holding Market Investment Catalogue data for a calculation that has both a portfolio side and a
 * benchmark side. The two sides are always kept in separate maps — the same security may appear in both holdings lists,
 * so they must never share one lookup table. Portfolio-only calculations simply ignore {@link #benchmark()}.
 *
 * @param <T>
 *          the per-holding data prepared for each side (monthly returns, fee data, ...)
 */
public interface PortfolioBenchmarkData<T> {

  Map<PortfolioHolding, T> portfolio();

  Map<PortfolioHolding, T> benchmark();
}
