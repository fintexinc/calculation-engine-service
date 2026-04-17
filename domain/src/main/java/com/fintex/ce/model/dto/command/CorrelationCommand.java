package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.holding.PortfolioHolding;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Command for correlation calculation between portfolio and benchmark. Supports metric: correlation")
public class CorrelationCommand extends PeriodCommand {

  @Override
  public PortfolioCommand setHoldings(final List<PortfolioHolding> holdings) {
    return super.setHoldings(holdings.stream()
        .map(holding -> (PortfolioHolding) holding.toBuilder().value(BigDecimal.ONE).build())
        .toList());
  }
}
