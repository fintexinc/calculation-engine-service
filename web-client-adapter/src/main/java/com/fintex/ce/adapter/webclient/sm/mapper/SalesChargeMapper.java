package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.sales.SalesChargeData;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Maps Security Master SalesChargeData response to SalesCharge domain model.
 */
@Component
public class SalesChargeMapper
    implements
      SecurityMasterResponseMapper<SalesCharge, SalesChargeData> {

  @Override
  public SalesCharge map(SalesChargeData smsResponse, PortfolioHolding holding) {
    final var datapoint = Optional.ofNullable(smsResponse).map(SalesChargeData::getSalesCharge);
    final List<DataProvider> providers = datapoint
        .map(d -> d.getDataProvider())
        .map(List::of)
        .orElseGet(List::of);

    return SalesCharge.builder()
        .holdingId(holding.getSecurityIdentifier().getId())
        .type(datapoint.map(d -> d.getValue()).orElse(null))
        .providers(providers)
        .build();
  }
}
