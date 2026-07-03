package com.fintex.ce.application.mapping;

import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.CountryAllocation;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountryAllocationMappingServiceTest {

  @Test
  void shouldInitCountryAllocationMapping_whenCheckResult() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    doCallRealMethod().when(service).getCountryAllocationInputStream();
    doCallRealMethod().when(service).initCountryAllocationMapping();

    // ACT
    Map<String, CountryAllocation> actual = service.initCountryAllocationMapping();

    // VERIFY
    assertFalse(actual.isEmpty());

    CountryAllocation can = actual.get("CAN");
    assertEquals(CountryRegionType.CANADA, can.getRegion());
    CountryAllocation usa = actual.get("USA");
    assertEquals(CountryRegionType.UNITED_STATES, usa.getRegion());
  }

  @Test
  void shouldInitCountryAllocationMapping_whenVerifyGetCountryAllocationInputStream() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    when(service.getCountryAllocationInputStream()).thenReturn(this.getClass().getResourceAsStream(
        "/jsons/country-allocation-mapping.json"));
    doCallRealMethod().when(service).initCountryAllocationMapping();

    // ACT
    service.initCountryAllocationMapping();

    // VERIFY
    verify(service).getCountryAllocationInputStream();
  }

  @Test
  void shouldInitCountryAllocationMapping_whenVerifyException() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    when(service.getCountryAllocationInputStream()).thenReturn(null);
    doCallRealMethod().when(service).initCountryAllocationMapping();

    // VERIFY
    assertThrows(com.fintex.ce.model.error.exceptions.CalculationException.class,
        service::initCountryAllocationMapping);
  }

  @Test
  void shouldSumAllocations_whenCheckResult() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    Map<CountryRegionType, BigDecimal> map = new EnumMap<>(CountryRegionType.class);

    doCallRealMethod().when(service).sumAllocations(any(), any(), any());

    // ACT
    service.sumAllocations(map, BigDecimal.TEN, CountryRegionType.EMERGING_MARKET);

    // VERIFY
    assertEquals(1, map.size());
    assertTrue(map.containsKey(CountryRegionType.EMERGING_MARKET));
    assertTrue(map.containsValue(BigDecimal.TEN));
  }

  @Test
  void shouldMapToRegions_whenVerifyAllocationEmpty() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    Map<String, BigDecimal> allocations = new HashMap<>();

    service.countryAllocationMap = mock(HashMap.class);

    doCallRealMethod().when(service).mapToRegions(any(), any(), any(), any());

    // ACT
    ArrayList<Notification> warnings = new ArrayList<>();
    service.mapToRegions(mock(PortfolioHolding.class), allocations, warnings, ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    // VERIFY
    assertEquals(1, warnings.size());
    verify(service.countryAllocationMap, times(0)).get(any());
  }

  @Test
  void shouldMapToRegions_whenVerifyGetAllocationNotFound() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);

    service.countryAllocationMap = mock(HashMap.class);

    doCallRealMethod().when(service).mapToRegions(any(), any(), any(), any());

    // ACT
    ArrayList<Notification> warnings = new ArrayList<>();
    service.mapToRegions(mock(PortfolioHolding.class), allocations, warnings, ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    // VERIFY
    assertEquals(1, warnings.size());
    verify(service.countryAllocationMap).get("T");
  }

  @Test
  void shouldMapToRegions_whenVerifyGetRegionIsNull() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);

    service.countryAllocationMap = mock(HashMap.class);
    when(service.countryAllocationMap.get(any())).thenReturn(mock(CountryAllocation.class));

    doCallRealMethod().when(service).mapToRegions(any(), any(), any(), any());

    // ACT
    ArrayList<Notification> warnings = new ArrayList<>();
    service.mapToRegions(mock(PortfolioHolding.class), allocations, warnings, ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    // VERIFY
    assertEquals(1, warnings.size());
    verify(service.countryAllocationMap).get("T");
  }

  @Test
  void shouldMapToRegions_whenCheckResult() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);

    service.countryAllocationMap = mock(HashMap.class);
    CountryAllocation dto = mock(CountryAllocation.class);
    when(dto.getRegion()).thenReturn(CountryRegionType.CANADA);
    when(service.countryAllocationMap.get(any())).thenReturn(dto);

    doCallRealMethod().when(service).mapToRegions(any(), any(), any(), any());

    // ACT
    service.mapToRegions(mock(PortfolioHolding.class), allocations, new ArrayList<>(),
        ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    // VERIFY
    verify(service).sumAllocations(argThat(Map::isEmpty), eq(BigDecimal.ONE), eq(CountryRegionType.CANADA));
  }

  @Test
  void shouldMapToCountryRegions_whenVerifyMapToRegions() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    PortfolioHolding h = mock(PortfolioHolding.class);
    Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);

    doCallRealMethod().when(service).mapToCountryRegions(any(), any());

    // ACT
    service.mapToCountryRegions(Map.of(h, allocations), ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    // VERIFY
    verify(service).mapToRegions(eq(h), eq(allocations), anyList(), eq(ErrorCode.PORTFOLIO_MISSING_CURRENCY));
  }

  @Test
  void shouldMapToCountryRegions_whenCheckResult() {
    // SETUP
    var service = mock(CountryAllocationMappingService.class);

    PortfolioHolding h = mock(PortfolioHolding.class);
    Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);

    Map<CountryRegionType, BigDecimal> emergingMarket = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal.ONE);
    when(service.mapToRegions(eq(h), eq(allocations), anyList(), eq(ErrorCode.PORTFOLIO_MISSING_CURRENCY)))
        .thenReturn(emergingMarket);

    doCallRealMethod().when(service).mapToCountryRegions(any(), any());

    // ACT
    ExposureDataHolder<CountryRegionType> actual = service.mapToCountryRegions(Map.of(h, allocations),
        ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    // VERIFY
    assertEquals(Map.of(h, emergingMarket), actual.allocations());
    assertTrue(actual.warnings().isEmpty());
  }

}