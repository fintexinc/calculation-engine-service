package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
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
  public Yield map(Income smsResponse, PortfolioHolding holding) {
    final var dividendYieldDp = Optional.ofNullable(smsResponse).map(Income::getDividendYield);
    final List<DataProvider> providers = dividendYieldDp
        .map(d -> d.getDataProvider())
        .map(List::of)
        .orElseGet(List::of);

    return Yield.builder()
        .dividendYield(dividendYieldDp.map(d -> d.getValue()).orElse(null))
        .providers(providers)
        .build();
  }
}
