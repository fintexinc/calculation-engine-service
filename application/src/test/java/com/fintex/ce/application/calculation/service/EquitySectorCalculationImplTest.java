package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.EquitySectorResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquitySectorResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("unchecked")
class EquitySectorCalculationImplTest {

  @Test
  void shouldFetch_whenCheckResult() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(EquitySectorResponseMapper.class);
    final var service = mock(EquitySectorCalculationImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(Holding.class);
    final var equitySector = new EquitySector(Map.of(EquitySectorAllocationType.TECHNOLOGY, TEN));
    when(fetcher.fetch(any(), any())).thenReturn(Map.of(holding, equitySector));

    doCallRealMethod().when(service).fetchExposures(any());
    final var result = service.fetchExposures(mock(PortfolioHoldingsCommand.class));
    final var actual = result.allocations();

    Assertions.assertTrue(actual.containsKey(holding));
    Assertions.assertEquals(TEN, actual.get(holding).get(EquitySectorAllocationType.TECHNOLOGY));
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var service = mock(EquitySectorCalculationImpl.class, withSettings()
          .useConstructor(fetcher, responseMapper));

      final var holding = mock(Holding.class);
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
      final var fetcher = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var service = mock(EquitySectorCalculationImpl.class, withSettings()
          .useConstructor(fetcher, responseMapper));

      final var holding = mock(Holding.class);
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
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var service = mock(EquitySectorCalculationImpl.class, withSettings()
          .useConstructor(fetcher, responseMapper));

      @SuppressWarnings("unchecked")
      final var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var service = mock(EquitySectorCalculationImpl.class, withSettings()
          .useConstructor(fetcher, responseMapper));

      @SuppressWarnings("unchecked")
      final var exposures = mock(Map.class);
      final var expected = new EquitySectorResult();
      expected.setEquitySector(Map.of());
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any());
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      Assertions.assertEquals(expected, actual);
      verify(responseMapper).toEmptyResponse(any());
    }
  }

}
