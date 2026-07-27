package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.ManagementFeeResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManagementFeeCalculationServiceImplTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final HoldingCurrencyConverter currencyConverter = new HoldingCurrencyConverter(
      fxRateService, new FxProperties());
  private final ManagementFeeCalculationServiceImpl service = new ManagementFeeCalculationServiceImpl(
      currencyConverter);

  {
    // Default: identity FX so single-currency tests stay focused on fee math.
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      Set<Currency> src = inv.getArgument(0);
      return src.stream().collect(Collectors.toMap(c -> c, c -> BigDecimal.ONE));
    });
  }

  @Test
  void fundsOnly_returnsManagementFeeAverageOverFundHoldings() {
    PortfolioHolding fund = holding("CIG-001", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, FeeData.builder()
            .managementFee(new BigDecimal("0.015"))
            .currency(Currency.CAD)
            .build());

    ManagementFeeResult result = service.perform(commandFor(List.of(fund), FUNDS_ONLY, WHOLE_PORTFOLIO),
        securityData);

    assertThat(result.getManagementFee().get(FUNDS_ONLY)).isEqualByComparingTo("0.015");
  }

  @Test
  void wholePortfolio_dilutesByNonFundHoldings() {
    PortfolioHolding fund = holding("CIG-002", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "100");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, FeeData.builder()
            .managementFee(new BigDecimal("0.020"))
            .currency(Currency.CAD)
            .build());

    ManagementFeeResult result = service.perform(commandFor(List.of(fund, stock), WHOLE_PORTFOLIO), securityData);

    assertThat(result.getManagementFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0.01");
  }

  /**
   * Regression: previously, if SMS returned any row for a non-fund holding (even an empty one with just a currency),
   * resolveFees would skip it and weightedAverage would drop it from the WHOLE_PORTFOLIO denominator — silently
   * collapsing WHOLE_PORTFOLIO into FUNDS_ONLY. The ZERO_MER_TYPES branch in resolveFees prevents this.
   */
  @Test
  void wholePortfolio_dilutesEvenWhenSmsReturnsRowForNonFundHolding() {
    PortfolioHolding fund = holding("CIG-DIL", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "100");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, FeeData.builder()
            .managementFee(new BigDecimal("0.020"))
            .currency(Currency.CAD)
            .build(),
        // SMS returns a row for the stock too — empty fee fields but currency present.
        stock, FeeData.builder().currency(Currency.USD).build());

    ManagementFeeResult result = service.perform(commandFor(List.of(fund, stock), FUNDS_ONLY, WHOLE_PORTFOLIO),
        securityData);

    assertThat(result.getManagementFee().get(FUNDS_ONLY)).isEqualByComparingTo("0.020");
    // Without the fix, WHOLE_PORTFOLIO would also be 0.020 (stock dropped from denominator).
    assertThat(result.getManagementFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0.01");
  }

  @Test
  void missingManagementFeeOnFund_throws() {
    PortfolioHolding fund = holding("CIG-003", FinancialInstrumentType.MUTUAL_FUND_CANADA, "100");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, FeeData.builder().currency(Currency.CAD).build());

    assertThatThrownBy(() -> service.perform(commandFor(List.of(fund), FUNDS_ONLY), securityData))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("missing Management Fee");
  }

  @Test
  void fundsOnly_isNulled_whenPortfolioHasNoFundHoldings() {
    PortfolioHolding stock = holding("AAPL", FinancialInstrumentType.STOCK_US, "100");

    ManagementFeeResult result = service.perform(commandFor(List.of(stock), FUNDS_ONLY, WHOLE_PORTFOLIO),
        Map.of());

    assertThat(result.getManagementFee().get(FUNDS_ONLY)).isNull();
    assertThat(result.getManagementFee().get(WHOLE_PORTFOLIO)).isEqualByComparingTo("0");
  }

  @Test
  void fundsOnly_weightedManagementFee_reflectsCadConvertedWeights_acrossCurrencies() {
    // USD fund and CAD fund with identical native marketValue (1000) but different management fees.
    // Weighted MF = (1350 * 0.005 + 1000 * 0.015) / 2350 = (6.75 + 15) / 2350 = 21.75 / 2350.
    PortfolioHolding usFund = holding("VFINX", FinancialInstrumentType.MUTUAL_FUND_US, "1000");
    PortfolioHolding caFund = holding("CIG-XCAD", FinancialInstrumentType.MUTUAL_FUND_CANADA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        usFund, FeeData.builder()
            .managementFee(new BigDecimal("0.005"))
            .currency(Currency.USD)
            .build(),
        caFund, FeeData.builder()
            .managementFee(new BigDecimal("0.015"))
            .currency(Currency.CAD)
            .build());
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(
        Currency.USD, new BigDecimal("1.35"),
        Currency.CAD, BigDecimal.ONE));

    ManagementFeeResult result = service.perform(commandFor(List.of(usFund, caFund), FUNDS_ONLY), securityData);

    // 21.75 / 2350 ≈ 0.00925531914893617; toUserScale truncates to 10 fractional digits.
    assertThat(result.getManagementFee().get(FUNDS_ONLY)).isEqualByComparingTo("0.0092553191");
    assertThat(result.getWarnings()).extracting("code").doesNotContain("FX-001");
  }

  @Test
  void usdFund_fxUnavailable_emitsWarning_andComputesUnconverted() {
    PortfolioHolding usFund = holding("VFINX", FinancialInstrumentType.MUTUAL_FUND_US, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        usFund, FeeData.builder()
            .managementFee(new BigDecimal("0.015"))
            .currency(Currency.USD)
            .build());
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      Map<Currency, BigDecimal> m = new EnumMap<>(Currency.class);
      m.put(Currency.USD, null);
      return m;
    });

    ManagementFeeResult result = service.perform(commandFor(List.of(usFund), FUNDS_ONLY), securityData);

    assertThat(result.getManagementFee().get(FUNDS_ONLY)).isEqualByComparingTo("0.015");
    assertThat(result.getWarnings()).extracting("code").contains("FX-001");
  }

  @Test
  void merBearingHoldingWithMissingCurrency_throws() {
    PortfolioHolding fund = holding("CIG-NOCURR", FinancialInstrumentType.MUTUAL_FUND_CANADA, "1000");
    Map<PortfolioHolding, FeeData> securityData = Map.of(
        fund, FeeData.builder()
            .managementFee(new BigDecimal("0.015"))
            // currency intentionally null
            .build());

    assertThatThrownBy(() -> service.perform(commandFor(List.of(fund), FUNDS_ONLY), securityData))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("missing Currency");
  }

  private static PortfolioHolding holding(String id, FinancialInstrumentType type, String value) {
    return PortfolioHolding.builder()
        .value(new BigDecimal(value))
        .holdingType(type)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.MORNINGSTAR_ID))
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
