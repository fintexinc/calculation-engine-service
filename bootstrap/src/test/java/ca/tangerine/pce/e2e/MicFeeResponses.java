package ca.tangerine.pce.e2e;

import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

import java.math.BigDecimal;
import java.util.List;
import lombok.experimental.UtilityClass;

import okhttp3.mockwebserver.Dispatcher;

/**
 * Shared fixtures for the {@code FEES} attribute, which every fee metric reads — MER, management fee, the fee amounts,
 * and the benchmark comparison.
 *
 * <p>
 * The rows are hand-written records rather than the commons domain classes on purpose. Market Investment Catalogue
 * serves the fee currency as {@code {"type": "CAD"}} — the field in {@code CurrencyDatapoint} is literally named
 * {@code type} — and Jackson deserialization is property-name driven, so a fixture built from the domain class would
 * emit {@code {"value":
 * "CAD"}} and the engine would receive no currency at all. Keeping the wire shape explicit here is what makes that
 * mismatch impossible to reintroduce silently.
 *
 * <p>
 * Fee values are in percentage form, as the vendor reports them: {@code "2.25"} means 2.25%, which the mapper converts
 * to the ratio 0.0225 for the rest of the engine.
 */
@UtilityClass
final class MicFeeResponses {

  private static final List<DataProvider> MORNINGSTAR_ONLY = List.of(DataProvider.MORNINGSTAR);

  /**
   * Answers every fee-attribute call with these rows. The path is matched, so a metric that stopped asking for
   * {@code FEES} would fail the test loudly rather than be answered anyway.
   */
  static Dispatcher feesDispatcher(FeeRow... rows) {
    return MicAttributeResponses.singleAttributeDispatcher(CompositeSecurityAttribute.FEES, body(rows));
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
            datapoint(managementFeePercent),
            datapoint(merPercent),
            datapoint(netExpenseRatioPercent),
            datapoint(grossExpenseRatioPercent),
            currency == null ? null : new CurrencyField(currency, MORNINGSTAR_ONLY)));
  }

  private static Datapoint datapoint(String percent) {
    return percent == null ? null : new Datapoint(new BigDecimal(percent), MORNINGSTAR_ONLY);
  }

  record FeeRow(SecurityIdentifier identifier, FeeFields data) {
  }

  record FeeFields(
      Datapoint managementFee,
      Datapoint managementExpenseRatio,
      Datapoint netExpenseRatio,
      Datapoint grossExpenseRatio,
      CurrencyField currency) {
  }

  record Datapoint(BigDecimal value, List<DataProvider> dataProviders) {
  }

  /**
   * The currency datapoint as it travels on the wire: the value under {@code type}, not under {@code value}. See the
   * class comment for why this cannot be the domain class.
   */
  record CurrencyField(Currency type, List<DataProvider> dataProviders) {
  }
}
