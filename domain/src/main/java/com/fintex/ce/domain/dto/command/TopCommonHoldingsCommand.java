package com.fintex.ce.domain.dto.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TopCommonHoldingsCommand extends PortfolioHoldingsCommand {
  private Integer numOfFundsMin;
  private Integer numOfTopCommonHoldings;
  private Set<String> accumulateHoldingTypes;
}
