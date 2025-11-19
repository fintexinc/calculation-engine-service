package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.model.redis.equitysector.REquitySector;
import com.fintex.ce.model.redis.equitysector.REquitySectorStock;
import com.fintex.ce.repository.graphql.query.EquitySectorSMRepository;
import com.fintex.ce.repository.redis.equitysector.EquitySectorRepository;
import com.fintex.ce.repository.redis.equitysector.EquitySectorStockRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_ES_ESA_001;
import static com.fintex.ce.service.impl.cache.EquitySectorCacheStorage.DEFAULT_MAP;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquitySectorCacheStorageTest {

    @Test
    void load_verifyFilters() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var fdsRepo = mock(EquitySectorSMRepository.class);
            final var sectorRepo = mock(EquitySectorRepository.class);
            final var stockRepository = mock(EquitySectorStockRepository.class);
            final var cacheStatisticService = mock(CacheStatisticService.class);

            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class, withSettings()
                    .useConstructor(fdsRepo, sectorRepo, sectorRepo, sectorRepo, stockRepository, cacheStatisticService));

            final List<Holding> holdings = List.of(mock(Holding.class));

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
            verify(m).mapForStocks(holdings, warnings);
        }
    }

    @Test
    void load_verifyLoadBenchOfFundCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

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
    void load_verifyLoadForBenchOfBenchmarks() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));

            final List<BenchmarkIndexHolding> filtered = List.of(mock(BenchmarkIndexHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(m).loadForBenchOfBenchmarks(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadForBenchOfEtfUs() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

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
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

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
    void mapForStocks_verifyLoadForBenchOfStock() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<Warning> warnings = List.of(mock(Warning.class));

            final List<StockHolding> filtered = List.of(mock(StockHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(m).mapForStocks(any(), anyList());
            //ACT
            m.mapForStocks(holdings, warnings);

            //VERIFY
            verify(m).loadForBenchOfStock(filtered, List.of());
        }
    }

    @Test
    void load_verifyMapForStocks() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(m).mapForStocks(holdings, warnings);
        }
    }

    @Test
    void mapForStocks_verifyFilterHoldings() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);
            final List<Warning> warnings = List.of(mock(Warning.class));
            final List<Holding> holdings = List.of(mock(Holding.class));

            when(m.loadForBenchOfStock(anyList(), anyList())).thenReturn(Map.of());

            doCallRealMethod().when(m).mapForStocks(any(), any());
            //ACT
            m.mapForStocks(holdings, warnings);

            //VERIFY
            mockedFilterUtils.verify((() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE))));
        }
    }

    @Test
    void mapForStocks_checkResult() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);
            final List<Warning> warnings = List.of(mock(Warning.class));
            final StockHolding h = mock(StockHolding.class);

            final REquitySectorStock sectorStock = mock(REquitySectorStock.class);
            when(sectorStock.getSectorName()).thenReturn("ERROR");

            final Map<StockHolding, REquitySectorStock> stocks = Map.of(h, sectorStock);

            when(m.loadForBenchOfStock(anyList(), anyList())).thenReturn(stocks);

            doCallRealMethod().when(m).mapForStocks(any(), any());
            //ACT
            final List<Holding> holdings = List.of(h);
            assertThrows(DataErrorException.class, () -> m.mapForStocks(holdings, warnings));

            //VERIFY
        }
    }

    @Test
    void mapForStocks_checkResult2() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

            final StockHolding h = mock(StockHolding.class);
            final List<Warning> warnings = List.of(mock(Warning.class));

            final REquitySectorStock sectorStock = mock(REquitySectorStock.class);
            when(sectorStock.getSectorName()).thenReturn(EquitySectorAllocationType.BASIC_MATERIALS.name());

            final Map<StockHolding, REquitySectorStock> stocks = Map.of(h, sectorStock);

            when(m.loadForBenchOfStock(anyList(), anyList())).thenReturn(stocks);

            doCallRealMethod().when(m).mapForStocks(any(), any());
            //ACT
            final List<Holding> holdings = List.of(h);
            final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> actual = m.mapForStocks(holdings, warnings);

            //VERIFY
            final HashMap<EquitySectorAllocationType, BigDecimal> defaultMap = new HashMap<>(DEFAULT_MAP);
            defaultMap.put(EquitySectorAllocationType.BASIC_MATERIALS, BigDecimal.ONE);
            assertEquals(Map.of(h, defaultMap), actual);
        }
    }

    @Test
    void mapForStocks_checkResult3() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

            final StockHolding h = mock(StockHolding.class);
            final List<Warning> warnings = new ArrayList<>();

            final REquitySectorStock sectorStock = mock(REquitySectorStock.class);
            when(sectorStock.getSectorName()).thenReturn(null);

            final Map<StockHolding, REquitySectorStock> stocks = Map.of(h, sectorStock);

            when(m.loadForBenchOfStock(anyList(), anyList())).thenReturn(stocks);

            doCallRealMethod().when(m).mapForStocks(any(), any());
            //ACT
            final List<Holding> holdings = List.of(h);
            final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> actual = m.mapForStocks(holdings, warnings);

            //VERIFY
            assertEquals(0, actual.size());
            assertEquals(1, warnings.size());
            assertEquals("The holding is missing values for Sector Name", warnings.get(0).getMessage());
            assertEquals("WRN_ES_SN_001", warnings.get(0).getCode());
        }
    }

    @Test
    void DEFAULT_MAP_checkResult() {
        //SETUP

        //ACT

        //VERIFY
        assertEquals(EquitySectorAllocationType.values().length, DEFAULT_MAP.size());
        assertEquals(BigDecimal.ZERO, DEFAULT_MAP.get(EquitySectorAllocationType.BASIC_MATERIALS));
    }

    @Test
    void mapForNoneStock_verifyEquitySectorAllocationMapper() {
        //SETUP
        final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

        final Holding h = mock(Holding.class);
        final REquitySector rEquitySector = mock(REquitySector.class);
        final Map<Holding, REquitySector> sectorMap = Map.of(h, rEquitySector);

        doCallRealMethod().when(m).mapForNoneStock(any(), any());
        //ACT
        final List<Warning> warnings = List.of(mock(Warning.class));
        m.mapForNoneStock(sectorMap, warnings);

        //VERIFY
        verify(m).equitySectorAllocationMapper(
                argThat(arg -> arg.getKey() == h && arg.getValue() == rEquitySector),
                eq(warnings)
        );
    }

    @Test
    void mapForNoneStock_checkResult() {
        //SETUP
        final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

        final Holding h = mock(Holding.class);
        final REquitySector rEquitySector = mock(REquitySector.class);
        final Map<Holding, REquitySector> sectorMap = Map.of(h, rEquitySector);

        final Map<EquitySectorAllocationType, BigDecimal> actualValue = Map.of(EquitySectorAllocationType.BASIC_MATERIALS, BigDecimal.TEN);
        when(m.equitySectorAllocationMapper(any(), any())).thenReturn(actualValue);

        doCallRealMethod().when(m).mapForNoneStock(any(), any());
        //ACT
        final List<Warning> warnings = List.of(mock(Warning.class));
        final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> actual = m.mapForNoneStock(sectorMap, warnings);

        //VERIFY
        assertEquals(Map.of(h, actualValue), actual);
    }

    @Test
    void equitySectorAllocationMapper_checkResult() {
        //SETUP
        final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

        final Holding h = mock(Holding.class);

        final Map.Entry entry = mock(Map.Entry.class);
        when(entry.getKey()).thenReturn(h);

        final REquitySector sector = mock(REquitySector.class);
        when(entry.getValue()).thenReturn(sector);

        when(sector.getAllocations()).thenReturn(Map.of());

        doCallRealMethod().when(m).equitySectorAllocationMapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map actual = m.equitySectorAllocationMapper(entry, warnings);

        //VERIFY
        assertEquals(DEFAULT_MAP, actual);
        assertEquals(1, warnings.size());
        assertEquals(WRN_ES_ESA_001.getMessage(), warnings.get(0).getMessage());
        assertEquals(WRN_ES_ESA_001.name(), warnings.get(0).getCode());
    }

    @Test
    void equitySectorAllocationMapper_checkResult2() {
        //SETUP
        final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

        final Holding h = mock(Holding.class);

        final Map.Entry entry = mock(Map.Entry.class);
        when(entry.getKey()).thenReturn(h);

        final REquitySector sector = mock(REquitySector.class);
        when(entry.getValue()).thenReturn(sector);

        when(sector.getAllocations()).thenReturn(Map.of("TEST", BigDecimal.ONE));

        doCallRealMethod().when(m).equitySectorAllocationMapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map actual = m.equitySectorAllocationMapper(entry, warnings);

        //VERIFY
        assertEquals(DEFAULT_MAP, actual);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains("TEST"));
    }

    @Test
    void equitySectorAllocationMapper_checkResult3() {
        //SETUP
        final EquitySectorCacheStorage m = mock(EquitySectorCacheStorage.class);

        final Holding h = mock(Holding.class);

        final Map.Entry entry = mock(Map.Entry.class);
        when(entry.getKey()).thenReturn(h);

        final REquitySector sector = mock(REquitySector.class);
        when(entry.getValue()).thenReturn(sector);

        when(sector.getAllocations()).thenReturn(Map.of(EquitySectorAllocationType.BASIC_MATERIALS.name(), BigDecimal.ONE));

        doCallRealMethod().when(m).equitySectorAllocationMapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map actual = m.equitySectorAllocationMapper(entry, warnings);

        //VERIFY
        final HashMap<EquitySectorAllocationType, BigDecimal> expected = new HashMap<>(DEFAULT_MAP);
        expected.put(EquitySectorAllocationType.BASIC_MATERIALS, BigDecimal.ONE);

        assertEquals(expected, actual);
        assertEquals(0, warnings.size());
    }


}
