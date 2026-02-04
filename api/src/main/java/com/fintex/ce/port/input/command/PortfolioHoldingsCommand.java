package com.fintex.ce.port.input.command;

import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PortfolioHoldingsCommand extends DataProviderCommand {
  private List<Holding> holdings;
}
