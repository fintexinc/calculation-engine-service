package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.datapoint.DatapointMetadata;
import com.fintex.wm.commons.domain.datapoint.FloatDatapoint;
import com.fintex.wm.commons.domain.financial.Fees;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.fintex.ce.model.util.BigDecimalUtils.percentageToRatio;

/**
 * Maps the Security Master {@link Fees} response to the engine-side {@link FeeData} model.
 *
 * <p>
 * SMS sends fee fields in <i>percentage</i> form ({@code 1.51} = 1.51%); the engine works in <i>ratio</i> form
 * ({@code 0.0151}). This mapper is the single boundary that converts: every fee field goes through
 * {@link com.fintex.ce.model.util.BigDecimalUtils#percentageToRatio}. Currency is passed through as-is.
 */
@Component
public class FeesMapper implements SecurityMasterResponseMapper<FeeData, Fees> {

  @Override
  public FeeData map(Fees fees, PortfolioHolding holding) {
    if (fees == null) {
      return FeeData.builder().build();
    }
    return FeeData.builder()
        .managementFee(ratio(fees.getManagementFee()))
        .managementFeeProvider(provider(fees.getManagementFee()))
        .managementExpenseRatio(ratio(fees.getManagementExpenseRatio()))
        .managementExpenseRatioProvider(provider(fees.getManagementExpenseRatio()))
        .netExpenseRatio(ratio(fees.getNetExpenseRatio()))
        .netExpenseRatioProvider(provider(fees.getNetExpenseRatio()))
        .grossExpenseRatio(ratio(fees.getGrossExpenseRatio()))
        .grossExpenseRatioProvider(provider(fees.getGrossExpenseRatio()))
        .actual12B1Fee(ratio(fees.getActual12B1Fee()))
        .actual12B1FeeProvider(provider(fees.getActual12B1Fee()))
        .currency(currency(fees.getCurrency()))
        .build();
  }

  private static BigDecimal ratio(FloatDatapoint dp) {
    return dp == null ? null : percentageToRatio(dp.getValue());
  }

  private static DataProvider provider(DatapointMetadata dp) {
    return dp == null ? null : dp.getDataProvider();
  }

  private static Currency currency(CurrencyDatapoint dp) {
    return dp == null ? null : dp.getValue();
  }
}
