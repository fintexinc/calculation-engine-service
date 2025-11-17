package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.repository.graphql.query.YieldFDSRepository;
import com.fintex.ce.repository.redis.YieldRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class YieldCacheStorageTest {

    @Test
    void load_verifyFilters() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            final var fdsRepo = mock(YieldFDSRepository.class);
            final var fundCanadaCacheRepo = mock(YieldRepository.class);
            final var etfCanadaCacheRepo = mock(YieldRepository.class);
            final var etfUsCacheRepo = mock(YieldRepository.class);
            final var stockCacheRepo = mock(YieldRepository.class);
            final var cacheStatisticService = mock(CacheStatisticService.class);

            final YieldCacheStorage m = mock(YieldCacheStorage.class, withSettings()
                    .useConstructor(fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo, stockCacheRepo, cacheStatisticService));

            final List<Holding> holdings = List.of(mock(Holding.class));

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            Map<Holding, RYield> holdingExposureMap = holdings.stream()
                    .collect(Collectors.toMap(holding -> holding, holding -> mock(RYield.class)));

            List<Holding> holdingsFromMap = new ArrayList<>(holdingExposureMap.keySet());

            m.load(holdingsFromMap, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_POOLED_FUND_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_MUTUAL_FUND_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_HEDGE_FUND_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(SEPARATELY_MANAGED_ACCOUNT_PREDICATE)));
            verify(m, times((9))).verify(anyMap(), anyList());
        }
    }

    @Test
    void load_verifyLoadBenchOfFundCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final YieldCacheStorage m = mock(YieldCacheStorage.class);
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
            final YieldCacheStorage m = mock(YieldCacheStorage.class);
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
            final YieldCacheStorage m = mock(YieldCacheStorage.class);
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
    void load_verifyLoadGics() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final YieldCacheStorage m = mock(YieldCacheStorage.class);
            final List<Holding> holdings = List.of(mock(Holding.class));
            final GicHolding gicHolding = mock(GicHolding.class);
            final List<GicHolding> filtered = List.of(gicHolding);

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(GIC_PREDICATE))).thenReturn(filtered);

            when(gicHolding.getType()).thenReturn(HoldingType.GIC);
            when(gicHolding.getName()).thenReturn("name");
            when(gicHolding.getClientIntRate()).thenReturn(BigDecimal.ONE);
            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            final Map<Holding, RYield> result = m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            Assertions.assertNotNull(result);
            Assertions.assertEquals(1, result.size());
            Assertions.assertTrue(result.containsKey(gicHolding));
        }
    }

    @Test
    void load_verifyLoadBenchOfFundCanada_yieldMapper() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final YieldCacheStorage m = mock(YieldCacheStorage.class);
            final RYield rYield = mock(RYield.class);
            final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
            final List<Holding> holdings = List.of();
            final List<FundSeriesHolding> filtered = List.of(fundSeriesHolding);

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(filtered);

            when(rYield.getDividendYield()).thenReturn(null);
            when(m.loadBenchOfFundCanada(anyList(), anyList())).thenReturn(Map.of(fundSeriesHolding, rYield));
            doCallRealMethod().when(m).load(any(), any(), any(), any());
            doCallRealMethod().when(m).verify(any(), any());
            //ACT
            final List<Warning> warnings = new ArrayList<>();

            final Map<Holding, RYield> result = m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            Assertions.assertNotNull(result);
            Assertions.assertEquals(1, warnings.size());
        }
    }

    @Test
    void load_verifyLoadGics2() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final YieldCacheStorage m = mock(YieldCacheStorage.class);
            final List<Holding> holdings = List.of(mock(Holding.class));
            final GicHolding gicHolding = mock(GicHolding.class);
            final List<GicHolding> filtered = List.of(gicHolding);

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(GIC_PREDICATE))).thenReturn(filtered);

            when(gicHolding.getType()).thenReturn(HoldingType.GIC);
            when(gicHolding.getName()).thenReturn("name");
            when(gicHolding.getClientIntRate()).thenReturn(BigDecimal.ONE);
            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));

            final Map<Holding, RYield> result = m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            Assertions.assertNotNull(result);
            Assertions.assertEquals(1, result.size());
            Assertions.assertTrue(result.containsKey(gicHolding));
        }
    }

    @Test
    void load_verifyLoadBenchOfFundCanada_yieldMapper2() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final YieldCacheStorage m = mock(YieldCacheStorage.class);
            final RYield rYield = mock(RYield.class);
            final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
            final List<Holding> holdings = List.of();
            final List<FundSeriesHolding> filtered = List.of(fundSeriesHolding);

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(filtered);

            when(rYield.getDividendYield()).thenReturn(BigDecimal.ONE);
            when(m.loadBenchOfFundCanada(anyList(), anyList())).thenReturn(Map.of(fundSeriesHolding, rYield));
            doCallRealMethod().when(m).load(any(), any(), any(), any());
            doCallRealMethod().when(m).verify(any(), any());
            //ACT
            final List<Warning> warnings = new ArrayList<>();

            final Map<Holding, RYield> result = m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            Assertions.assertNotNull(result);
            Assertions.assertEquals(0, warnings.size());
        }
    }

}
