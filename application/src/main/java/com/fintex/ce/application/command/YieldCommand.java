package com.fintex.ce.application.command;

import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class YieldCommand extends PortfolioHoldingsCommand {
  private Integer timeIntervalPeriods;
}
