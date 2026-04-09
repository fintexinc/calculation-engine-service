package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.datapoint.Income;

import org.springframework.stereotype.Component;

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
              .ifPresent(provider -> result.setProvider(provider.name()));
        });

    return result;
  }
}
