package ca.tangerine.pce.webclient.mic.mapper;

import ca.tangerine.pce.model.domain.calculation.fee.FeeData;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.util.BigDecimalUtils;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.currency.CurrencyDatapoint;
import ca.tangerine.wm.commons.domain.datapoint.DatapointMetadata;
import ca.tangerine.wm.commons.domain.datapoint.FloatDatapoint;
import ca.tangerine.wm.commons.domain.financial.Fees;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static ca.tangerine.pce.model.util.BigDecimalUtils.percentageToRatio;

/**
 * Maps the Market Investment Catalogue {@link Fees} response to the engine-side {@link FeeData} model.
 *
 * <p>
 * MIC sends fee fields in <i>percentage</i> form ({@code 1.51} = 1.51%); the engine works in <i>ratio</i> form
 * ({@code 0.0151}). This mapper is the single boundary that converts: every fee field goes through
 * {@link BigDecimalUtils#percentageToRatio}. Currency is passed through as-is.
 */
@Component
public class FeesMapper implements MarketInvestmentCatalogueResponseMapper<FeeData, Fees> {

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
    if (dp == null) {
      return null;
    }
    List<DataProvider> providers = dp.getDataProviders();
    return providers == null || providers.isEmpty() ? null : providers.get(0);
  }

  private static Currency currency(CurrencyDatapoint dp) {
    return dp == null ? null : dp.getValue();
  }
}
