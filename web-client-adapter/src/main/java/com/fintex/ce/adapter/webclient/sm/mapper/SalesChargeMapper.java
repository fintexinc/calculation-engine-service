package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
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
    SalesCharge result = new SalesCharge()
        .setHoldingId(holding.getSecurityIdentifier().getId());

    if (smsResponse == null) {
      return result;
    }

    var salesChargeDatapoint = smsResponse.getSalesCharge();
    if (salesChargeDatapoint == null) {
      return result;
    }

    result.setType(salesChargeDatapoint.getValue());

    Optional.ofNullable(salesChargeDatapoint.getDataProvider())
        .ifPresent(provider -> result.setProviders(List.of(provider)));

    return result;
  }
}
