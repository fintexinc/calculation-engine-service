package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.Country;
import com.fintex.ce.config.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.REquityCountryAllocation;
import com.fintex.ce.service.GeographicAllocationMappingService;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_RRC_EGE_001;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityGeographicAllocationCacheStorageTest {

    @Test
    void load_verifyLoadBenchOfFundCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final GeographicAllocationMappingService geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
            final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

            final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
                    withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService,
                            null, businessCountryCacheStorage));

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<FundSeriesHolding> filtered = List.of(mock(FundSeriesHolding.class));

            when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(WRN_RRC_EGE_001)))
                    .thenReturn(Map.of());
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadBenchOfFundCanada(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadCanadaPooledFunds() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final GeographicAllocationMappingService geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
            final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

            final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
                    withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService,
                            null, businessCountryCacheStorage));
            when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(WRN_RRC_EGE_001)))
                    .thenReturn(Map.of());

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<CanadaPooledFundHolding> filtered = List.of(mock(CanadaPooledFundHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_POOLED_FUND_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadCanadaPooledFunds(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadUsMutualFunds() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final GeographicAllocationMappingService geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
            final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

            final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
                    withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService,
                            null, businessCountryCacheStorage));
            when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(WRN_RRC_EGE_001)))
                    .thenReturn(Map.of());

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<UsMutualFundHolding> filtered = List.of(mock(UsMutualFundHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_MUTUAL_FUND_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadUsMutualFunds(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadCanadaHedgeFunds() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final GeographicAllocationMappingService geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
            final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

            final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
                    withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService,
                            null, businessCountryCacheStorage));
            when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(WRN_RRC_EGE_001)))
                    .thenReturn(Map.of());

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<CanadaHedgeFundHolding> filtered = List.of(mock(CanadaHedgeFundHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_HEDGE_FUND_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadCanadaHedgeFunds(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadForBenchOfEtfUs() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final GeographicAllocationMappingService geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
            final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

            final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
                    withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService,
                            null, businessCountryCacheStorage));
            when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(WRN_RRC_EGE_001)))
                    .thenReturn(Map.of());

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadForBenchOfEtfUs(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadForBenchOfEtfCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final GeographicAllocationMappingService geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
            final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

            final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
                    withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService,
                            null, businessCountryCacheStorage));
            when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(WRN_RRC_EGE_001)))
                    .thenReturn(Map.of());

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadForBenchOfEtfCanada(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadForBenchOfBenchmarks() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final GeographicAllocationMappingService geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
            final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

            final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
                    withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService,
                            null, businessCountryCacheStorage));

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<BenchmarkIndexHolding> filtered = List.of(mock(BenchmarkIndexHolding.class));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadForBenchOfBenchmarks(filtered, List.of());
        }
    }

    @Test
    void load_mapForNoneStock() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final GeographicAllocationMappingService geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
            final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

            final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
                    withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService,
                            null, businessCountryCacheStorage));

            final List<Holding> holdings = List.of(mock(Holding.class));
            final Map allocations = mock(Map.class);
            final Map nonStockResult = mock(Map.class);
            final FundSeriesHolding fundSeries = mock(FundSeriesHolding.class);
            final REquityCountryAllocation rEquityCountryAllocation = mock(REquityCountryAllocation.class);
            final List<FundSeriesHolding> filtered = List.of(fundSeries);

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)))
                    .thenReturn(filtered);
            when(sut.loadBenchOfFundCanada(filtered, List.of()))
                    .thenReturn(Map.of(fundSeries, rEquityCountryAllocation));
            when(rEquityCountryAllocation.getAllocations()).thenReturn(allocations);
            when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(WRN_RRC_EGE_001)))
                    .thenReturn(Map.of(fundSeries, nonStockResult));

            doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            final Map<Holding, Map<GeographicRegionType, BigDecimal>> result = sut.load(
                    holdings,
                    List.of(),
                    warnings,
                    new ParamHolderDTO()
            );

            //VERIFY
            Assertions.assertEquals(1, result.size());
            final var entry = result.entrySet().stream().findFirst().orElseThrow();
            Assertions.assertEquals(fundSeries, entry.getKey());
            Assertions.assertEquals(nonStockResult, entry.getValue());
        }
    }

    @Test
    void load_mapForStock() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final GeographicAllocationMappingService geographicAllocationMappingService = mock(GeographicAllocationMappingService.class);
            final BusinessCountryCacheStorage businessCountryCacheStorage = mock(BusinessCountryCacheStorage.class);

            final EquityGeographicAllocationCacheStorage sut = mock(EquityGeographicAllocationCacheStorage.class,
                    withSettings().useConstructor(null, null, null, null, geographicAllocationMappingService,
                            null, businessCountryCacheStorage));

            final List<Holding> holdings = List.of(mock(Holding.class));
            final Map allocations = mock(Map.class);
            final Map nonStockResult = mock(Map.class);
            final StockHolding stockHolding = mock(StockHolding.class);
            final REquityCountryAllocation rEquityCountryAllocation = mock(REquityCountryAllocation.class);
            final List<StockHolding> filtered = List.of(stockHolding);

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)))
                    .thenReturn(filtered);
            when(sut.loadForBenchOfStock(filtered, List.of()))
                    .thenReturn(Map.of(stockHolding, rEquityCountryAllocation));
            when(rEquityCountryAllocation.getAllocations()).thenReturn(allocations);
            when(geographicAllocationMappingService.mapToGeographicRegions(Mockito.anyMap(), Mockito.anyList(), Mockito.eq(WRN_RRC_EGE_001)))
                    .thenReturn(Map.of(stockHolding, nonStockResult));
            when(businessCountryCacheStorage.loadBusinessCountries(any(), any(), anyBoolean(), anyList()))
                    .thenReturn(Map.of(stockHolding, Country.CAN));

            doCallRealMethod().when(sut).load(any(), any(), any(), any(ParamHolderDTO.class));
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            final Map<Holding, Map<GeographicRegionType, BigDecimal>> result = sut.load(
                    holdings,
                    List.of(),
                    warnings,
                    new ParamHolderDTO()
            );

            //VERIFY
            Assertions.assertEquals(1, result.size());
            final var entry = result.entrySet().stream().findFirst().orElseThrow();
            Assertions.assertEquals(stockHolding, entry.getKey());
            Assertions.assertEquals(1, entry.getValue().size());
            final Map.Entry<GeographicRegionType, BigDecimal> regionTypeResult = entry.getValue().entrySet()
                    .stream().findFirst().orElseThrow();
            Assertions.assertEquals(GeographicRegionType.CANADA, regionTypeResult.getKey());
            Assertions.assertEquals(BigDecimal.ONE, regionTypeResult.getValue());
        }
    }

}
