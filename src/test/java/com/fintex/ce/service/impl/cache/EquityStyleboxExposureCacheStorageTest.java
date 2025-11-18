package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.calculation.EquityStyleboxType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.REquityStyleboxExposure;
import com.fintex.ce.repository.graphql.query.EquityStyleboxAllocationSMRepository;
import com.fintex.ce.repository.redis.EquityStyleboxAllocationRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_ES_ESE_001;
import static com.fintex.ce.service.impl.cache.EquityStyleboxExposureCacheStorage.DEFAULT_MAP;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityStyleboxExposureCacheStorageTest {

    @Test
    void load_verifyFilters() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var fdsRepo = mock(EquityStyleboxAllocationSMRepository.class);
            final var fundCanadaCacheRepo = mock(EquityStyleboxAllocationRepository.class);
            final var etfCanadaCacheRepo = mock(EquityStyleboxAllocationRepository.class);
            final var etfUsCacheRepo = mock(EquityStyleboxAllocationRepository.class);
            final var cacheStatisticService = mock(CacheStatisticService.class);

            final EquityStyleboxExposureCacheStorage m = mock(EquityStyleboxExposureCacheStorage.class, withSettings()
                    .useConstructor(fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo, cacheStatisticService));

            final List<Holding> holdings = List.of(mock(Holding.class));

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            Map<Holding, REquityStyleboxExposure> holdingExposureMap = holdings.stream()
                    .collect(Collectors.toMap(holding -> holding, holding -> mock(REquityStyleboxExposure.class)));

            List<Holding> holdingsFromMap = new ArrayList<>(holdingExposureMap.keySet());

            m.load(holdingsFromMap, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_POOLED_FUND_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_MUTUAL_FUND_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_HEDGE_FUND_PREDICATE)));
            verify(m, times((7))).mapForNoneStock(anyMap(), anyList());
        }
    }

    @Test
    void load_verifyLoadBenchOfFundCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquityStyleboxExposureCacheStorage m = mock(EquityStyleboxExposureCacheStorage.class);
            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<FundSeriesHolding> filtered = List.of(mock(FundSeriesHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(m).loadBenchOfFundCanada(filtered, List.of());
        }
    }


    @Test
    void load_verifyLoadForBenchOfEtfUs() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquityStyleboxExposureCacheStorage m = mock(EquityStyleboxExposureCacheStorage.class);
            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(m).loadForBenchOfEtfUs(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadForBenchOfEtfCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquityStyleboxExposureCacheStorage m = mock(EquityStyleboxExposureCacheStorage.class);
            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(m).loadForBenchOfEtfCanada(filtered, List.of());
        }
    }


    @Test
    void mapForNoneStock_verifyEquityStyleboxExposureMapper() {
        //SETUP
        final EquityStyleboxExposureCacheStorage m = mock(EquityStyleboxExposureCacheStorage.class);
        final Holding h = mock(Holding.class);
        final REquityStyleboxExposure rEquityStyleboxExposure = mock(REquityStyleboxExposure.class);
        final Map<Holding, REquityStyleboxExposure> holdingEquityStyleboxExposureMap = Map.of(h, rEquityStyleboxExposure);

        doCallRealMethod().when(m).mapForNoneStock(any(), any());
        //ACT
        final List<Warning> warnings = List.of(mock(Warning.class));

        m.mapForNoneStock(holdingEquityStyleboxExposureMap, warnings);

        //VERIFY
        verify(m).equityStyleboxExposureMapper(
                argThat(arg -> arg.getKey() == h && arg.getValue() == rEquityStyleboxExposure),
                eq(warnings)
        );
    }

    @Test
    void mapForNoneStock_checkResult() {
        //SETUP
        final EquityStyleboxExposureCacheStorage m = mock(EquityStyleboxExposureCacheStorage.class);
        final Holding h = mock(Holding.class);
        final REquityStyleboxExposure rEquityStyleboxExposure = mock(REquityStyleboxExposure.class);
        final Map<Holding, REquityStyleboxExposure> holdingEquityStyleboxExposureMap = Map.of(h, rEquityStyleboxExposure);
        final Map<EquityStyleboxType, BigDecimal> actualValue = Map.of(EquityStyleboxType.LARGE_VALUE, BigDecimal.TEN);

        when(m.equityStyleboxExposureMapper(any(), any())).thenReturn(actualValue);

        doCallRealMethod().when(m).mapForNoneStock(any(), any());
        //ACT
        final List<Warning> warnings = List.of(mock(Warning.class));
        final Map<Holding, Map<EquityStyleboxType, BigDecimal>> actual = m.mapForNoneStock(holdingEquityStyleboxExposureMap, warnings);

        //VERIFY
        assertEquals(Map.of(h, actualValue), actual);
    }

    @Test
    void equityStyleboxExposureMapper_checkResult() {
        //SETUP
        final EquityStyleboxExposureCacheStorage m = mock(EquityStyleboxExposureCacheStorage.class);
        final Holding h = mock(Holding.class);
        final Map.Entry<Holding, REquityStyleboxExposure> entry = mock(Map.Entry.class);

        when(entry.getKey()).thenReturn(h);

        final REquityStyleboxExposure rEquityStyleboxExposure = mock(REquityStyleboxExposure.class);
        final Map<String, BigDecimal> emptyMap = Map.of();

        when(rEquityStyleboxExposure.getBoxValues()).thenReturn(emptyMap);

        when(entry.getValue()).thenReturn(rEquityStyleboxExposure);

        doCallRealMethod().when(m).equityStyleboxExposureMapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<EquityStyleboxType, BigDecimal> actual = m.equityStyleboxExposureMapper(entry, warnings);

        //VERIFY
        assertEquals(DEFAULT_MAP, actual);
        assertEquals(1, warnings.size());
        assertEquals(WRN_ES_ESE_001.getMessage(), warnings.get(0).getMessage());
        assertEquals(WRN_ES_ESE_001.name(), warnings.get(0).getCode());
    }

    @Test
    void equityStyleboxExposureMapper_checkResult2() {
        //SETUP
        final EquityStyleboxExposureCacheStorage m = mock(EquityStyleboxExposureCacheStorage.class);
        final Holding h = mock(Holding.class);
        final Map.Entry<Holding, REquityStyleboxExposure> entry = mock(Map.Entry.class);

        when(entry.getKey()).thenReturn(h);

        final REquityStyleboxExposure rEquityStyleboxExposure = mock(REquityStyleboxExposure.class);
        final Map<String, BigDecimal> emptyMap = Map.of();

        when(rEquityStyleboxExposure.getBoxValues()).thenReturn(emptyMap);
        when(entry.getValue()).thenReturn(rEquityStyleboxExposure);
        when(rEquityStyleboxExposure.getBoxValues()).thenReturn(Map.of("TEST", BigDecimal.ONE));

        doCallRealMethod().when(m).equityStyleboxExposureMapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<EquityStyleboxType, BigDecimal> actual = m.equityStyleboxExposureMapper(entry, warnings);

        //VERIFY
        assertEquals(DEFAULT_MAP, actual);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains("TEST"));
    }

    @Test
    void equityStyleboxExposureMapper_checkResult3() {
        //SETUP
        final EquityStyleboxExposureCacheStorage m = mock(EquityStyleboxExposureCacheStorage.class);
        final Holding h = mock(Holding.class);
        final Map.Entry<Holding, REquityStyleboxExposure> entry = mock(Map.Entry.class);

        when(entry.getKey()).thenReturn(h);

        final REquityStyleboxExposure rEquityStyleboxExposure = mock(REquityStyleboxExposure.class);
        final Map<String, BigDecimal> emptyMap = Map.of();

        when(rEquityStyleboxExposure.getBoxValues()).thenReturn(emptyMap);
        when(entry.getValue()).thenReturn(rEquityStyleboxExposure);
        when(rEquityStyleboxExposure.getBoxValues()).thenReturn(Map.of(EquityStyleboxType.LARGE_VALUE.name(), BigDecimal.ONE));

        doCallRealMethod().when(m).equityStyleboxExposureMapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<EquityStyleboxType, BigDecimal> actual = m.equityStyleboxExposureMapper(entry, warnings);

        //VERIFY
        final HashMap<EquityStyleboxType, BigDecimal> expected = new HashMap<>(DEFAULT_MAP);
        expected.put(EquityStyleboxType.LARGE_VALUE, BigDecimal.ONE);

        assertEquals(expected, actual);
        assertEquals(0, warnings.size());
    }


}