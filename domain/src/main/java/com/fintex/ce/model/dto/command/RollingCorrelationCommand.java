package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

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
  public PortfolioCommand setHoldings(List<PortfolioHolding> holdings) {
    return super.setHoldings(holdings.stream()
        .map(holding -> (PortfolioHolding) holding.toBuilder().value(BigDecimal.ONE).build())
        .toList());
  }

  @Override
  public PortfolioCommand setBenchmarkHoldings(List<PortfolioHolding> benchmarkHoldings) {
    return super.setBenchmarkHoldings(benchmarkHoldings.stream()
        .map(holding -> (PortfolioHolding) holding.toBuilder().value(BigDecimal.ONE).build())
        .toList());
  }
}
