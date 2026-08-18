package com.fintex.ce.application.config;

import com.fintex.ce.application.constant.AccumulateHoldingType;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Top Holdings calculation configuration. {@code accumulateTypes} are the MIC holding-type codes counted as candidates
 * for the Top-N aggregation; {@code defaultNumOfTopCommonHoldings} is the response size used when the request omits
 * {@code numOfTopCommonHoldings}; {@code maxRecursionDepth} bounds the underlying-holdings tree walk;
 * {@code maxLeavesPerHolding} caps the number of leaves emitted per portfolio holding so a malformed MIC response
 * cannot OOM the service.
 */
@Getter
@Setter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "default.top-common-holdings")
public class TopHoldingsProperties {

  private Set<AccumulateHoldingType> accumulateTypes = EnumSet.noneOf(AccumulateHoldingType.class);
  private int defaultNumOfTopCommonHoldings = 10;
  private int maxRecursionDepth = 5;
  private int maxLeavesPerHolding = 10_000;
}
