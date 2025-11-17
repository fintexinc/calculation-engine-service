package com.fintex.ce.service.impl;


import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
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

class GeographicAllocationMappingServiceImplTest {

    @Mock
    private Holding holding;

    private GeographicAllocationMappingServiceImpl geographicAllocationMappingServiceImpl;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        geographicAllocationMappingServiceImpl = new GeographicAllocationMappingServiceImpl();
    }

    @Test
    void mapToGeographicRegions_returnsExpectedResults_whenValidDataProvided() {
        Map<String, BigDecimal> allocations = new HashMap<>();
        allocations.put("countryId", BigDecimal.ONE);
        Map<Holding, Map<String, BigDecimal>> holdingAllocations = new HashMap<>();
        holdingAllocations.put(holding, allocations);

        Map<Holding, Map<GeographicRegionType, BigDecimal>> result = geographicAllocationMappingServiceImpl.mapToGeographicRegions(holdingAllocations, new ArrayList<>(), ExceptionCode.WRN_UNKNOWN_001);

        assertTrue(result.containsKey(holding));
        assertFalse(result.get(holding).containsKey(GeographicRegionType.ASIA));
    }

    @Test
    void mapToGeographicRegions_returnsWarningResults_whenNoDataProvided() {
        Map<Holding, Map<String, BigDecimal>> holdingAllocations = new HashMap<>();
        holdingAllocations.put(holding, new HashMap<>());

        List<Warning> warnings = new ArrayList<>();
        geographicAllocationMappingServiceImpl.mapToGeographicRegions(holdingAllocations, warnings, ExceptionCode.WRN_UNKNOWN_001);

        assertEquals(1, warnings.size());
        assertEquals(ExceptionCode.WRN_UNKNOWN_001.toString(), warnings.get(0).getCode());
    }

    @Test
    void mapToGeographicRegions_returnsWarningResults_whenUnknownCountryId() {
        Map<String, BigDecimal> allocations = new HashMap<>();
        allocations.put("unknownCountryId", BigDecimal.ONE);
        Map<Holding, Map<String, BigDecimal>> holdingAllocations = new HashMap<>();
        holdingAllocations.put(holding, allocations);

        List<Warning> warnings = new ArrayList<>();
        geographicAllocationMappingServiceImpl.mapToGeographicRegions(holdingAllocations, warnings, ExceptionCode.WRN_UNKNOWN_001);

        assertEquals(1, warnings.size());
        assertEquals(ExceptionCode.WRN_UNKNOWN_001.toString(), warnings.get(0).getCode());
    }
}