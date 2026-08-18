package com.fintex.ce.model.domain.calculation.holding;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

/**
 * Composite role of the Composite pattern: an internal (fund-like) node of the expanded underlying-holdings tree that
 * carries its own effective weight and source while delegating leaf extraction to its {@code children}. Keeping it
 * separate from the MIC-mapped {@link CommonHolding} prevents the recursion from mutating the mapped tree as a side
 * effect, mirroring {@link LeafHolding}.
 */
public record CompositeHolding(PortfolioHolding holding, BigDecimal weight, CommonHolding source,
    List<HoldingComponent> children) implements HoldingComponent {

  @Override
  public Stream<LeafHolding> leaves() {
    return children.stream().flatMap(HoldingComponent::leaves);
  }
}
