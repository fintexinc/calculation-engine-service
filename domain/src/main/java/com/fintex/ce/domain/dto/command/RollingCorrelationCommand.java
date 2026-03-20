package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.holding.Holding;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RollingCorrelationCommand extends RollingCalculationCommand {

  @Override
  public PortfolioCommand setHoldings(List<Holding> holdings) {
    holdings.forEach(holding -> holding.setValue(BigDecimal.ONE));
    return super.setHoldings(holdings);
  }

  @Override
  public PortfolioCommand setBenchmarkHoldings(List<Holding> benchmarkHoldings) {
    benchmarkHoldings.forEach(holding -> holding.setValue(BigDecimal.ONE));
    return super.setBenchmarkHoldings(benchmarkHoldings);
  }
}
