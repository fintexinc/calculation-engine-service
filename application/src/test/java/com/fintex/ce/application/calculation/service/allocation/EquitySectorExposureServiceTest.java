package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_SECTOR_ALLOCATION;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType.ENERGY;
import static com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType.FINANCIAL_SERVICES;
import static com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType.TECHNOLOGY;
import static com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType.UNKNOWN;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EquitySectorExposureServiceTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final EquitySectorExposureService service = new EquitySectorExposureService(
      new PortfolioWeightCalculator(new DefaultTargetCurrencyConverter(fxRateService, new FxProperties())));

  @Test
  void shouldAggregateSectorDistribution_whenHoldingIsFund() {
    var fund = fund("RBF605", "100");
    var data = Map.of(fund, sector(Currency.CAD, Map.of(TECHNOLOGY, new BigDecimal("0.7"),
        FINANCIAL_SERVICES, new BigDecimal("0.3"))));

    var result = service.perform(command(fund), data);

    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("0.7");
    assertThat(result.getEquitySector().get(FINANCIAL_SERVICES)).isEqualByComparingTo("0.3");
    assertThat(result.getWarnings()).isEmpty();
  }

  /**
   * TMI-475: a stock is delivered by Security Master through the same EQUITY_SECTOR_ALLOCATION attribute as a single
   * 100% bucket, so it flows through the fund path unchanged and contributes fully to its one sector.
   */
  @Test
  void shouldContributeFullWeightToOneSector_whenHoldingIsStockWithSingleBucket() {
    var stock = stock("T", "100");
    var data = Map.of(stock, sector(Currency.CAD, Map.of(ENERGY, ONE)));

    var result = service.perform(command(stock), data);

    assertThat(result.getEquitySector().get(ENERGY)).isEqualByComparingTo("1");
    assertThat(result.getEquitySector().get(UNKNOWN)).isEqualByComparingTo("0");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldBlendByValue_whenPortfolioMixesFundAndStock() {
    var fund = fund("RBF605", "100");
    var stock = stock("T", "100");
    var data = Map.of(
        fund, sector(Currency.CAD, Map.of(TECHNOLOGY, ONE)),
        stock, sector(Currency.CAD, Map.of(ENERGY, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());

    var result = service.perform(command(fund, stock), data);

    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("0.5");
    assertThat(result.getEquitySector().get(ENERGY)).isEqualByComparingTo("0.5");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldWarnAndBucketUnknown_whenStockSectorMissing() {
    var stock = stock("T", "100");

    var result = service.perform(command(stock), Map.of());

    assertThat(result.getEquitySector().get(UNKNOWN)).isEqualByComparingTo("1");
    assertThat(result.getWarnings()).extracting("code").containsExactly(SECURITY_NOT_FOUND_FOR_METRIC.getCode());
  }

  @Test
  void shouldWarnAndBucketUnknown_whenFundAllocationsAreEmpty() {
    var fund = fund("RBF605", "100");
    var data = Map.of(fund, sector(Currency.CAD, Map.of()));

    var result = service.perform(command(fund), data);

    assertThat(result.getEquitySector().get(UNKNOWN)).isEqualByComparingTo("1");
    assertThat(result.getWarnings()).extracting("code").containsExactly(MISSING_EQUITY_SECTOR_ALLOCATION.getCode());
  }

  @Test
  void shouldReturnAllNullBuckets_whenOnlyCashAndGicHoldings() {
    var cash = CashHolding.builder().value(new BigDecimal("100")).holdingType(FinancialInstrumentType.CASH)
        .currency(Currency.CAD).build();
    var gic = GicHolding.builder().value(new BigDecimal("100")).holdingType(FinancialInstrumentType.GIC)
        .currency(Currency.CAD).build();

    var result = service.perform(command(cash, gic), Map.of());

    assertThat(result.getEquitySector().values()).containsOnlyNulls();
    assertThat(result.getWarnings()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("cashAndGicHoldings")
  void shouldExcludeHoldingFromWeighting_whenHoldingIsCashOrGic(PortfolioHolding excluded) {
    var fund = fund("RBF605", "100");
    var data = Map.of(fund, sector(Currency.CAD, Map.of(TECHNOLOGY, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());

    var result = service.perform(command(excluded, fund), data);

    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("1");
    assertThat(result.getEquitySector().get(UNKNOWN)).isEqualByComparingTo("0");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldFxWeightSectors_whenHoldingsHaveDifferentCurrencies() {
    var cadFund = fund("CAD-1", "100");
    var usdFund = fund("USD-1", "100");
    var data = Map.of(
        cadFund, sector(Currency.CAD, Map.of(TECHNOLOGY, ONE)),
        usdFund, sector(Currency.USD, Map.of(FINANCIAL_SERVICES, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    var result = service.perform(command(cadFund, usdFund), data);

    // cad=100 CAD, usd=100 USD * 1.5 = 150 CAD -> weights 0.4 / 0.6
    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("0.4");
    assertThat(result.getEquitySector().get(FINANCIAL_SERVICES)).isEqualByComparingTo("0.6");
    assertThat(result.getWarnings()).isEmpty();
  }

  @Test
  void shouldWeightByRawValueAndWarn_whenFxRateUnavailable() {
    var cadFund = fund("CAD-1", "100");
    var usdFund = fund("USD-1", "100");
    var data = Map.of(
        cadFund, sector(Currency.CAD, Map.of(TECHNOLOGY, ONE)),
        usdFund, sector(Currency.USD, Map.of(FINANCIAL_SERVICES, ONE)));
    Map<Currency, BigDecimal> noRate = new EnumMap<>(Currency.class);
    noRate.put(Currency.USD, null);
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(noRate);

    var result = service.perform(command(cadFund, usdFund), data);

    // no USD rate -> both weighted by raw value: 100/200 = 0.5 each
    assertThat(result.getEquitySector().get(TECHNOLOGY)).isEqualByComparingTo("0.5");
    assertThat(result.getEquitySector().get(FINANCIAL_SERVICES)).isEqualByComparingTo("0.5");
    assertThat(result.getWarnings()).extracting("code").containsExactly(FX_RATES_UNAVAILABLE.getCode());
  }

  static Stream<PortfolioHolding> cashAndGicHoldings() {
    return Stream.of(
        CashHolding.builder().value(new BigDecimal("100")).holdingType(FinancialInstrumentType.CASH)
            .currency(Currency.CAD).build(),
        GicHolding.builder().value(new BigDecimal("100")).holdingType(FinancialInstrumentType.GIC)
            .currency(Currency.CAD).build());
  }

  private static PortfolioHoldingsCommand command(PortfolioHolding... holdings) {
    return PortfolioHoldingsCommand.builder().holdings(List.of(holdings)).build();
  }

  private static PortfolioHolding fund(String id, String value) {
    return holding(FinancialInstrumentType.MUTUAL_FUND_CANADA, id, value);
  }

  private static PortfolioHolding stock(String id, String value) {
    return holding(FinancialInstrumentType.STOCK_CANADA, id, value);
  }

  private static PortfolioHolding holding(FinancialInstrumentType type, String id, String value) {
    return PortfolioHolding.builder()
        .value(new BigDecimal(value))
        .holdingType(type)
        .securityIdentifier(new SecurityIdentifier(id, FiIdentifierType.TICKER))
        .build();
  }

  private static EquitySector sector(Currency currency, Map<EquitySectorAllocationType, BigDecimal> allocations) {
    return new EquitySector(allocations, currency);
  }
}
