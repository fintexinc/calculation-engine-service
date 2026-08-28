package ca.tangerine.pce.model.domain.calculation.holding;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.holding.HoldingType;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

/**
 * Component role of the Composite pattern for the expanded underlying-holdings tree. Both the terminal
 * {@link LeafHolding} and the internal {@link CompositeHolding} expose the same shape, so a single holding and a whole
 * subtree are interchangeable to callers. {@link #leaves()} is the uniform operation that yields the terminal
 * contributions regardless of whether the node is a leaf or a composite; {@link #children()} is empty for a leaf.
 */
public interface HoldingComponent {

  PortfolioHolding holding();

  BigDecimal weight();

  CommonHolding source();

  List<HoldingComponent> children();

  Stream<LeafHolding> leaves();

  default HoldingAggregator aggregator() {
    return source().aggregator();
  }

  default HoldingType type() {
    return source().getType();
  }
}
