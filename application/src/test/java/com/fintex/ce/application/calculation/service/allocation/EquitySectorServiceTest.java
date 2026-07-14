package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.EquitySectorResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_SECTOR_ALLOCATION;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class EquitySectorServiceTest {

  @Test
  void shouldReturnPerHoldingSectorExposures_whenSecurityDataResolved() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorService.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    final var equitySector = new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, TEN));
    when(command.getHoldings()).thenReturn(List.of(holding));
    when(command.getDataProviders()).thenReturn(List.of(DataProvider.MORNINGSTAR));
    when(fetcher.fetch(any(), any())).thenReturn(Map.of(holding, equitySector));

    doCallRealMethod().when(service).fetchExposures(any());
    final var result = service.fetchExposures(command);
    final var actual = result.allocations();

    assertTrue(actual.containsKey(holding));
    assertEquals(TEN, actual.get(holding).get(EquitySectorAllocationType.TECHNOLOGY));
    assertTrue(result.warnings().isEmpty());
    verify(fetcher).fetch(List.of(holding), List.of(DataProvider.MORNINGSTAR));
  }

  @Test
  void shouldEmitWarningAndReturnUnknownAllocation_whenSecurityDataIsMissing() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorService.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    when(fetcher.fetch(any(), any())).thenReturn(Map.of());

    doCallRealMethod().when(service).fetchExposures(any());
    doCallRealMethod().when(service).getMetric();
    final var result = service.fetchExposures(command);

    assertEquals(1, result.warnings().size());
    assertTrue(result.warnings().stream().anyMatch(w -> SECURITY_NOT_FOUND_FOR_METRIC.getCode().equals(w
        .getCode())));
    assertTrue(result.allocations().containsKey(holding));
    assertEquals(0,
        result.allocations().get(holding).get(EquitySectorAllocationType.UNKNOWN).compareTo(BigDecimal.ONE));
  }

  @ParameterizedTest
  @MethodSource("filteredInstrumentTypes")
  void shouldExcludeHoldingFromExposures_whenHoldingIsCashOrGic(FinancialInstrumentType filteredType) {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorService.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var filteredHolding = mock(PortfolioHolding.class);
    when(filteredHolding.getHoldingType()).thenReturn(filteredType);
    final var equityHolding = mock(PortfolioHolding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    final var equitySector = new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, TEN));
    when(command.getHoldings()).thenReturn(List.of(filteredHolding, equityHolding));
    when(fetcher.fetch(any(), any())).thenReturn(Map.of(equityHolding, equitySector));

    doCallRealMethod().when(service).fetchExposures(any());
    final var result = service.fetchExposures(command);
    final var actual = result.allocations();

    assertEquals(1, actual.size());
    assertFalse(actual.containsKey(filteredHolding));
    assertTrue(actual.containsKey(equityHolding));
    assertEquals(TEN, actual.get(equityHolding).get(EquitySectorAllocationType.TECHNOLOGY));
    assertTrue(result.warnings().isEmpty());
  }

  static Stream<FinancialInstrumentType> filteredInstrumentTypes() {
    return Stream.of(FinancialInstrumentType.CASH, FinancialInstrumentType.GIC, FinancialInstrumentType.GIC_CANADA);
  }

  @Test
  void shouldReturnEmptyResponse_whenSectorsMapIsEmpty() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorService.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var expected = EquitySectorResult.builder()
        .equitySector(Map.of())
        .warnings(List.of())
        .build();
    when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

    doCallRealMethod().when(service).calculate(any(), any());
    final var actual = service.calculate(new ExposureDataHolder<>(Map.of(), List.of()), List.of());

    assertEquals(expected, actual);
    verify(responseMapper).toEmptyResponse(any());
    verify(service, never()).calculateNetProducts(any(), any(), any());
  }

  @Test
  void shouldComputeUnknownAllocation_forPortfolioWithFieldMissingAndSmUnresolvedHoldings() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = new EquitySectorResponseMapper();
    final var service = new EquitySectorService(fetcher, responseMapper);

    PortfolioHolding resolved = PortfolioHolding.builder()
        .value(BigDecimal.valueOf(50))
        .holdingType(FinancialInstrumentType.STOCK_US)
        .securityIdentifier(new SecurityIdentifier("AAPL", FiIdentifierType.TICKER))
        .build();
    PortfolioHolding fieldMissing = PortfolioHolding.builder()
        .value(BigDecimal.valueOf(30))
        .holdingType(FinancialInstrumentType.STOCK_US)
        .securityIdentifier(new SecurityIdentifier("XYZ", FiIdentifierType.TICKER))
        .build();
    PortfolioHolding notFoundBySm = PortfolioHolding.builder()
        .value(BigDecimal.valueOf(20))
        .holdingType(FinancialInstrumentType.STOCK_US)
        .securityIdentifier(new SecurityIdentifier("ZZZ", FiIdentifierType.TICKER))
        .build();

    final var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(resolved, fieldMissing, notFoundBySm));
    when(command.getDataProviders()).thenReturn(List.of());
    when(fetcher.fetch(any(), any())).thenReturn(Map.of(
        resolved, new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, BigDecimal.ONE)),
        fieldMissing, new EquitySector(Map.of())));

    EquitySectorResult result = service.perform(command);

    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.UNKNOWN)
        .compareTo(new BigDecimal("0.5000000000")));
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.TECHNOLOGY)
        .compareTo(new BigDecimal("0.5000000000")));
    assertEquals(2, result.getWarnings().size());
    assertEquals(1, result.getWarnings().stream()
        .filter(w -> SECURITY_NOT_FOUND_FOR_METRIC.getCode().equals(w.getCode())).count());
    assertEquals(1, result.getWarnings().stream()
        .filter(w -> MISSING_EQUITY_SECTOR_ALLOCATION.getCode().equals(w.getCode())).count());
  }

  @Test
  void shouldNormalizeSectorsAndUnknownAllocation_toSumToOne_whenPortfolioHasCash() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = new EquitySectorResponseMapper();
    final var service = new EquitySectorService(fetcher, responseMapper);

    PortfolioHolding techStock = PortfolioHolding.builder()
        .value(BigDecimal.valueOf(50))
        .holdingType(FinancialInstrumentType.STOCK_US)
        .securityIdentifier(new SecurityIdentifier("AAPL", FiIdentifierType.TICKER))
        .build();
    PortfolioHolding notFoundBySm = PortfolioHolding.builder()
        .value(BigDecimal.valueOf(20))
        .holdingType(FinancialInstrumentType.STOCK_US)
        .securityIdentifier(new SecurityIdentifier("ZZZ", FiIdentifierType.TICKER))
        .build();
    CashHolding cash = CashHolding.builder()
        .value(BigDecimal.valueOf(30))
        .holdingType(FinancialInstrumentType.CASH)
        .securityIdentifier(new SecurityIdentifier("CASH-CAD", FiIdentifierType.MORNINGSTAR_ID))
        .currency(Currency.CAD)
        .build();

    final var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(techStock, notFoundBySm, cash));
    when(command.getDataProviders()).thenReturn(List.of());
    when(fetcher.fetch(any(), any())).thenReturn(Map.of(
        techStock, new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, BigDecimal.ONE))));

    EquitySectorResult result = service.perform(command);

    // Cash is 30% of the whole portfolio and never enters the sector map (fetchExposures filters it out), but the
    // equity universe (tech stock 50 + unresolved stock 20 = 70) must still be fully accounted for: TECHNOLOGY = 50/70
    // and UNKNOWN = 20/70, summing to 100% rather than 70%.
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.TECHNOLOGY)
        .compareTo(new BigDecimal("0.7142857143")));
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.UNKNOWN)
        .compareTo(new BigDecimal("0.2857142857")));
    BigDecimal sum = result.getEquitySector().get(EquitySectorAllocationType.TECHNOLOGY)
        .add(result.getEquitySector().get(EquitySectorAllocationType.UNKNOWN));
    assertEquals(0, sum.compareTo(BigDecimal.ONE));
  }

}
