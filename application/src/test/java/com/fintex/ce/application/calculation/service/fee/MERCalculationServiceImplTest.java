package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY_STRICT;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class MERCalculationServiceImplTest {

  private final SecurityDataFetcher<FeeData> feesFetcher = mock(SecurityDataFetcher.class);
  private final FxRateService fxRateService = mock(FxRateService.class);
  private final DefaultTargetCurrencyConverter defaultTargetCurrencyConverter = new DefaultTargetCurrencyConverter(
      fxRateService, new FxProperties());
  private final MERCalculationServiceImpl service = new MERCalculationServiceImpl(feesFetcher,
      DEFAULT_DATA_PROPERTIES, defaultTargetCurrencyConverter, new MerFeeResolver(List.of(
          new CanadianFeeResolutionStrategy(), new UsFeeResolutionStrategy())));

  {
    // Default: identity FX for any source currency so single-currency tests stay focused on fee math.
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      Set<Currency> src = inv.getArgument(0);
      return src.stream().collect(Collectors.toMap(c -> c, c -> BigDecimal.ONE));
    });
  }

  @Test
  void fundsOnly_includesOnlyMerBearingHoldings_normalisedWithinSubset() {
    // 100 in Canadian MF (MER 0.02), 100 in stock (no MER) -> FUNDS_ONLY = 0.02
    PortfolioHolding fund = holding("CIG-001", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        fund, fee("0.02", null, null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(fund, stock), FUNDS_ONLY, WHOLE_PORTFOLIO));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.02");
  }

  @Test
  void wholePortfolio_includesNonFundHoldingsAt0Pct() {
    // 100 in Canadian MF (MER 0.02), 100 in stock (no MER) -> WHOLE_PORTFOLIO = 0.01
    PortfolioHolding fund = holding("CIG-001", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        fund, fee("0.02", null, null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(fund, stock), FUNDS_ONLY, WHOLE_PORTFOLIO));

    assertThat(result.getManagementExpenseRatio().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0.01");
  }

  @Test
  void segregatedFundCanada_isTreatedAsCanadianFund() {
    PortfolioHolding seg = holding("SEG-001", FinancialInstrumentType.SEGREGATED_FUND_CANADA, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        seg, fee("0.025", "0.020", null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(seg), FUNDS_ONLY, FUNDS_ONLY_STRICT));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.025");
    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY_STRICT)).isEqualByComparingTo("0.025");
  }

  @Test
  void hedgeFundCanada_allFeeFieldsMissing_throwsMissingFundFeeData() {
    PortfolioHolding hedge = holding("HF-001", FinancialInstrumentType.HEDGE_FUND_CANADA, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        hedge, fee(null, null, null, null)));

    assertThatThrownBy(() -> service.perform(commandFor(List.of(hedge), FUNDS_ONLY)))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("has no fee data");
  }

  @Test
  void fundHoldingMissingFromSmsResponse_throwsSmsNoDataForHolding() {
    // SMS returned data for one fund but not the other — strict check refuses to silently treat the missing one as 0%.
    PortfolioHolding present = holding("CIG-PRESENT", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    PortfolioHolding missing = holding("US-MISSING", FinancialInstrumentType.MUTUAL_FUND_US, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        present, fee("0.02", null, null, null)));

    assertThatThrownBy(() -> service.perform(commandFor(List.of(present, missing), FUNDS_ONLY, WHOLE_PORTFOLIO)))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("No data returned for holding")
        .hasMessageContaining("US-MISSING");
  }

  @Test
  void duplicateFundIdsWithDifferentValues_passCheck_whenSmsReturnsOneRowForTheId() {
    // Same fund held twice with different market values — SMS dedupes by identifier and returns one row.
    // The strict check must accept both holdings because the fetcher fans the row out to each requested holding.
    PortfolioHolding fundA = holding("CIG-DUP", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    PortfolioHolding fundB = holding("CIG-DUP", FinancialInstrumentType.MUTUAL_FUND_CANADA, "300");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        fundA, fee("0.02", null, null, null),
        fundB, fee("0.02", null, null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(fundA, fundB), FUNDS_ONLY, WHOLE_PORTFOLIO));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.02");
    assertThat(result.getManagementExpenseRatio().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0.02");
  }

  @Test
  void zeroMerHoldingMissingFromSms_isExempt_andCountedAtZeroPct() {
    // A stock isn't expected to have an SMS /fees row — strict check exempts ZERO_MER_TYPES, holding contributes 0%.
    PortfolioHolding fund = holding("CIG-OK", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        fund, fee("0.02", null, null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(fund, stock), FUNDS_ONLY, WHOLE_PORTFOLIO));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.02");
    assertThat(result.getManagementExpenseRatio().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0.01");
  }

  @Test
  void usFund_usesNerFirst_noWarnings_evenWhenSmsAlsoReportsMer() {
    // US chain starts at NER (MER is excluded — it's a Canadian metric). MER value in SMS is ignored without warning.
    PortfolioHolding usFund = holding("VFINX", FinancialInstrumentType.MUTUAL_FUND_US, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        usFund, fee("0.020", null, "0.018", null)));

    AverageMerResult result = service.perform(commandFor(List.of(usFund), FUNDS_ONLY));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.018");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void caFund_missingMer_fallsBackToManagementFee_warnsOnlyAboutMer() {
    // CA chain skips NER/GER entirely (US-only metrics). A CA fund with MER null falls straight to Management Fee, and
    // only the missing-MER warning is emitted — no spurious "missing NER/GER" noise.
    PortfolioHolding caFund = holding("CIG-002", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        caFund, fee(null, "0.012", null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(caFund), FUNDS_ONLY));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.012");
    assertThat(result.getWarnings()).extracting("code").containsExactly("FDS-022");
  }

  @Test
  void caFund_onlyNerPresent_throws_becauseNerIsNotInCanadianChain() {
    // NER is a US regulatory metric and is not part of the Canadian fee chain — even if SMS returns one for a CA fund,
    // the resolver ignores it and throws MISSING_FUND_FEE_DATA when no applicable source is populated.
    PortfolioHolding caFund = holding("CIG-NER-ONLY", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        caFund, fee(null, null, "0.018", null)));

    assertThatThrownBy(() -> service.perform(commandFor(List.of(caFund), FUNDS_ONLY)))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("has no fee data");
  }

  @Test
  void usFund_missingNer_fallsBackToGer_warnsAboutNer() {
    PortfolioHolding etf = holding("VTI", FinancialInstrumentType.ETF_US, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        etf, fee(null, null, null, "0.0125")));

    AverageMerResult result = service.perform(commandFor(List.of(etf), FUNDS_ONLY));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.0125");
    assertThat(result.getWarnings()).extracting("code").containsExactly("FDS-024");
  }

  @Test
  void fund_onlyManagementFeePresent_fallsBackAndWarnsAboutAllThreeRatios() {
    // Real SMS data shape: fund row has MER, NER, GER all null but managementFee populated.
    PortfolioHolding etf = holding("VFINX", FinancialInstrumentType.MUTUAL_FUND_US, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        etf, fee(null, "0.01", null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(etf), FUNDS_ONLY));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.01");
    assertThat(result.getWarnings()).extracting("code").containsExactly("FDS-024", "FDS-025");
  }

  @Test
  void fund_merAndManagementFeePresent_usesMerWithoutWarnings() {
    // Most common real-data case: SMS populates both MER and managementFee. Use MER, no warnings.
    PortfolioHolding fund = holding("CIG-007", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        fund, fee("0.0151", "0.01", null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(fund), FUNDS_ONLY));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.0151");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void fundsOnlyStrict_returnsNull_whenAnyIncludedHoldingMissingPrimary() {
    PortfolioHolding fund = holding("CIG-003", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    // Primary (MER) missing, secondary (managementFee) present.
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        fund, fee(null, "0.018", null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(fund), FUNDS_ONLY, FUNDS_ONLY_STRICT));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.018");
    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY_STRICT)).isNull();
  }

  @Test
  void fundsOnlyAndStrict_areNulled_whenPortfolioHasNoFundHoldings() {
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "100");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of());

    AverageMerResult result = service.perform(
        commandFor(List.of(stock), FUNDS_ONLY, WHOLE_PORTFOLIO, FUNDS_ONLY_STRICT));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isNull();
    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY_STRICT)).isNull();
    assertThat(result.getManagementExpenseRatio().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0");
  }

  @Test
  void fixedIncome_isTreatedAsZeroMer_andCountedInWholePortfolio() {
    PortfolioHolding fund = holding("CIG-004", FinancialInstrumentType.MUTUAL_FUND_CANADA, "50");
    PortfolioHolding fi = holding("BOND-001", FinancialInstrumentType.FIXED_INCOME, "50");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        fund, fee("0.02", null, null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(fund, fi), WHOLE_PORTFOLIO));

    // Fund alone is 50/100 of portfolio, fee 0.02 -> 0.01.
    assertThat(result.getManagementExpenseRatio().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0.01");
  }

  @Test
  void fundsOnlyAndWholePortfolio_areIndependent_noStateBleed() {
    // Regression: prior implementation mutated calc objects across passes.
    PortfolioHolding fund = holding("CIG-005", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "300");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        fund, fee("0.04", null, null, null)));

    AverageMerResult result = service.perform(commandFor(List.of(fund, stock), FUNDS_ONLY, WHOLE_PORTFOLIO));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.04");
    assertThat(result.getManagementExpenseRatio().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0.01");
  }

  @Test
  void fundsOnly_weightedMer_reflectsCadConvertedWeights_acrossCurrencies() {
    // USD fund and CAD fund with identical native marketValue (1000) but different MERs.
    // With USD->CAD rate of 1.35, the USD holding's CAD weight is 1350 vs CAD's 1000.
    // Weighted MER = (1350 * 0.01 + 1000 * 0.02) / 2350 = 33.5 / 2350 ≈ 0.01425531915...
    PortfolioHolding usFund = holding("VFINX", FinancialInstrumentType.MUTUAL_FUND_US, "1000");
    PortfolioHolding caFund = holding("CIG-XCAD", FinancialInstrumentType.MUTUAL_FUND_CANADA, "1000");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        usFund, FeeData.builder()
            .netExpenseRatio(new BigDecimal("0.01"))
            .currency(Currency.USD)
            .build(),
        caFund, FeeData.builder()
            .managementExpenseRatio(new BigDecimal("0.02"))
            .currency(Currency.CAD)
            .build()));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(
        Currency.USD, new BigDecimal("1.35"),
        Currency.CAD, BigDecimal.ONE));

    AverageMerResult result = service.perform(commandFor(List.of(usFund, caFund), FUNDS_ONLY));

    // 33.5 / 2350 ≈ 0.01425531914893617; toUserScale truncates to 10 fractional digits.
    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.0142553191");
    assertThat(result.getWarnings()).extracting("code").doesNotContain("FX-001");
  }

  @Test
  void usdFund_fxUnavailable_emitsWarning_andComputesUnconverted() {
    // FX rate missing for USD: value stays in native, FX-001 warning emitted, result still numeric.
    PortfolioHolding usFund = holding("VFINX", FinancialInstrumentType.MUTUAL_FUND_US, "1000");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        usFund, FeeData.builder()
            .netExpenseRatio(new BigDecimal("0.02"))
            .currency(Currency.USD)
            .build()));
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      Map<Currency, BigDecimal> m = new HashMap<>();
      m.put(Currency.USD, null);
      return m;
    });

    AverageMerResult result = service.perform(commandFor(List.of(usFund), FUNDS_ONLY));

    assertThat(result.getManagementExpenseRatio().get(FUNDS_ONLY)).isEqualByComparingTo("0.02");
    assertThat(result.getWarnings()).extracting("code").contains("FX-001");
  }

  @Test
  void merBearingHoldingWithMissingCurrency_throws() {
    PortfolioHolding fund = holding("CIG-NOCURR", FinancialInstrumentType.MUTUAL_FUND_CANADA, "1000");
    when(feesFetcher.fetch(any(), any())).thenReturn(Map.of(
        fund, FeeData.builder()
            .managementExpenseRatio(new BigDecimal("0.02"))
            // currency intentionally null
            .build()));

    assertThatThrownBy(() -> service.perform(commandFor(List.of(fund), FUNDS_ONLY)))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("missing Currency");
  }

  // ---------- helpers ----------

  private static PortfolioHolding holding(String id, FinancialInstrumentType type, String value) {
    return PortfolioHolding.builder()
        .value(new BigDecimal(value))
        .holdingType(type)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.MORNINGSTAR_ID))
        .build();
  }

  private static FeeData fee(String mer, String managementFee, String ner, String ger) {
    // Default currency CAD: SMS always returns a currency for a fund row, and the FX path now hard-fails when it's
    // missing on a MER-bearing holding. Tests that want a different source currency build their own FeeData inline.
    return FeeData.builder()
        .managementExpenseRatio(mer == null ? null : new BigDecimal(mer))
        .managementFee(managementFee == null ? null : new BigDecimal(managementFee))
        .netExpenseRatio(ner == null ? null : new BigDecimal(ner))
        .grossExpenseRatio(ger == null ? null : new BigDecimal(ger))
        .currency(Currency.CAD)
        .build();
  }

  private static AverageMerCommand commandFor(List<PortfolioHolding> holdings,
      com.fintex.ce.model.domain.enumeration.FeeAggregationMode... modes) {
    var cmd = new AverageMerCommand();
    cmd.setHoldings(new java.util.ArrayList<>(holdings));
    cmd.setParameterTypes(List.of(modes));
    return cmd;
  }
}
