package com.fintex.ce.application.mapping;

import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountryAllocationMappingServiceTest {

  private final CountryRegionResolver countryRegionResolver = mock(CountryRegionResolver.class);
  private final CountryAllocationMappingService service = new CountryAllocationMappingService(countryRegionResolver);

  @Test
  void shouldSumAllocations_whenCheckResult() {
    Map<CountryRegionType, BigDecimal> map = new EnumMap<>(CountryRegionType.class);

    service.sumAllocations(map, BigDecimal.TEN, CountryRegionType.EMERGING_MARKET);

    assertEquals(1, map.size());
    assertEquals(0, map.get(CountryRegionType.EMERGING_MARKET).compareTo(BigDecimal.TEN));
  }

  @Test
  void shouldMapToRegions_whenAllocationsEmpty_addWarningAndSkipResolver() {
    List<Notification> warnings = new ArrayList<>();

    Map<CountryRegionType, BigDecimal> result = service.mapToRegions(mock(PortfolioHolding.class), Map.of(), warnings,
        ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    assertTrue(result.isEmpty());
    assertEquals(1, warnings.size());
    verify(countryRegionResolver, never()).regionOf(any());
  }

  @Test
  void shouldMapToRegions_whenCountryUnmapped_addWarning() {
    when(countryRegionResolver.regionOf(Country.CANADA)).thenReturn(null);
    List<Notification> warnings = new ArrayList<>();

    Map<CountryRegionType, BigDecimal> result = service.mapToRegions(mock(PortfolioHolding.class),
        Map.of(Country.CANADA, BigDecimal.ONE), warnings, ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    assertTrue(result.isEmpty());
    assertEquals(1, warnings.size());
  }

  @Test
  void shouldMapToRegions_whenCountryMapped_sumIntoRegion() {
    when(countryRegionResolver.regionOf(Country.CANADA)).thenReturn(CountryRegionType.CANADA);
    List<Notification> warnings = new ArrayList<>();

    Map<CountryRegionType, BigDecimal> result = service.mapToRegions(mock(PortfolioHolding.class),
        Map.of(Country.CANADA, BigDecimal.ONE), warnings, ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    assertEquals(0, result.get(CountryRegionType.CANADA).compareTo(BigDecimal.ONE));
    assertTrue(warnings.isEmpty());
  }

  @Test
  void shouldMapToCountryRegions_aggregatePerHolding() {
    PortfolioHolding holding = mock(PortfolioHolding.class);
    when(countryRegionResolver.regionOf(Country.CANADA)).thenReturn(CountryRegionType.CANADA);

    ExposureDataHolder<CountryRegionType> actual = service.mapToCountryRegions(
        Map.of(holding, Map.of(Country.CANADA, BigDecimal.ONE)), ErrorCode.PORTFOLIO_MISSING_CURRENCY);

    assertTrue(actual.warnings().isEmpty());
    assertEquals(0, actual.allocations().get(holding).get(CountryRegionType.CANADA).compareTo(BigDecimal.ONE));
  }
}
