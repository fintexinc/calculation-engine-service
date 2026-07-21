package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.CountryAllocationMappingService;
import com.fintex.ce.application.mapping.response.CountryExposureResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.CountryExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.error.Notification;

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

class FixedIncomeCountryExposureServiceTest {

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var responseMapper = mock(CountryExposureResponseMapper.class);
      var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      var service = mock(FixedIncomeCountryExposureService.class,
          withSettings().useConstructor(responseMapper, countryAllocationMappingService));

      var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var responseMapper = mock(CountryExposureResponseMapper.class);
      var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      var service = mock(FixedIncomeCountryExposureService.class,
          withSettings().useConstructor(responseMapper, countryAllocationMappingService));

      var exposures = mock(Map.class);
      var expected = CountryExposureResult.builder()
          .countryExposure(FixedIncomeCountryExposureService.DEFAULT_MAP)
          .warnings(List.of())
          .build();
      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(service).calculate(any(), any());
      var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      assertEquals(expected, actual);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldFetch_whenCheckResult() {
    var responseMapper = mock(CountryExposureResponseMapper.class);
    var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
    var service = mock(FixedIncomeCountryExposureService.class,
        withSettings().useConstructor(responseMapper, countryAllocationMappingService));

    var holding = mock(PortfolioHolding.class);
    var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    var countryExposure = new CountryExposure();
    countryExposure.setAllocations(Map.of(Country.CANADA, TEN));
    var data = Map.of(holding, countryExposure);
    var mappedAllocations = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
    var mappedResult = new ExposureDataHolder<>(mappedAllocations, List.<Notification>of());

    when(countryAllocationMappingService.mapToCountryRegions(any(), any())).thenReturn(mappedResult);
    doCallRealMethod().when(service).fetchExposures(any(), any());
    var result = service.fetchExposures(command, data);
    var actual = result.allocations();

    assertEquals(mappedAllocations, actual);
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var responseMapper = mock(CountryExposureResponseMapper.class);
      var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      var service = mock(FixedIncomeCountryExposureService.class,
          withSettings().useConstructor(responseMapper, countryAllocationMappingService));

      var holding = mock(PortfolioHolding.class);
      var holdings = List.of(holding);
      var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(false);
      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(service).calculateNetProducts(exposures, holdings, CountryRegionType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      var responseMapper = mock(CountryExposureResponseMapper.class);
      var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      var service = mock(FixedIncomeCountryExposureService.class,
          withSettings().useConstructor(responseMapper, countryAllocationMappingService));

      var holding = mock(PortfolioHolding.class);
      var holdings = List.of(holding);
      var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      var netProducts = mock(Map.class);

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
      var responseMapper = mock(CountryExposureResponseMapper.class);
      var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
      var service = mock(FixedIncomeCountryExposureService.class,
          withSettings().useConstructor(responseMapper, countryAllocationMappingService));

      var holding = mock(PortfolioHolding.class);
      var holdings = List.of(holding);
      var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(false);
      when(service.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(responseMapper).fromNetProducts(any(), any());
    }
  }

}
