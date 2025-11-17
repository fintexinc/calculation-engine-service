package com.fintex.ce.service.impl;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import com.fintex.ce.dto.CountryAllocationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.SystemException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CountryAllocationMappingServiceImplTest {

    @Test
    void initCountryAllocationMapping_checkResult() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        doCallRealMethod().when(sut).getCountryAllocationInputStream();
        doCallRealMethod().when(sut).initCountryAllocationMapping();

        //ACT
        final Map<String, CountryAllocationDTO> actual = sut.initCountryAllocationMapping();

        //VERIFY
        assertFalse(actual.isEmpty());

        final CountryAllocationDTO can = actual.get("CAN");
        assertEquals(CountryRegionType.CANADA, can.getRegion());
        final CountryAllocationDTO usa = actual.get("USA");
        assertEquals(CountryRegionType.UNITED_STATES, usa.getRegion());
    }

    @Test
    void initCountryAllocationMapping_verifyGetCountryAllocationInputStream() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        when(sut.getCountryAllocationInputStream()).thenReturn(this.getClass().getResourceAsStream("/jsons/country-allocation-mapping.json"));
        doCallRealMethod().when(sut).initCountryAllocationMapping();

        //ACT
        sut.initCountryAllocationMapping();

        //VERIFY
        verify(sut).getCountryAllocationInputStream();
    }

    @Test
    void initCountryAllocationMapping_verifyException() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        when(sut.getCountryAllocationInputStream()).thenReturn(null);
        doCallRealMethod().when(sut).initCountryAllocationMapping();

        //VERIFY
        assertThrows(SystemException.class, sut::initCountryAllocationMapping);
    }

    @Test
    void sumAllocations_checkResult() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        final HashMap<CountryRegionType, BigDecimal> map = new HashMap<>();

        doCallRealMethod().when(sut).sumAllocations(any(), any(), any());

        //ACT
        sut.sumAllocations(map, BigDecimal.TEN, CountryRegionType.EMERGING_MARKET);

        //VERIFY
        assertEquals(1, map.size());
        assertTrue(map.containsKey(CountryRegionType.EMERGING_MARKET));
        assertTrue(map.containsValue(BigDecimal.TEN));
    }

    @Test
    void mapToRegions_verifyAllocationEmpty() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        final Map<String, BigDecimal> allocations = new HashMap<>();

        sut.countryAllocationMap = mock(HashMap.class);

        doCallRealMethod().when(sut).mapToRegions(any(), any(), any(), any());

        //ACT
        final ArrayList<Warning> warnings = new ArrayList<>();
        sut.mapToRegions(mock(Holding.class), allocations, warnings, ExceptionCode.ERR_RRC_MC_001);

        //VERIFY
        assertEquals(1, warnings.size());
        verify(sut.countryAllocationMap, times(0)).get(any());
    }

    @Test
    void mapToRegions_verifyGetAllocationNotFound() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        final Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);

        sut.countryAllocationMap = mock(HashMap.class);

        doCallRealMethod().when(sut).mapToRegions(any(), any(), any(), any());

        //ACT
        final ArrayList<Warning> warnings = new ArrayList<>();
        sut.mapToRegions(mock(Holding.class), allocations, warnings, ExceptionCode.ERR_RRC_MC_001);

        //VERIFY
        assertEquals(1, warnings.size());
        verify(sut.countryAllocationMap).get("T");
    }

    @Test
    void mapToRegions_verifyGetRegionIsNull() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        final Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);

        sut.countryAllocationMap = mock(HashMap.class);
        when(sut.countryAllocationMap.get(any())).thenReturn(mock(CountryAllocationDTO.class));

        doCallRealMethod().when(sut).mapToRegions(any(), any(), any(), any());

        //ACT
        final ArrayList<Warning> warnings = new ArrayList<>();
        sut.mapToRegions(mock(Holding.class), allocations, warnings, ExceptionCode.ERR_RRC_MC_001);

        //VERIFY
        assertEquals(1, warnings.size());
        verify(sut.countryAllocationMap).get("T");
    }

    @Test
    void mapToRegions_checkResult() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        final Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);

        sut.countryAllocationMap = mock(HashMap.class);
        final CountryAllocationDTO dto = mock(CountryAllocationDTO.class);
        when(dto.getRegion()).thenReturn(CountryRegionType.CANADA);
        when(sut.countryAllocationMap.get(any())).thenReturn(dto);

        doCallRealMethod().when(sut).mapToRegions(any(), any(), any(), any());

        //ACT
        sut.mapToRegions(mock(Holding.class), allocations, new ArrayList<>(), ExceptionCode.ERR_RRC_MC_001);

        //VERIFY
        verify(sut).sumAllocations(argThat(Map::isEmpty), eq(BigDecimal.ONE), eq(CountryRegionType.CANADA));
    }

    @Test
    void mapToCountryRegions_verifyMapToRegions() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        final Holding h = mock(Holding.class);
        final Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);

        doCallRealMethod().when(sut).mapToCountryRegions(any(), any(), any());

        //ACT
        final List<Warning> warnings = List.of(mock(Warning.class));
        sut.mapToCountryRegions(Map.of(h, allocations), warnings, ExceptionCode.ERR_RRC_MC_001);

        //VERIFY
        verify(sut).mapToRegions(h, allocations, warnings, ExceptionCode.ERR_RRC_MC_001);
    }

    @Test
    void mapToCountryRegions_checkResult() {
        //SETUP
        final var sut = mock(CountryAllocationMappingServiceImpl.class);

        final Holding h = mock(Holding.class);
        final Map<String, BigDecimal> allocations = Map.of("T", BigDecimal.ONE);
        final List<Warning> warnings = List.of(mock(Warning.class));

        final Map<CountryRegionType, BigDecimal> emergingMarket = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal.ONE);
        when(sut.mapToRegions(h, allocations, warnings, ExceptionCode.ERR_RRC_MC_001)).thenReturn(emergingMarket);

        doCallRealMethod().when(sut).mapToCountryRegions(any(), any(), any());

        //ACT
        final Map<Holding, Map<CountryRegionType, BigDecimal>> actual = sut.mapToCountryRegions(Map.of(h, allocations), warnings, ExceptionCode.ERR_RRC_MC_001);

        //VERIFY
        assertEquals(Map.of(h, emergingMarket), actual);
    }

}