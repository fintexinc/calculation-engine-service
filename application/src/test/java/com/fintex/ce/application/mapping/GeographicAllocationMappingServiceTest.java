package com.fintex.ce.application.mapping;

import com.fintex.ce.model.domain.calculation.allocation.GeographicRegionType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeographicAllocationMappingServiceTest {

  @Mock
  private PortfolioHolding holding;

  private GeographicAllocationMappingService geographicAllocationMappingService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    geographicAllocationMappingService = new GeographicAllocationMappingService();
  }

  @Test
  void shouldMapToGeographicRegions_whenReturnsExpectedResultsWhenValidDataProvided() {
    Map<String, BigDecimal> allocations = new HashMap<>();
    allocations.put("countryId", BigDecimal.ONE);
    Map<PortfolioHolding, Map<String, BigDecimal>> holdingAllocations = new HashMap<>();
    holdingAllocations.put(holding, allocations);

    Map<PortfolioHolding, Map<GeographicRegionType, BigDecimal>> result = geographicAllocationMappingService
        .mapToGeographicRegions(holdingAllocations, new ArrayList<>(), ErrorCode.UNKNOWN_TYPE_FROM_DATA_POINT);

    assertTrue(result.containsKey(holding));
    assertFalse(result.get(holding).containsKey(GeographicRegionType.ASIA));
  }

  @Test
  void shouldMapToGeographicRegions_whenReturnsWarningResultsWhenNoDataProvided() {
    Map<PortfolioHolding, Map<String, BigDecimal>> holdingAllocations = new HashMap<>();
    holdingAllocations.put(holding, new HashMap<>());

    List<Notification> warnings = new ArrayList<>();
    geographicAllocationMappingService.mapToGeographicRegions(holdingAllocations, warnings,
        ErrorCode.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE);

    assertEquals(1, warnings.size());
    assertEquals(ErrorCode.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE.getCode(), warnings.get(0).getCode());
  }

  @Test
  void shouldMapToGeographicRegions_whenReturnsWarningResultsWhenUnknownCountryId() {
    Map<String, BigDecimal> allocations = new HashMap<>();
    allocations.put("unknownCountryId", BigDecimal.ONE);
    Map<PortfolioHolding, Map<String, BigDecimal>> holdingAllocations = new HashMap<>();
    holdingAllocations.put(holding, allocations);

    List<Notification> warnings = new ArrayList<>();
    geographicAllocationMappingService.mapToGeographicRegions(holdingAllocations, warnings,
        ErrorCode.UNKNOWN_TYPE_FROM_DATA_POINT);

    assertEquals(1, warnings.size());
    assertEquals(ErrorCode.UNKNOWN_TYPE_FROM_DATA_POINT.getCode(), warnings.get(0).getCode());
  }
}