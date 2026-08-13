package com.fintex.ce.application.config;

import com.fintex.wm.commons.domain.holding.HoldingType;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Top Holdings calculation configuration. {@code accumulateTypes} are the SM holding-type codes counted as candidates
 * for the Top-N aggregation; {@code defaultNumOfTopCommonHoldings} is the response size used when the request omits
 * {@code numOfTopCommonHoldings}; {@code maxRecursionDepth} bounds the underlying-holdings tree walk;
 * {@code maxLeavesPerHolding} caps the number of leaves emitted per portfolio holding so a malformed SM response cannot
 * OOM the service.
 */
@Getter
@Setter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "default.top-common-holdings")
public class TopHoldingsProperties {

  /**
   * The holding types a Top-N aggregation treats as candidate securities — a deliberate subset of {@link HoldingType},
   * configured rather than hardcoded because which classes of instrument belong in a "common holdings" answer is a
   * product decision, not a property of the vendor's vocabulary.
   *
   * <p>
   * The configured default is the six equity and plain-bond codes. It leaves out cash, currency, FX forwards, swaps,
   * options, futures and fund wrappers deliberately — none of them is a security a client holds in common, and the
   * wrappers are what the look-through walks into rather than counts — but it also leaves out real securities: agency
   * MBS, ABS, municipal debt, CMBS, bank loans, bank notes and preferred shares are 28.6% of the holdings in the
   * 2025-10-23 extracts and 7.2% of portfolio weight, the two figures differing because they are many small debt
   * positions. Widening it is a product call.
   *
   * <p>
   * {@link HoldingType} carries all 116 codes those extracts use, numeric ones such as {@code 12} on US municipals
   * included, and every holding now arrives already typed on it. The vendor's own set is about 120, so an unmapped code
   * stays possible: such a holding arrives untyped and simply never matches this set.
   */
  private Set<HoldingType> accumulateTypes = EnumSet.noneOf(HoldingType.class);
  private int defaultNumOfTopCommonHoldings = 10;
  private int maxRecursionDepth = 5;
  private int maxLeavesPerHolding = 10_000;
}
