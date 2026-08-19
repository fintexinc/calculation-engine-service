package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.config.FeeProjectionProperties;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.FeesResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;
import com.fintex.wm.commons.domain.id.FiIdentifierType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY_STRICT;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.FIVE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.ONE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TEN_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.THREE_YR;
import static com.fintex.wm.commons.domain.enumeration.TimePeriod.TWENTY_YR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeesCalculationServiceImplTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final HoldingCurrencyConverter currencyConverter = new HoldingCurrencyConverter(
      fxRateService, new FxProperties());
  private final FeeProjectionProperties projectionProperties = flatProjection();
  private final FeesCalculationServiceImpl service = new FeesCalculationServiceImpl(currencyConverter,
      new MerFeeResolver(List.of(new CanadianFeeResolutionStrategy(), new UsFeeResolutionStrategy())),
      projectionProperties);

  /**
   * A flat balance keeps the projections at {@code annual x years} so the existing fee assertions stay readable; the
   * growth formula is covered by {@code FeeProjectionUtilsTest}.
   */
  private static FeeProjectionProperties flatProjection() {
    var properties = new FeeProjectionProperties();
    properties.setAnnualGrowthRate(BigDecimal.ZERO);
    properties.setPeriods(new LinkedHashSet<>(List.of(ONE_YR, TEN_YR, TWENTY_YR)));
    return properties;
  }

  {
    // Default: identity FX for any source currency so single-currency fee math is the focus of each test. Tests that
    // care about FX override this via Mockito.when() in the test body.
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      java.util.Set<com.fintex.wm.commons.domain.currency.Currency> src = inv.getArgument(0);
      return src.stream().collect(java.util.stream.Collectors.toMap(c -> c, c -> BigDecimal.ONE));
    });
  }

  @Test
  void annualFee_isSumOfValueTimesMer_overFundHoldings() {
    // 1000 @ 0.02 + 2000 @ 0.01 = 20 + 20 = 40
    PortfolioHolding cad = holding("CIG-001", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");
    PortfolioHolding us = holding("VTI", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.ETF, Country.USA, "2000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        cad, fee("0.02", null, null, null),
        us, fee(null, null, "0.01", null));

    FeesResult result = service.perform(commandFor(List.of(cad, us), FUNDS_ONLY, WHOLE_PORTFOLIO), securityData);

    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isEqualByComparingTo("40");
    assertThat(result.getAnnualFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("40");
  }

  @Test
  void monthlyFee_isAnnualDividedBy12() {
    PortfolioHolding fund = holding("CIG-002", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1200");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, fee("0.10", null, null, null));

    FeesResult result = service.perform(commandFor(List.of(fund), FUNDS_ONLY), securityData);

    // Annual = 120; Monthly = 10
    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isEqualByComparingTo("120");
    assertThat(result.getMonthlyFee().get(FUNDS_ONLY)).isEqualByComparingTo("10");
  }

  @Test
  void wholePortfolio_includesNonFundHoldingsAt0Pct() {
    // Fund 1000 × 0.02 = 20; stock contributes 0.
    PortfolioHolding fund = holding("CIG-003", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");
    PortfolioHolding stock = holding("AAPL", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.STOCK, Country.USA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, fee("0.02", null, null, null));

    FeesResult result = service.perform(commandFor(List.of(fund, stock), WHOLE_PORTFOLIO), securityData);

    assertThat(result.getAnnualFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("20");
    assertThat(result.getMonthlyFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo(new BigDecimal("20")
        .divide(new BigDecimal("12"), java.math.MathContext.DECIMAL64));
  }

  @Test
  void fundsOnlyStrict_isNull_whenAnyIncludedHoldingMissingPrimary() {
    PortfolioHolding fund = holding("CIG-004", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, fee(null, "0.02", null, null));

    FeesResult result = service.perform(commandFor(List.of(fund), FUNDS_ONLY, FUNDS_ONLY_STRICT), securityData);

    // FUNDS_ONLY uses fallback → 1000 × 0.02 = 20.
    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isEqualByComparingTo("20");
    assertThat(result.getAnnualFee().get(FUNDS_ONLY_STRICT)).isNull();
    assertThat(result.getMonthlyFee().get(FUNDS_ONLY_STRICT)).isNull();
  }

  @Test
  void parentHoldingType_isRejectedWithCleanError() {
    PortfolioHolding parent = holding("X", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.FUND, Country.CANADA, "1000");

    assertThatThrownBy(() -> service.perform(commandFor(List.of(parent), FUNDS_ONLY), Map.of()))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("unsupported holding type FUND")
        .hasMessageContaining("pick a specific subtype");
  }

  @Test
  void allFeeFieldsNull_throws() {
    PortfolioHolding hedge = holding("HF-001", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.HEDGE_FUND, Country.CANADA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        hedge, fee(null, null, null, null));

    assertThatThrownBy(() -> service.perform(commandFor(List.of(hedge), FUNDS_ONLY), securityData))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("has no fee data");
  }

  @Test
  void usdFund_isConvertedToCadBeforeFeeSum() {
    // USD 1000 fund × 0.02 NER = USD 20 annual fee × 1.35 USD/CAD = CAD 27.
    PortfolioHolding usdFund = holding("VTI", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.USA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        usdFund, FeeData.builder()
            .netExpenseRatio(new BigDecimal("0.02"))
            .currency(Currency.USD)
            .build());
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(
        Currency.USD, new BigDecimal("1.35")));

    FeesResult result = service.perform(commandFor(List.of(usdFund), FUNDS_ONLY), securityData);

    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isEqualByComparingTo("27");
    assertThat(result.getWarnings()).extracting("code").doesNotContain("FX-001");
  }

  @Test
  void usdFund_fxUnavailable_emitsWarningAndLeavesValueUnconverted() {
    // USD 1000 fund × 0.02 NER = USD 20. With no FX rate available, value is left in USD, sum is 20, FX-001 warning.
    PortfolioHolding usdFund = holding("VTI", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.USA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        usdFund, FeeData.builder()
            .netExpenseRatio(new BigDecimal("0.02"))
            .currency(Currency.USD)
            .build());
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      Map<Currency, BigDecimal> m = new EnumMap<>(Currency.class);
      m.put(Currency.USD, null);
      return m;
    });

    FeesResult result = service.perform(commandFor(List.of(usdFund), FUNDS_ONLY), securityData);

    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isEqualByComparingTo("20");
    assertThat(result.getWarnings()).extracting("code").contains("FX-001");
  }

  @Test
  void merBearingHoldingWithMissingCurrency_throws() {
    PortfolioHolding fund = holding("CIG-NOCURR", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, FeeData.builder()
            .managementExpenseRatio(new BigDecimal("0.02"))
            // currency intentionally null
            .build());

    assertThatThrownBy(() -> service.perform(commandFor(List.of(fund), FUNDS_ONLY), securityData))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("missing Currency");
  }

  @Test
  void usFund_missingNerAndGer_throwsMissingNerAndGer_evenWhenManagementFeePresent() {
    // Intended behavior: the MER and Fees metrics share one US resolution chain (NER → GER), so Fees does NOT keep a
    // Management-Fee fallback for US funds. With NER and GER both absent the Fees request hard-fails with MER-002
    // instead of reporting a fee off the Management Fee (0.01). The Management Fee metric is unaffected.
    PortfolioHolding etf = holding("VFINX", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.USA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        etf, fee(null, "0.01", null, null));

    assertThatThrownBy(() -> service.perform(commandFor(List.of(etf), FUNDS_ONLY), securityData))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("missing both Net Expense Ratio and Gross Expense Ratio")
        .extracting(e -> ((CalculationException) e).getErrorCode())
        .isEqualTo(ErrorCode.MISSING_NER_AND_GER);
  }

  @Test
  void usEtf_missingNerAndGer_throwsMissingNerAndGer_evenWhenManagementFeeIsZero() {
    // Real reported security F000015AWQ for the Fees metric: NER and GER blank, ActualManagementFee = 0.00000
    // (present, not null). A present zero previously resolved to a 0 fee via the Management Fee fallback; it must now
    // fail MER-002 rather than report a fee of 0.
    PortfolioHolding etf = holding("F000015AWQ", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.ETF, Country.USA, "100000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        etf, fee(null, "0.00", null, null));

    assertThatThrownBy(() -> service.perform(commandFor(List.of(etf), FUNDS_ONLY), securityData))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("missing both Net Expense Ratio and Gross Expense Ratio")
        .hasMessageContaining("F000015AWQ")
        .extracting(e -> ((CalculationException) e).getErrorCode())
        .isEqualTo(ErrorCode.MISSING_NER_AND_GER);
  }

  @Test
  void emptyFundModes_areNulled_whenNoFundHoldingsPresent() {
    PortfolioHolding stock = holding("AAPL", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.STOCK, Country.USA, "1000");

    FeesResult result = service.perform(commandFor(List.of(stock), FUNDS_ONLY, WHOLE_PORTFOLIO,
        FUNDS_ONLY_STRICT), Map.of());

    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isNull();
    assertThat(result.getAnnualFee().get(FUNDS_ONLY_STRICT)).isNull();
    assertThat(result.getAnnualFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0");
    assertThat(result.getMonthlyFee().get(FUNDS_ONLY)).isNull();
    assertThat(result.getMonthlyFee().get(FUNDS_ONLY_STRICT)).isNull();
    assertThat(result.getMonthlyFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0");
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY)).isNull();
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY_STRICT)).isNull();
    assertThat(result.getProjectedSpend().get(WHOLE_PORTFOLIO))
        .containsOnlyKeys(ONE_YR, TEN_YR, TWENTY_YR)
        .allSatisfy((years, spend) -> assertThat(spend).isEqualByComparingTo("0"));
  }

  @Test
  void shouldProjectEveryConfiguredHorizonPerMode_whenPortfolioHoldsFunds() {
    // 1000 @ 0.02 = 20 a year over the funds; the 3000 stock only widens the whole-portfolio denominator
    PortfolioHolding fund = holding("CIG-010", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");
    PortfolioHolding stock = holding("RY", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.STOCK, Country.CANADA, "3000");

    FeesResult result = service.perform(commandFor(List.of(fund, stock), FUNDS_ONLY, WHOLE_PORTFOLIO),
        Map.of(fund, fee("0.02", null, null, null)));

    assertThat(result.getProjectedSpend()).containsOnlyKeys(FUNDS_ONLY, WHOLE_PORTFOLIO);
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY)).containsOnlyKeys(ONE_YR, TEN_YR, TWENTY_YR);
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY).get(ONE_YR)).isEqualByComparingTo("20");
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY).get(TEN_YR)).isEqualByComparingTo("200");
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY).get(TWENTY_YR)).isEqualByComparingTo("400");
    // a zero-fee stock contributes no dollars, so both views project the same spend
    assertThat(result.getProjectedSpend().get(WHOLE_PORTFOLIO).get(TWENTY_YR)).isEqualByComparingTo("400");
  }

  @Test
  void shouldMatchTheAnnualFee_whenHorizonIsOneYear() {
    PortfolioHolding fund = holding("CIG-011", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1234.56");

    FeesResult result = service.perform(commandFor(List.of(fund), FUNDS_ONLY, WHOLE_PORTFOLIO),
        Map.of(fund, fee("0.0175", null, null, null)));

    assertThat(result.getProjectedSpend().get(FUNDS_ONLY).get(ONE_YR))
        .isEqualByComparingTo(result.getAnnualFee().get(FUNDS_ONLY));
    assertThat(result.getProjectedSpend().get(WHOLE_PORTFOLIO).get(ONE_YR))
        .isEqualByComparingTo(result.getAnnualFee().get(WHOLE_PORTFOLIO));
  }

  @Test
  void shouldProjectNullsRatherThanZeros_whenTheStrictModeHasNoAnswer() {
    PortfolioHolding fund = holding("CIG-012", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");

    FeesResult result = service.perform(commandFor(List.of(fund), FUNDS_ONLY, FUNDS_ONLY_STRICT),
        Map.of(fund, fee(null, "0.01", null, null)));

    assertThat(result.getAnnualFee().get(FUNDS_ONLY_STRICT)).isNull();
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY_STRICT)).isNull();
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY).get(TEN_YR)).isEqualByComparingTo("100");
  }

  @Test
  void shouldProjectOnTheConvertedValue_whenHoldingCurrencyDiffersFromReportingCurrency() {
    when(fxRateService.spotRates(anySet(), any(), any()))
        .thenReturn(Map.of(Currency.USD, new BigDecimal("1.25"), Currency.CAD, BigDecimal.ONE));
    PortfolioHolding usFund = holding("VTI", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.ETF, Country.USA, "1000");

    FeesResult result = service.perform(commandFor(List.of(usFund), FUNDS_ONLY),
        Map.of(usFund, FeeData.builder()
            .netExpenseRatio(new BigDecimal("0.01"))
            .currency(Currency.USD)
            .build()));

    // 1000 USD -> 1250 CAD, so the annual fee is 12.5 and ten years of a flat balance is 125
    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isEqualByComparingTo("12.5");
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY).get(TEN_YR)).isEqualByComparingTo("125");
    assertThat(result.getProjectedSpend().get(FUNDS_ONLY).get(TWENTY_YR)).isEqualByComparingTo("250");
  }

  @Test
  void shouldGrowTheProjectionAboveTheFlatCase_whenAGrowthRateIsConfigured() {
    projectionProperties.setAnnualGrowthRate(new BigDecimal("0.06"));
    PortfolioHolding fund = holding("CIG-013", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");

    FeesResult result = service.perform(commandFor(List.of(fund), FUNDS_ONLY),
        Map.of(fund, fee("0.02", null, null, null)));

    Map<TimePeriod, BigDecimal> projected = result.getProjectedSpend().get(FUNDS_ONLY);
    assertThat(projected.get(ONE_YR)).isEqualByComparingTo("20");
    assertThat(projected.get(TEN_YR)).isEqualByComparingTo("263.6158988476");
    assertThat(projected.get(TWENTY_YR)).isEqualByComparingTo("735.7118240709");
    assertThat(projected.get(TWENTY_YR)).isGreaterThan(projected.get(TEN_YR).multiply(BigDecimal.valueOf(2)));
  }

  @Test
  void shouldReportTheRequestedHorizons_whenTheCommandSuppliesThem() {
    PortfolioHolding fund = holding("CIG-014", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");

    FeesResult result = service.perform(commandWithHorizons(List.of(fund), new LinkedHashSet<>(List.of(THREE_YR,
        FIVE_YR)), FUNDS_ONLY),
        Map.of(fund, fee("0.02", null, null, null)));

    // the configured 1/10/20 give way entirely to the requested set — a request narrows and widens, it does not merge
    Map<TimePeriod, BigDecimal> projected = result.getProjectedSpend().get(FUNDS_ONLY);
    assertThat(projected).containsOnlyKeys(THREE_YR, FIVE_YR);
    assertThat(projected.get(THREE_YR)).isEqualByComparingTo("60");
    assertThat(projected.get(FIVE_YR)).isEqualByComparingTo("100");
  }

  @Test
  void shouldKeepTheRequestedOrder_whenHorizonsArriveUnsorted() {
    PortfolioHolding fund = holding("CIG-015", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");

    FeesResult result = service.perform(commandWithHorizons(List.of(fund), new LinkedHashSet<>(List.of(TWENTY_YR,
        ONE_YR, TEN_YR)), FUNDS_ONLY),
        Map.of(fund, fee("0.02", null, null, null)));

    assertThat(result.getProjectedSpend().get(FUNDS_ONLY).keySet()).containsExactly(TWENTY_YR, ONE_YR, TEN_YR);
  }

  @Test
  void shouldFallBackToTheConfiguredHorizons_whenTheCommandOmitsThem() {
    PortfolioHolding fund = holding("CIG-016", FiIdentifierType.MORNINGSTAR_ID,
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, "1000");

    FeesResult omitted = service.perform(commandWithHorizons(List.of(fund), null, FUNDS_ONLY),
        Map.of(fund, fee("0.02", null, null, null)));
    FeesResult empty = service.perform(commandWithHorizons(List.of(fund), Set.of(), FUNDS_ONLY),
        Map.of(fund, fee("0.02", null, null, null)));

    assertThat(omitted.getProjectedSpend().get(FUNDS_ONLY)).containsOnlyKeys(ONE_YR, TEN_YR, TWENTY_YR);
    assertThat(empty.getProjectedSpend().get(FUNDS_ONLY)).containsOnlyKeys(ONE_YR, TEN_YR, TWENTY_YR);
  }

  // ---------- helpers ----------

  private static FeeData fee(String mer, String managementFee, String ner, String ger) {
    // Default currency CAD: SMS always returns a currency for a fund row, and the FX path now hard-fails when it's
    // missing. Tests that want a different source currency build their own FeeData inline.
    return FeeData.builder()
        .managementExpenseRatio(mer == null ? null : new BigDecimal(mer))
        .managementFee(managementFee == null ? null : new BigDecimal(managementFee))
        .netExpenseRatio(ner == null ? null : new BigDecimal(ner))
        .grossExpenseRatio(ger == null ? null : new BigDecimal(ger))
        .currency(Currency.CAD)
        .build();
  }

  private static AverageMerCommand commandFor(List<PortfolioHolding> holdings, FeeAggregationMode... modes) {
    var cmd = new AverageMerCommand();
    cmd.setHoldings(new ArrayList<>(holdings));
    cmd.setParameterTypes(List.of(modes));
    return cmd;
  }

  private static AverageMerCommand commandWithHorizons(List<PortfolioHolding> holdings, Set<TimePeriod> periods,
      FeeAggregationMode... modes) {
    AverageMerCommand command = commandFor(holdings, modes);
    command.setProjectionPeriods(periods);
    return command;
  }
}
