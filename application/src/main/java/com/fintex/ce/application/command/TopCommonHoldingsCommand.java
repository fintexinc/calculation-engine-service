package com.fintex.ce.application.command;

import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
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
