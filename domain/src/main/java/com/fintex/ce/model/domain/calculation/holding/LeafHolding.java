package com.fintex.ce.model.domain.calculation.holding;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

/**
 * Leaf role of the Composite pattern: the immutable result of expanding one branch of the underlying-holdings tree down
 * to a terminal node. Carries the effective weight of that node within its portfolio parent. Keeping it separate from
 * the SM-mapped {@link CommonHolding} prevents the recursion from mutating the mapped tree as a side effect. A leaf has
 * no children, so {@link #leaves()} yields only itself.
 */
public record LeafHolding(PortfolioHolding holding, BigDecimal weight, CommonHolding source)
    implements
      HoldingComponent {

  @Override
  public List<HoldingComponent> children() {
    return List.of();
  }

  @Override
  public Stream<LeafHolding> leaves() {
    return Stream.of(this);
  }
}
