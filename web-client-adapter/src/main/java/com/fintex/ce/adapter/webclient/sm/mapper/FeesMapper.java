package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.datapoint.FloatDatapoint;
import com.fintex.wm.commons.domain.financial.Fees;
import com.fintex.wm.commons.domain.financial.ManagementFeeDatapoint;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Maps Security Master Fees response to Fees domain model.
 */
@Component
public class FeesMapper implements SecurityMasterResponseMapper<FeeData, Fees> {

  @Override
  public FeeData map(Fees smsResponse, PortfolioHolding holding) {
    FeeData result = new FeeData()
        .setHoldingId(holding.getSecurityIdentifier().getId());

    if (smsResponse == null) {
      return result;
    }

    mapManagementFee(smsResponse.getManagementFee(), result);
    mapFloatDatapoint(smsResponse.getManagementExpenseRatio(), result::setManagementExpenseRatio,
        result::setManagementExpenseRatioProvider);
    mapFloatDatapoint(smsResponse.getNetExpenseRatio(), result::setNetExpenseRatio,
        result::setNetExpenseRatioProvider);
    mapFloatDatapoint(smsResponse.getGrossExpenseRatio(), result::setGrossExpenseRatio,
        result::setGrossExpenseRatioProvider);
    mapFloatDatapoint(smsResponse.getActual12B1Fee(), result::setActual12B1Fee,
        result::setActual12B1FeeProvider);

    return result;
  }

  private void mapManagementFee(ManagementFeeDatapoint datapoint, FeeData result) {
    Optional.ofNullable(datapoint)
        .ifPresent(dp -> {
          result.setManagementFee(dp.getValue());
          result.setManagementFeeProvider(dp.getDataProvider());
        });
  }

  private void mapFloatDatapoint(FloatDatapoint datapoint,
      Consumer<BigDecimal> valueSetter,
      Consumer<DataProvider> providerSetter) {
    Optional.ofNullable(datapoint)
        .ifPresent(dp -> {
          valueSetter.accept(dp.getValue());
          providerSetter.accept(dp.getDataProvider());
        });
  }
}
