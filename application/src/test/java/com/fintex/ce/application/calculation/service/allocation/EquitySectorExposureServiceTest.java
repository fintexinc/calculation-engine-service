package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.application.mapping.response.EquitySectorResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

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
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EquitySectorExposureServiceTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final EquitySectorExposureService service = new EquitySectorExposureService(new EquitySectorResponseMapper(),
      new PortfolioWeightCalculator(new DefaultTargetCurrencyConverter(fxRateService, new FxProperties())));

  private PortfolioHolding fund(BigDecimal value) {
    var holding = mock(PortfolioHolding.class);
    when(holding.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    when(holding.getValue()).thenReturn(value);
    return holding;
  }

  private PortfolioHoldingsCommand commandOf(List<PortfolioHolding> holdings) {
    var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(holdings);
    return command;
  }

  @Test
  void shouldEmitWarningAndPopulateUnknownBucket_whenSecurityDataIsMissing() {
    var holding = fund(BigDecimal.valueOf(100));
    when(holding.getIdsString()).thenReturn("FUND-1");

    var result = service.perform(commandOf(List.of(holding)), Map.of());

    assertEquals(1, result.getWarnings().size());
    assertEquals(SECURITY_NOT_FOUND_FOR_METRIC.getCode(), result.getWarnings().get(0).getCode());
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.UNKNOWN).compareTo(ONE));
  }

  @Test
  void shouldEmitWarningAndPopulateUnknownBucket_whenAllocationsAreEmpty() {
    var holding = fund(BigDecimal.valueOf(100));
    when(holding.getIdsString()).thenReturn("FUND-1");
    var data = Map.of(holding, new EquitySector(Map.of(), Currency.CAD));

    var result = service.perform(commandOf(List.of(holding)), data);

    assertEquals(1, result.getWarnings().size());
    assertEquals(MISSING_EQUITY_SECTOR_ALLOCATION.getCode(), result.getWarnings().get(0).getCode());
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.UNKNOWN).compareTo(ONE));
  }

  @Test
  void shouldReturnEmptyResponse_whenOnlyCashHoldings() {
    var cash = CashHolding.builder().value(BigDecimal.valueOf(100)).holdingType(FinancialInstrumentType.CASH)
        .currency(Currency.CAD).build();

    var result = service.perform(commandOf(List.of(cash)), Map.of());

    assertTrue(result.getWarnings().isEmpty());
    assertNull(result.getEquitySector().get(EquitySectorAllocationType.TECHNOLOGY));
  }

  @Test
  void shouldAggregateSectorAllocations_forFundHolding() {
    var fund = fund(BigDecimal.valueOf(100));
    var data = Map.of(fund, new EquitySector(Map.of(
        EquitySectorAllocationType.TECHNOLOGY, new BigDecimal("0.7"),
        EquitySectorAllocationType.FINANCIAL_SERVICES, new BigDecimal("0.3")), Currency.CAD));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());

    var result = service.perform(commandOf(List.of(fund)), data);

    var sectors = result.getEquitySector();
    assertEquals(0, sectors.get(EquitySectorAllocationType.TECHNOLOGY).compareTo(new BigDecimal("0.7")));
    assertEquals(0, sectors.get(EquitySectorAllocationType.FINANCIAL_SERVICES).compareTo(new BigDecimal("0.3")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldFxWeightSectorSplit_acrossTwoCurrencyFunds() {
    var cadFund = fund(BigDecimal.valueOf(100));
    var usdFund = fund(BigDecimal.valueOf(100));
    var data = Map.of(
        cadFund, new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, ONE), Currency.CAD),
        usdFund, new EquitySector(Map.of(EquitySectorAllocationType.FINANCIAL_SERVICES, ONE), Currency.USD));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    var result = service.perform(commandOf(List.of(cadFund, usdFund)), data);

    // cad=100 CAD, usd=100 USD * 1.5 = 150 CAD -> weight(cad)=0.4, weight(usd)=0.6
    var sectors = result.getEquitySector();
    assertEquals(0, sectors.get(EquitySectorAllocationType.TECHNOLOGY).compareTo(new BigDecimal("0.4")));
    assertEquals(0, sectors.get(EquitySectorAllocationType.FINANCIAL_SERVICES).compareTo(new BigDecimal("0.6")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldFallBackToRawValues_whenFxRateIsMissing() {
    var cadFund = fund(BigDecimal.valueOf(100));
    var usdFund = fund(BigDecimal.valueOf(100));
    var data = Map.of(
        cadFund, new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, ONE), Currency.CAD),
        usdFund, new EquitySector(Map.of(EquitySectorAllocationType.FINANCIAL_SERVICES, ONE), Currency.USD));
    Map<Currency, BigDecimal> noRate = new EnumMap<>(Currency.class);
    noRate.put(Currency.USD, null);
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(noRate);

    var result = service.perform(commandOf(List.of(cadFund, usdFund)), data);

    // no FX rate for USD -> both weighted by raw value: 100/200 = 0.5 each
    var sectors = result.getEquitySector();
    assertEquals(0, sectors.get(EquitySectorAllocationType.TECHNOLOGY).compareTo(new BigDecimal("0.5")));
    assertEquals(0, sectors.get(EquitySectorAllocationType.FINANCIAL_SERVICES).compareTo(new BigDecimal("0.5")));
    assertEquals(1, result.getWarnings().size());
    assertEquals(FX_RATES_UNAVAILABLE.getCode(), result.getWarnings().get(0).getCode());
  }

  @ParameterizedTest
  @MethodSource("cashAndGicHoldings")
  void shouldExcludeHoldingFromSectors_whenHoldingIsCashOrGic(PortfolioHolding excluded) {
    var fund = fund(BigDecimal.valueOf(100));
    var data = Map.of(fund, new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, ONE), Currency.CAD));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of());

    var result = service.perform(commandOf(List.of(excluded, fund)), data);

    var sectors = result.getEquitySector();
    assertEquals(0, sectors.get(EquitySectorAllocationType.TECHNOLOGY).compareTo(ONE));
    assertEquals(0, sectors.get(EquitySectorAllocationType.UNKNOWN).compareTo(ZERO));
    assertTrue(result.getWarnings().isEmpty());
  }

  static Stream<PortfolioHolding> cashAndGicHoldings() {
    return Stream.of(
        CashHolding.builder().value(BigDecimal.valueOf(100)).holdingType(FinancialInstrumentType.CASH)
            .currency(Currency.CAD).build(),
        GicHolding.builder().value(BigDecimal.valueOf(100)).holdingType(FinancialInstrumentType.GIC)
            .currency(Currency.CAD).build());
  }
}
