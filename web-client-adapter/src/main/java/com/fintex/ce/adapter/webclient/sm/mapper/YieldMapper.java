package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.wm.commons.domain.financial.Income;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Maps Security Master Income response to Yield domain model.
 */
@Component
public class YieldMapper implements SecurityMasterResponseMapper<Yield, Income> {

  @Override
  public Yield map(Income smsResponse, Holding holding) {
    Yield result = new Yield()
        .setHoldingId(holding.getSecurityIdentifier().getId());

    if (smsResponse == null) {
      return result;
    }

    Optional.ofNullable(smsResponse.getDividendYield())
        .ifPresent(dp -> {
          result.setDividendYield(dp.getValue());
          Optional.ofNullable(dp.getDataProvider())
              .ifPresent(provider -> result.setProviders(List.of(provider)));
        });

    return result;
  }
}
