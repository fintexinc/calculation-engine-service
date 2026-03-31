package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.datapoint.SalesChargeData;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Maps Security Master SalesChargeData response to SalesCharge domain model.
 */
@Component
public class SalesChargeMapper
        implements SecurityMasterResponseMapper<SalesCharge, SalesChargeData> {

  @Override
  public SalesCharge map(SalesChargeData smsResponse, Holding holding) {
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
            .ifPresent(provider -> result.setProvider(provider.name()));

    return result;
  }
}
