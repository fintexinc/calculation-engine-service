package com.fintex.ce.domain.dto.command;

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
