package com.fintex.ce.application.calculation.service;

import com.fintex.ce.mapping.CountryAllocationMappingService;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityCountryExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityCountryExposureCalculationServiceImplTest {

  @SuppressWarnings("unchecked")
  private final SecurityDataFetcher<EquityCountryAllocation> securityDataPort = mock(SecurityDataFetcher.class);
  private final CountryAllocationMappingService countryAllocationMappingService = mock(
      CountryAllocationMappingService.class);

  @Test
  void shouldPerform_whenVerifyValidateHoldings() {
    final var service = mock(EquityCountryExposureCalculationServiceImpl.class,
        withSettings().useConstructor(securityDataPort, countryAllocationMappingService));

    final PortfolioHoldingsCommand req = mock(PortfolioHoldingsCommand.class);
    final List<PortfolioHolding> holdings = List.of(mock(PortfolioHolding.class));
    when(req.getHoldings()).thenReturn(holdings);
    when(service.fetchExposures(any())).thenReturn(new ExposureDataHolder<>(Map.of(), List.of()));

    doCallRealMethod().when(service).perform(any());
    service.perform(req);
  }

  @Test
  void shouldCalculateNetProduct_whenCheckResult() {
    final EquityCountryExposureCalculationServiceImpl e = mock(EquityCountryExposureCalculationServiceImpl.class);

    final CountryRegionType canada = CountryRegionType.CANADA;

    final PortfolioHolding h1 = new PortfolioHolding(BigDecimal.ONE, FinancialInstrumentType.CASH, null);
    final PortfolioHolding h2 = new PortfolioHolding(BigDecimal.TEN, FinancialInstrumentType.ETF_US, null);

    final Map<PortfolioHolding, Map<CountryRegionType, BigDecimal>> exposures = Map.of(
        h1, Map.of(canada, BigDecimal.valueOf(2), CountryRegionType.EMERGING_MARKET, BigDecimal.valueOf(21)),
        h2, Map.of(canada, BigDecimal.valueOf(5)));

    doCallRealMethod().when(e).calculateNetProduct(any(), any(), any());
    final BigDecimal actual = e.calculateNetProduct(canada, exposures, Map.of(h1, BigDecimal.valueOf(3), h2, BigDecimal
        .valueOf(4)));

    assertEquals(0, BigDecimal.valueOf(26).compareTo(actual));
  }

  @Test
  void shouldCalculateNetProduct_whenCheckResult2() {
    final EquityCountryExposureCalculationServiceImpl e = mock(EquityCountryExposureCalculationServiceImpl.class);

    final CountryRegionType type = CountryRegionType.EMERGING_MARKET;

    final PortfolioHolding h1 = new PortfolioHolding(BigDecimal.ONE, FinancialInstrumentType.ETF_US, null);
    final PortfolioHolding h2 = new PortfolioHolding(BigDecimal.TEN, FinancialInstrumentType.MUTUAL_FUND_CANADA, null);

    final Map<PortfolioHolding, Map<CountryRegionType, BigDecimal>> exposures = Map.of(
        h1, Map.of(type, BigDecimal.valueOf(2), CountryRegionType.CANADA, BigDecimal.valueOf(21)),
        h2, Map.of(CountryRegionType.CANADA, BigDecimal.valueOf(5)));

    doCallRealMethod().when(e).calculateNetProduct(any(), any(), any());
    final BigDecimal actual = e.calculateNetProduct(type, exposures, Map.of(h1, BigDecimal.valueOf(3), h2, BigDecimal
        .valueOf(4)));

    assertEquals(0, BigDecimal.valueOf(6).compareTo(actual));
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityCountryExposureCalculationServiceImpl.class,
          withSettings().useConstructor(securityDataPort, countryAllocationMappingService));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityCountryExposureCalculationServiceImpl.class,
          withSettings().useConstructor(securityDataPort, countryAllocationMappingService));

      final var exposures = mock(Map.class);
      final var expected = new EquityCountryExposureResult();
      expected.setEquityCountryExposure(EquityCountryExposureCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(true);

      doCallRealMethod().when(service).calculate(any(), any());
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldFetchExposures_whenCheckResult() {
    final var service = mock(EquityCountryExposureCalculationServiceImpl.class,
        withSettings().useConstructor(securityDataPort, countryAllocationMappingService));

    final var holding = mock(PortfolioHolding.class);
    final var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(List.of(holding));
    when(command.getDataProviders()).thenReturn(List.of());

    final var allocation = new EquityCountryAllocation()
        .setAllocations(Map.of("CAN", BigDecimal.valueOf(0.65)));
    when(securityDataPort.fetch(any(), any())).thenReturn(Map.of(holding, allocation));

    final var expected = Map.of(holding, Map.of(CountryRegionType.CANADA, BigDecimal.valueOf(0.65)));
    when(countryAllocationMappingService.mapToCountryRegions(any(), anyList(), any())).thenReturn(expected);

    doCallRealMethod().when(service).fetchExposures(any());
    final var result = service.fetchExposures(command);
    final var actual = result.allocations();

    assertEquals(expected, actual);
    assertTrue(actual.containsKey(holding));
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityCountryExposureCalculationServiceImpl.class);

      final var holding = mock(PortfolioHolding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(service).calculateNetProducts(exposures, holdings, CountryRegionType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityCountryExposureCalculationServiceImpl.class);

      final var holding = mock(PortfolioHolding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(service.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyToUserScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityCountryExposureCalculationServiceImpl.class);

      final var holding = mock(PortfolioHolding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(anyMap())).thenReturn(false);
      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(netProducts);

      when(service.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(netProducts));
    }
  }

}