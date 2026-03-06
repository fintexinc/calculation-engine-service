package com.fintex.ce.port.input.command;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PeriodCommand;
import com.fintex.ce.port.input.command.PortfolioCommand;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CorrelationCommand extends PeriodCommand {

  @Override
  public PortfolioCommand setHoldings(final List<Holding> holdings) {
    holdings.forEach(holding -> holding.setValue(BigDecimal.ONE));
    return super.setHoldings(holdings);
  }
}
