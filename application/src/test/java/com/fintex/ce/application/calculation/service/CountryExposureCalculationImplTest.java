package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.CountryExposureResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.CountryExposureResult;
import com.fintex.ce.mapping.CountryAllocationMappingService;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CountryExposureCalculationImplTest {

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var storage = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      final var service = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper, countryAllocationMappingService));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var storage = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      final var service = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper, countryAllocationMappingService));

      final var exposures = mock(Map.class);
      final var expected = new CountryExposureResult();
      expected.setCountryExposure(CountryExposureCalculationImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any());
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      assertEquals(expected, actual);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldFetch_whenCheckResult() {
    final var storage = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(CountryExposureResponseMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
    final var service = mock(CountryExposureCalculationImpl.class,
        withSettings().useConstructor(storage, responseMapper, countryAllocationMappingService));

    final var holding = mock(Holding.class);
    final var countryExposure = new CountryExposure();
    countryExposure.setAllocations(Map.of("CA", TEN));
    final var rawData = Map.of(holding, countryExposure);
    final var mappedResult = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));

    when(storage.fetch(any(), any())).thenReturn(rawData);
    when(countryAllocationMappingService.mapToCountryRegions(any(), any(), any())).thenReturn(mappedResult);
    doCallRealMethod().when(service).fetchExposures(any());
    final var result = service.fetchExposures(mock(PortfolioHoldingsCommand.class));
    final var actual = result.allocations();

    assertEquals(mappedResult, actual);
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var storage = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      final var service = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper, countryAllocationMappingService));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(false);
      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(service).calculateNetProducts(exposures, holdings, CountryRegionType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var storage = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      final var service = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper, countryAllocationMappingService));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(false);
      when(service.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(responseMapper).fromNetProducts(any(), any());
    }
  }

  @Test
  void shouldCalculate_whenVerifyResponseMapperFromNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var storage = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      final var service = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper, countryAllocationMappingService));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(false);
      when(service.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(responseMapper).fromNetProducts(any(), any());
    }
  }

}
