package com.fintex.ce.service.interfaces.health;

import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.exception.ReqValidationException;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.math.BigDecimal;
import java.util.List;

public abstract class CalculationHeathIndicator<Req> implements HealthIndicator {

  @Override
  public Health health() {
    try {
      final WarningDTO res = calculateResponse(buildInput());

      if (!res.getWarnings().isEmpty()) {
        return Health.down().withDetail("warnings", res.getWarnings()).build();
      }

      return Health.up().build();
    } catch (ReqValidationException ex) {
      return Health.up().withDetail("data validation error", ex.getMessage()).build();
    } catch (Exception ex) {
      return Health.down().withDetail("exception", ex.getMessage()).build();
    }
  }

  abstract protected WarningDTO calculateResponse(final Req request);

  abstract protected Req buildInput();

  protected List<Holding> getHoldings() {
    return List.of(new FundSeriesHolding(BigDecimal.ONE, "RBF605").setHoldingIdentifier(HoldingIdentifierType.FUNDSERV)
        .setType(HoldingType.CANADA_MUTUAL_FUNDS));
  }

}
