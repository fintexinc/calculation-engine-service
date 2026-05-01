package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Schema(description = "Command for rolling correlation calculation over a moving time window. Supports metric: rolling-correlation")
public class RollingCorrelationCommand extends RollingCalculationCommand {

  @Override
  public void setHoldings(List<PortfolioHolding> holdings) {
    super.setHoldings(holdings.stream()
        .map(holding -> (PortfolioHolding) holding.toBuilder().value(BigDecimal.ONE).build())
        .toList());
  }

  @Override
  public void setBenchmarkHoldings(List<PortfolioHolding> benchmarkHoldings) {
    super.setBenchmarkHoldings(benchmarkHoldings.stream()
        .map(holding -> (PortfolioHolding) holding.toBuilder().value(BigDecimal.ONE).build())
        .toList());
  }
}
