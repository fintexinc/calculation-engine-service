package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.service.GeographicAllocationMappingService;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.PortfolioUtils;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static java.math.BigDecimal.TEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityGeographicExposureCalculationServiceImplTest {

  @SuppressWarnings("unchecked")
  @Test
  void shouldFetch_whenCheckResult() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
    final var sut = mock(EquityGeographicExposureCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, geographicAllocationMappingService));

    final var holding = mock(Holding.class);
    final var rawAllocation = new EquityCountryAllocation();
    rawAllocation.setAllocations(Map.of("CA", TEN));
    final var rawData = Map.of(holding, rawAllocation);
    final var mappedResult = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));

    when(fetcher.fetch(any(), any())).thenReturn(rawData);
    when(geographicAllocationMappingService.mapToGeographicRegions(any(), any(), any())).thenReturn(mappedResult);
    doCallRealMethod().when(sut).fetchExposures(any(), any());
    final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

    Assertions.assertEquals(mappedResult, actual);
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
      final var sut = mock(EquityGeographicExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(fetcher, geographicAllocationMappingService));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, holdings, List.of());

      verify(sut).calculateNetProducts(exposures, holdings, GeographicRegionType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
      final var sut = mock(EquityGeographicExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(fetcher, geographicAllocationMappingService));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(sut.calculateNetProducts(exposures, holdings, GeographicRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, holdings, List.of());

      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyToUserScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class);
        var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
      final var sut = mock(EquityGeographicExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(fetcher, geographicAllocationMappingService));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(netProducts);

      when(sut.calculateNetProducts(exposures, holdings, GeographicRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, holdings, List.of());

      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(netProducts));
    }
  }

}
