package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CashHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RFixedIncomeBondSecurities;
import com.fintex.ce.repository.graphql.query.FixedIncomeBondSectorFDSRepository;
import com.fintex.ce.repository.redis.fixedincomebondsector.FixedIncomeBondSectorRedisRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.MapUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType.ASSET_BACKED_SECURITIES;
import static com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType.CORPORATE_BONDS;
import static com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType.GOVERNMENT_BONDS;
import static com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType.MORTGAGE_BACKED_SECURITIES;
import static com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType.OTHER_BONDS;
import static com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType.ST_INVESTMENTS;
import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeBondSectorCacheStorageTest {

    @Test
    void load_verifyFilters() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var queryRepository = mock(FixedIncomeBondSectorFDSRepository.class);
            final var fixedIncomeBondSectorRepository = mock(FixedIncomeBondSectorRedisRepository.class);
            final var cacheStatisticService = mock(CacheStatisticService.class);

            final var sut = mock(FixedIncomeBondSectorCacheStorage.class, withSettings()
                    .useConstructor(queryRepository, fixedIncomeBondSectorRepository, cacheStatisticService));

            final List<Holding> holdings = List.of(mock(Holding.class));

            doCallRealMethod().when(sut).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));
        }
    }

    @Test
    void load_verifyGetCashHoldingValues() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final FixedIncomeBondSectorCacheStorage sut = mock(FixedIncomeBondSectorCacheStorage.class);
            final List<Holding> holdings = List.of(mock(Holding.class));

            doCallRealMethod().when(sut).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).getCashHoldingValues(holdings);
        }
    }

    @Test
    void getCashHoldingValues_checkResults() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final FixedIncomeBondSectorCacheStorage sut = mock(FixedIncomeBondSectorCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<Holding> cacheHoldings = List.of(new CashHolding().setType(HoldingType.CASH));

            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CASH_PREDICATE))).thenReturn(cacheHoldings);

            doCallRealMethod().when(sut).getCashHoldingValues(any());
            //ACT
            final Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> cashHoldingValues = sut.getCashHoldingValues(holdings);

            //VERIFY
            assertEquals(Set.of(ST_INVESTMENTS), cashHoldingValues.values().stream().findFirst().get().keySet());
            assertEquals(ONE, cashHoldingValues.values().stream().findFirst().get().values().toArray()[0]);
        }
    }

    @Test
    void load_verifyLoadBenchOfFundCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final FixedIncomeBondSectorCacheStorage sut = mock(FixedIncomeBondSectorCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<FundSeriesHolding> filtered = List.of(mock(FundSeriesHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadBenchOfFundCanada(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadForBenchOfEtfUs() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final FixedIncomeBondSectorCacheStorage sut = mock(FixedIncomeBondSectorCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any());
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
            final FixedIncomeBondSectorCacheStorage sut = mock(FixedIncomeBondSectorCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any());
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
            final FixedIncomeBondSectorCacheStorage sut = mock(FixedIncomeBondSectorCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<BenchmarkIndexHolding> filtered = List.of(mock(BenchmarkIndexHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(BENCHMARKS_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadForBenchOfBenchmarks(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadBenchOfFixedIncomes() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final FixedIncomeBondSectorCacheStorage sut = mock(FixedIncomeBondSectorCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));
            final List<FixedIncomeHolding> filtered = List.of(mock(FixedIncomeHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE)))
                    .thenReturn(filtered);

            doCallRealMethod().when(sut).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            sut.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(sut).loadBenchOfFixedIncomes(filtered, List.of());
        }
    }

    @Test
    void mapResponse_verifyConvertValues() {
        //SETUP
        final FixedIncomeBondSectorCacheStorage fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);

        final Holding h = mock(Holding.class);
        final RFixedIncomeBondSecurities r = mock(RFixedIncomeBondSecurities.class);

        final Map<Holding, RFixedIncomeBondSecurities> holdings = Map.of(h, r);
        final Map.Entry<Holding, RFixedIncomeBondSecurities> entry =
                new AbstractMap.SimpleEntry<>(h, r);

        doCallRealMethod().when(fixedIncomeBondSectorCacheStorage).mapResponse(any(), any());
        //ACT
        final List<Warning> warnings = List.of(mock(Warning.class));
        fixedIncomeBondSectorCacheStorage.mapResponse(holdings, warnings);

        //VERIFY
        verify(fixedIncomeBondSectorCacheStorage).convertValues(entry, warnings);
    }

    @Test
    void convertValues_checkResult() {
        //SETUP
        final FixedIncomeBondSectorCacheStorage fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);

        final StockHolding h = mock(StockHolding.class);
        final RFixedIncomeBondSecurities r = mock(RFixedIncomeBondSecurities.class);

        final Map.Entry<Holding, RFixedIncomeBondSecurities> entry =
                new AbstractMap.SimpleEntry<>(h, r);

        doCallRealMethod().when(fixedIncomeBondSectorCacheStorage).convertValues(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        fixedIncomeBondSectorCacheStorage.convertValues(entry, warnings);

        //VERIFY
        assertEquals(1, warnings.size());
    }

    @Test
    void convertRatings_verifyMapToRequiredFormat() {
        //SETUP
        final FixedIncomeBondSectorCacheStorage fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);

        final RFixedIncomeBondSecurities r = new RFixedIncomeBondSecurities().setFixedIncomeBondSectors(Map.of("test", ONE));
        final Map.Entry<Holding, RFixedIncomeBondSecurities> entry =
                new AbstractMap.SimpleEntry<>(mock(StockHolding.class), r);
        final Map<String, BigDecimal> ratingsRaw = entry.getValue().getFixedIncomeBondSectors();

        doCallRealMethod().when(fixedIncomeBondSectorCacheStorage).convertValues(any(), any());
        //ACT
        final List<Warning> warnings = List.of(mock(Warning.class));
        fixedIncomeBondSectorCacheStorage.convertValues(entry, warnings);

        //VERIFY
        verify(fixedIncomeBondSectorCacheStorage).mapToRequiredFormat(entry.getKey(), ratingsRaw, warnings);
    }

    @Test
    void mapRatingsToRequiredFormat_verifyGetFixedIncomeSectorType() {
        //SETUP
        final FixedIncomeBondSectorCacheStorage fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);

        final Holding h = mock(Holding.class);
        final Map<String, BigDecimal> ratingsRaw = Map.of("GIANT", ONE);

        doCallRealMethod().when(fixedIncomeBondSectorCacheStorage).mapToRequiredFormat(any(), any(), any());
        //ACT
        final List<Warning> warnings = List.of(mock(Warning.class));
        fixedIncomeBondSectorCacheStorage.mapToRequiredFormat(h, ratingsRaw, warnings);

        //VERIFY
        verify(fixedIncomeBondSectorCacheStorage).getFixedIncomeSectorType(h, "GIANT", warnings);
    }

    @Test
    void mapRatingsToRequiredFormat_verifyOverrideDefaultValues() {
        try (var mockedMapUtils = Mockito.mockStatic(MapUtils.class)) {
            //SETUP
            final FixedIncomeBondSectorCacheStorage sut = mock(FixedIncomeBondSectorCacheStorage.class);

            final Map<FixedIncomeSectorType, BigDecimal> DEFAULT_MAP = Map.of(CORPORATE_BONDS, ZERO, GOVERNMENT_BONDS, ZERO, OTHER_BONDS, ZERO, MORTGAGE_BACKED_SECURITIES, ZERO, ST_INVESTMENTS, ZERO, ASSET_BACKED_SECURITIES, ZERO);
            final Holding h = mock(Holding.class);
            final Map<String, BigDecimal> ratingsRaw = Map.of("CORPORATE_BONDS", ONE);
            final Map<FixedIncomeSectorType, BigDecimal> ratings = Map.of(CORPORATE_BONDS, ONE);

            when(sut.getFixedIncomeSectorType(any(), any(), any())).thenReturn(CORPORATE_BONDS);
            doCallRealMethod().when(sut).mapToRequiredFormat(any(), any(), any());
            //ACT
            final List<Warning> warnings = new ArrayList<>();
            sut.mapToRequiredFormat(h, ratingsRaw, warnings);

            //VERIFY
            mockedMapUtils.verify(() -> MapUtils.overrideDefaultValues(DEFAULT_MAP, ratings));
        }
    }

    @Test
    void getFixedIncomeSectorType_checkResult() {
        //SETUP
        final FixedIncomeBondSectorCacheStorage fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);

        final Holding h = mock(Holding.class);
        final String s = "CORPORATE_BONDS";

        doCallRealMethod().when(fixedIncomeBondSectorCacheStorage).getFixedIncomeSectorType(any(), any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        FixedIncomeSectorType e = fixedIncomeBondSectorCacheStorage.getFixedIncomeSectorType(h, s, warnings);

        //VERIFY
        assertEquals(CORPORATE_BONDS, e);
    }

    @Test
    void getFixedIncomeSectorType_checkWarningSize() {
        //SETUP
        final FixedIncomeBondSectorCacheStorage fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);

        final Holding h = mock(Holding.class);
        final String s = "test";

        doCallRealMethod().when(fixedIncomeBondSectorCacheStorage).getFixedIncomeSectorType(any(), any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        fixedIncomeBondSectorCacheStorage.getFixedIncomeSectorType(h, s, warnings);

        //VERIFY
        assertEquals(1, warnings.size());
    }

}
