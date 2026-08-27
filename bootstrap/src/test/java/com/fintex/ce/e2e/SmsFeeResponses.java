package com.fintex.ce.e2e;

import com.fintex.ce.test.AttributeCurrencyDatapoint;
import com.fintex.ce.test.AttributeDatapoint;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.util.List;
import lombok.experimental.UtilityClass;

import okhttp3.mockwebserver.Dispatcher;

/**
 * Shared fixtures for the {@code FEES} attribute, which every fee metric reads — MER, management fee, the fee amounts,
 * and the benchmark comparison.
 *
 * <p>
 * The rows are hand-written records rather than the commons domain classes on purpose; {@link AttributeDatapoint} and
 * {@link AttributeCurrencyDatapoint}, which carry the values, say why. Only the fee-specific envelope is declared here
 * — which datapoints the {@code FEES} attribute carries is this attribute's business, and nothing else's.
 *
 * <p>
 * Fee values are in percentage form, as the vendor reports them: {@code "2.25"} means 2.25%, which the mapper converts
 * to the ratio 0.0225 for the rest of the engine.
 */
@UtilityClass
final class SmsFeeResponses {

  private static final List<DataProvider> MORNINGSTAR_ONLY = List.of(DataProvider.MORNINGSTAR);

  /**
   * Answers every fee-attribute call with these rows. The path is matched, so a metric that stopped asking for
   * {@code FEES} would fail the test loudly rather than be answered anyway.
   */
  static Dispatcher feesDispatcher(FeeRow... rows) {
    return SmsAttributeResponses.singleAttributeDispatcher(CompositeSecurityAttribute.FEES, body(rows));
  }

  static String body(FeeRow... rows) {
    return AbstractPortfolioCalculationE2ETest.writeJson(List.of(rows));
  }

  /**
   * A row carrying only the management expense ratio — what a fund answers for the {@code mer} metric and for the fee
   * amounts, whose primary datapoint it is.
   */
  static FeeRow merRow(String id, FiIdentifierType idType, String merPercent, Currency currency) {
    return feeRow(id, idType, currency, null, merPercent, null, null);
  }

  /**
   * A row carrying only the management fee — a different datapoint from the MER, and the one the {@code management-fee}
   * metric reads.
   */
  static FeeRow managementFeeRow(String id, FiIdentifierType idType, String managementFeePercent, Currency currency) {
    return feeRow(id, idType, currency, managementFeePercent, null, null, null);
  }

  /**
   * A row with no fee datapoint at all, only the currency every security carries. The shape a fund arrives in when the
   * vendor covers the security but not its fees — which the fee metrics must not read as 0%.
   */
  static FeeRow currencyOnlyRow(String id, FiIdentifierType idType, Currency currency) {
    return feeRow(id, idType, currency, null, null, null, null);
  }

  static FeeRow feeRow(String id, FiIdentifierType idType, Currency currency, String managementFeePercent,
      String merPercent, String netExpenseRatioPercent, String grossExpenseRatioPercent) {
    return new FeeRow(
        new SecurityIdentifier(id, idType),
        new FeeFields(
            AttributeDatapoint.of(managementFeePercent, MORNINGSTAR_ONLY),
            AttributeDatapoint.of(merPercent, MORNINGSTAR_ONLY),
            AttributeDatapoint.of(netExpenseRatioPercent, MORNINGSTAR_ONLY),
            AttributeDatapoint.of(grossExpenseRatioPercent, MORNINGSTAR_ONLY),
            currency == null ? null : new AttributeCurrencyDatapoint(currency, MORNINGSTAR_ONLY)));
  }

  record FeeRow(SecurityIdentifier identifier, FeeFields data) {
  }

  record FeeFields(
      AttributeDatapoint managementFee,
      AttributeDatapoint managementExpenseRatio,
      AttributeDatapoint netExpenseRatio,
      AttributeDatapoint grossExpenseRatio,
      AttributeCurrencyDatapoint currency) {
  }
}
