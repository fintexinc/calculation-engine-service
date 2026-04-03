package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.holding.Holding;
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
  public PortfolioCommand setHoldings(final List<Holding> holdings) {
    holdings.forEach(holding -> holding.setValue(BigDecimal.ONE));
    return super.setHoldings(holdings);
  }
}
