package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.financial.Fees;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Maps Security Master Fees response to Fees domain model.
 */
@Component
public class FeesMapper implements SecurityMasterResponseMapper<FeeData, Fees> {

  @Override
  public FeeData map(Fees smsResponse, PortfolioHolding holding) {
    final var fees = Optional.ofNullable(smsResponse);
    final var mgmtFee = fees.map(Fees::getManagementFee);
    final var mer = fees.map(Fees::getManagementExpenseRatio);
    final var ner = fees.map(Fees::getNetExpenseRatio);
    final var ger = fees.map(Fees::getGrossExpenseRatio);
    final var fee12b1 = fees.map(Fees::getActual12B1Fee);

    return FeeData.builder()
        .holdingId(holding.getSecurityIdentifier().getId())
        .managementFee(mgmtFee.map(d -> d.getValue()).orElse(null))
        .managementFeeProvider(mgmtFee.map(d -> d.getDataProvider()).orElse(null))
        .managementExpenseRatio(mer.map(d -> d.getValue()).orElse(null))
        .managementExpenseRatioProvider(mer.map(d -> d.getDataProvider()).orElse(null))
        .netExpenseRatio(ner.map(d -> d.getValue()).orElse(null))
        .netExpenseRatioProvider(ner.map(d -> d.getDataProvider()).orElse(null))
        .grossExpenseRatio(ger.map(d -> d.getValue()).orElse(null))
        .grossExpenseRatioProvider(ger.map(d -> d.getDataProvider()).orElse(null))
        .actual12B1Fee(fee12b1.map(d -> d.getValue()).orElse(null))
        .actual12B1FeeProvider(fee12b1.map(d -> d.getDataProvider()).orElse(null))
        .build();
  }
}
