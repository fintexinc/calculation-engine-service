package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.holding.Holding;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Command for rolling correlation calculation over a moving time window. Supports metric: rolling-correlation")
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
