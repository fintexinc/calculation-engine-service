package com.fintex.ce.application.calculation.service;

import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.GeographicExposureResult;
import com.fintex.ce.mapping.GeographicAllocationMappingService;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.PortfolioUtils;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeGeographicExposureCalculationImplTest {

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var storage = mock(SecurityDataFetcher.class);
      final var geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
      final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
          withSettings().useConstructor(storage, geographicAllocationMappingService));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, List.of(), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var storage = mock(SecurityDataFetcher.class);
      final var geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
      final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
          withSettings().useConstructor(storage, geographicAllocationMappingService));

      final var exposures = mock(Map.class);
      final var expected = new GeographicExposureResult();
      expected.setEquityGeographicExposure(FixedIncomeGeographicExposureCalculationImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(true);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final var actual = sut.calculate(exposures, List.of(), List.of());

      assertEquals(expected, actual);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldFetch_whenCheckResult() {
    final var storage = mock(SecurityDataFetcher.class);
    final var geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
    final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
        withSettings().useConstructor(storage, geographicAllocationMappingService));

    final var holding = mock(Holding.class);
    final var countryExposure = new CountryExposure();
    countryExposure.setAllocations(Map.of("CA", TEN));
    final var rawData = Map.of(holding, countryExposure);
    final var mappedResult = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));

    when(storage.fetch(any(), any())).thenReturn(rawData);
    when(geographicAllocationMappingService.mapToGeographicRegions(any(), any(), any())).thenReturn(mappedResult);
    doCallRealMethod().when(sut).fetchExposures(any(), any());
    final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

    assertEquals(mappedResult, actual);
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var storage = mock(SecurityDataFetcher.class);
      final var geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
      final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
          withSettings().useConstructor(storage, geographicAllocationMappingService));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, holdings, List.of());

      verify(sut).calculateNetProducts(exposures, holdings, GeographicRegionType.values());
    }
  }

}
