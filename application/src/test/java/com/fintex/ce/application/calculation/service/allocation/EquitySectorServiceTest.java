package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.EquitySectorResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_SECTOR_ALLOCATION;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class EquitySectorServiceTest {

  @Test
  void shouldFetch_whenCheckResult() {
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorService.class, withSettings()
        .useConstructor(responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    final var equitySector = new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, TEN));
    when(command.getHoldings()).thenReturn(List.of(holding));
    final var data = Map.of(holding, equitySector);

    doCallRealMethod().when(service).fetchExposures(any(), any());
    final var result = service.fetchExposures(command, data);
    final var actual = result.allocations();

    assertTrue(actual.containsKey(holding));
    assertEquals(TEN, actual.get(holding).get(EquitySectorAllocationType.TECHNOLOGY));
    assertTrue(result.warnings().isEmpty());
  }

  @Test
  void shouldEmitWarning_whenSecurityIsUnknown() {
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorService.class, withSettings()
        .useConstructor(responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));

    doCallRealMethod().when(service).getMetric();
    doCallRealMethod().when(service).fetchExposures(any(), any());
    final var result = service.fetchExposures(command, Map.of());

    assertEquals(1, result.warnings().size());
    assertEquals(SECURITY_NOT_FOUND_FOR_METRIC.getCode(), result.warnings().get(0).getCode());
    assertTrue(result.allocations().containsKey(holding));
    assertEquals(0,
        result.allocations().get(holding).get(EquitySectorAllocationType.UNKNOWN).compareTo(ONE));
  }

  @Test
  void shouldEmitWarning_whenAllocationsAreEmpty() {
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorService.class, withSettings()
        .useConstructor(responseMapper));

    final var holding = mock(PortfolioHolding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    final var data = Map.of(holding, new EquitySector(Map.of()));

    doCallRealMethod().when(service).fetchExposures(any(), any());
    final var result = service.fetchExposures(command, data);

    assertEquals(1, result.warnings().size());
    assertEquals(MISSING_EQUITY_SECTOR_ALLOCATION.getCode(), result.warnings().get(0).getCode());
    assertTrue(result.allocations().containsKey(holding));
    assertEquals(0,
        result.allocations().get(holding).get(EquitySectorAllocationType.UNKNOWN).compareTo(ONE));
  }

  @ParameterizedTest
  @MethodSource("filteredInstrumentTypes")
  void shouldExcludeHoldingFromExposures_whenHoldingIsCashOrGic(FinancialInstrumentType filteredType) {
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorService.class, withSettings()
        .useConstructor(responseMapper));

    final var filteredHolding = mock(PortfolioHolding.class);
    when(filteredHolding.getHoldingType()).thenReturn(filteredType);
    final var equityHolding = mock(PortfolioHolding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    final var equitySector = new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, TEN));
    when(command.getHoldings()).thenReturn(List.of(filteredHolding, equityHolding));
    final var data = Map.of(equityHolding, equitySector);

    doCallRealMethod().when(service).fetchExposures(any(), any());
    final var result = service.fetchExposures(command, data);
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
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var service = mock(EquitySectorService.class, withSettings()
          .useConstructor(responseMapper));

      final var holding = mock(PortfolioHolding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquitySectorAllocationType.CONSUMER_DEFENSIVE, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(service).calculateNetProducts(exposures, holdings, EquitySectorAllocationType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyResponseMapperFromNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var service = mock(EquitySectorService.class, withSettings()
          .useConstructor(responseMapper));

      final var holding = mock(PortfolioHolding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquitySectorAllocationType.CONSUMER_DEFENSIVE, TEN));
      @SuppressWarnings("unchecked")
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(service.calculateNetProducts(exposures, holdings, EquitySectorAllocationType.values())).thenReturn(
          netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(responseMapper).fromNetProducts(any(), any());
    }
  }

  @Test
  void shouldCalculate_whenExposuresAreEmpty() {
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorService.class, withSettings()
        .useConstructor(responseMapper));

    final var expected = EquitySectorResult.builder()
        .equitySector(Map.of())
        .warnings(List.of())
        .build();
    when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

    doCallRealMethod().when(service).calculate(any(), any());
    final var actual = service.calculate(new ExposureDataHolder<>(Map.of(), List.of()), List.of());

    assertEquals(expected, actual);
    verify(responseMapper).toEmptyResponse(any());
  }

}
