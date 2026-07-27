package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.FeesResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY_STRICT;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeesCalculationServiceImplTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final DefaultTargetCurrencyConverter defaultTargetCurrencyConverter = new DefaultTargetCurrencyConverter(
      fxRateService, new FxProperties());
  private final FeesCalculationServiceImpl service = new FeesCalculationServiceImpl(defaultTargetCurrencyConverter,
      new MerFeeResolver(List.of(new CanadianFeeResolutionStrategy(), new UsFeeResolutionStrategy())));

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
    PortfolioHolding cad = holding("CIG-001", FinancialInstrumentType.MUTUAL_FUND_CANADA, "1000");
    PortfolioHolding us = holding("VTI", FinancialInstrumentType.ETF_US, "2000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        cad, fee("0.02", null, null, null),
        us, fee(null, null, "0.01", null));

    FeesResult result = service.perform(commandFor(List.of(cad, us), FUNDS_ONLY, WHOLE_PORTFOLIO), securityData);

    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isEqualByComparingTo("40");
    assertThat(result.getAnnualFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("40");
  }

  @Test
  void monthlyFee_isAnnualDividedBy12() {
    PortfolioHolding fund = holding("CIG-002", FinancialInstrumentType.MUTUAL_FUND_CANADA, "1200");
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
    PortfolioHolding fund = holding("CIG-003", FinancialInstrumentType.MUTUAL_FUND_CANADA, "1000");
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, fee("0.02", null, null, null));

    FeesResult result = service.perform(commandFor(List.of(fund, stock), WHOLE_PORTFOLIO), securityData);

    assertThat(result.getAnnualFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("20");
    assertThat(result.getMonthlyFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo(new BigDecimal("20")
        .divide(new BigDecimal("12"), java.math.MathContext.DECIMAL64));
  }

  @Test
  void fundsOnlyStrict_isNull_whenAnyIncludedHoldingMissingPrimary() {
    PortfolioHolding fund = holding("CIG-004", FinancialInstrumentType.MUTUAL_FUND_CANADA, "1000");
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
    PortfolioHolding parent = holding("X", FinancialInstrumentType.FUND, "1000");

    assertThatThrownBy(() -> service.perform(commandFor(List.of(parent), FUNDS_ONLY), Map.of()))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("unsupported holding type FUND")
        .hasMessageContaining("pick a specific subtype");
  }

  @Test
  void allFeeFieldsNull_throws() {
    PortfolioHolding hedge = holding("HF-001", FinancialInstrumentType.HEDGE_FUND_CANADA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        hedge, fee(null, null, null, null));

    assertThatThrownBy(() -> service.perform(commandFor(List.of(hedge), FUNDS_ONLY), securityData))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("has no fee data");
  }

  @Test
  void usdFund_isConvertedToCadBeforeFeeSum() {
    // USD 1000 fund × 0.02 NER = USD 20 annual fee × 1.35 USD/CAD = CAD 27.
    PortfolioHolding usdFund = holding("VTI", FinancialInstrumentType.MUTUAL_FUND_US, "1000");
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
    PortfolioHolding usdFund = holding("VTI", FinancialInstrumentType.MUTUAL_FUND_US, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        usdFund, FeeData.builder()
            .netExpenseRatio(new BigDecimal("0.02"))
            .currency(Currency.USD)
            .build());
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      java.util.Map<Currency, BigDecimal> m = new HashMap<>();
      m.put(Currency.USD, null);
      return m;
    });

    FeesResult result = service.perform(commandFor(List.of(usdFund), FUNDS_ONLY), securityData);

    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isEqualByComparingTo("20");
    assertThat(result.getWarnings()).extracting("code").contains("FX-001");
  }

  @Test
  void merBearingHoldingWithMissingCurrency_throws() {
    PortfolioHolding fund = holding("CIG-NOCURR", FinancialInstrumentType.MUTUAL_FUND_CANADA, "1000");
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
    PortfolioHolding etf = holding("VFINX", FinancialInstrumentType.MUTUAL_FUND_US, "1000");
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
    PortfolioHolding etf = holding("F000015AWQ", FinancialInstrumentType.ETF_US, "100000");
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
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "1000");

    FeesResult result = service.perform(commandFor(List.of(stock), FUNDS_ONLY, WHOLE_PORTFOLIO,
        FUNDS_ONLY_STRICT), Map.of());

    assertThat(result.getAnnualFee().get(FUNDS_ONLY)).isNull();
    assertThat(result.getAnnualFee().get(FUNDS_ONLY_STRICT)).isNull();
    assertThat(result.getAnnualFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0");
    assertThat(result.getMonthlyFee().get(FUNDS_ONLY)).isNull();
    assertThat(result.getMonthlyFee().get(FUNDS_ONLY_STRICT)).isNull();
    assertThat(result.getMonthlyFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0");
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
    // missing. Tests that want a different source currency build their own FeeData inline.
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
