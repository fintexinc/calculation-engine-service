package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.calculation.CreditQualityRating;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RCreditQuality;
import com.fintex.ce.repository.graphql.query.CreditQualityFDSRepository;
import com.fintex.ce.repository.redis.CreditQualityRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.util.FilterUtils.BENCHMARKS_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.TestConstants.GREATER_THAN_YEAR;
import static com.fintex.ce.util.TestConstants.LESS_THAN_YEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CreditQualityCacheStorageTest {

    @Test
    void load_verifyFilters() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var fdsRepo = mock(CreditQualityFDSRepository.class);
            final var creditQualityRepository = mock(CreditQualityRepository.class);
            final var cacheStatisticService = mock(CacheStatisticService.class);

            final CreditQualityCacheStorage m = mock(CreditQualityCacheStorage.class, withSettings()
                    .useConstructor(fdsRepo, creditQualityRepository, creditQualityRepository, creditQualityRepository, cacheStatisticService));

            final List<Holding> holdings = List.of(mock(Holding.class));

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE)));
            mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE)));

        }
    }

    @Test
    void load_verifyLoadBenchOfFundCanada() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final CreditQualityCacheStorage m = mock(CreditQualityCacheStorage.class);

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
    void load_verifyLoadBenchOfFixedIncomes() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final CreditQualityCacheStorage m = mock(CreditQualityCacheStorage.class);

            final List<Holding> holdings = List.of(mock(Holding.class));

            final List<FixedIncomeHolding> filtered = List.of(mock(FixedIncomeHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(m).loadBenchOfFixedIncomes(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadForBenchOfEtfUs() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final CreditQualityCacheStorage m = mock(CreditQualityCacheStorage.class);

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
            final CreditQualityCacheStorage m = mock(CreditQualityCacheStorage.class);

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
    void load_verifyLoadForBenchOfBenchmarks() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final CreditQualityCacheStorage m = mock(CreditQualityCacheStorage.class);

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
    void load_verifyAddGics() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final CreditQualityCacheStorage m = mock(CreditQualityCacheStorage.class);

            final List<Holding> holdings = List.of(mock(GicHolding.class));

            final List<Holding> filtered = List.of(mock(GicHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(GIC_PREDICATE))).thenReturn(filtered);

            doCallRealMethod().when(m).load(any(), any(), any(), any());
            //ACT
            final List<Warning> warnings = List.of(mock(Warning.class));
            m.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(m).addGics(filtered);
        }
    }

    @Test
    void addGics_ifGicIsLessThanAYearThanMapContainsAAA() {
        //SETUP
        final CreditQualityCacheStorage sut = mock(CreditQualityCacheStorage.class);

        final GicHolding gic = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
        gic.setTerm(GREATER_THAN_YEAR);
        final List<Holding> holdings = List.of(gic);

        final HashMap<Holding, Map<CreditQualityRating, BigDecimal>> expected = new HashMap<>();
        expected.put(gic, Map.of(CreditQualityRating.AAA, BigDecimal.ONE));

        doCallRealMethod().when(sut).addGics(any());
        //ACT
        final Map<Holding, Map<CreditQualityRating, BigDecimal>> actual = sut.addGics(holdings);

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareMaps(expected, actual);
    }

    @Test
    void addGics_ifGicIsLessThanAYearThanMapContainsNothing() {
        //SETUP
        final CreditQualityCacheStorage sut = mock(CreditQualityCacheStorage.class);

        final GicHolding gic = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
        gic.setTerm(LESS_THAN_YEAR);
        final List<Holding> holdings = List.of(gic);

        final HashMap<Holding, Map<CreditQualityRating, BigDecimal>> expected = new HashMap<>();

        doCallRealMethod().when(sut).addGics(any());
        //ACT
        final Map<Holding, Map<CreditQualityRating, BigDecimal>> actual = sut.addGics(holdings);

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareMaps(expected, actual);
    }

    @Test
    void mapRatings_checkResult() {
        //SETUP
        final CreditQualityCacheStorage c = mock(CreditQualityCacheStorage.class);

        final Holding h = mock(Holding.class);
        when(h.generateUserIdentifier()).thenReturn("ID");

        final RCreditQuality rc = mock(RCreditQuality.class);
        when(rc.getRatings()).thenReturn(null);

        doCallRealMethod().when(c).mapRatings(any(), any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<CreditQualityRating, BigDecimal> actual = c.mapRatings(h, rc, warnings);

        //VERIFY
        assertEquals(1, warnings.size());
        assertTrue(actual.isEmpty());
    }

    @Test
    void mapRatings_checkResult2() {
        //SETUP
        final CreditQualityCacheStorage c = mock(CreditQualityCacheStorage.class);

        final Holding h = mock(Holding.class);
        when(h.generateUserIdentifier()).thenReturn("ID");

        final RCreditQuality rc = mock(RCreditQuality.class);
        final Map<String, BigDecimal> map = Map.of("SDF", BigDecimal.ONE);
        when(rc.getRatings()).thenReturn(map);

        doCallRealMethod().when(c).mapRatings(any(), any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<CreditQualityRating, BigDecimal> actual = c.mapRatings(h, rc, warnings);

        //VERIFY
        assertEquals(1, warnings.size());
        assertTrue(actual.isEmpty());
    }

    @Test
    void mapRatings_checkResult3() {
        //SETUP
        final CreditQualityCacheStorage c = mock(CreditQualityCacheStorage.class);

        final Holding h = mock(Holding.class);
        when(h.generateUserIdentifier()).thenReturn("ID");

        final RCreditQuality rc = mock(RCreditQuality.class);
        final Map<String, BigDecimal> map = Map.of(CreditQualityRating.A.name(), BigDecimal.ONE);
        when(rc.getRatings()).thenReturn(map);

        doCallRealMethod().when(c).mapRatings(any(), any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<CreditQualityRating, BigDecimal> actual = c.mapRatings(h, rc, warnings);

        //VERIFY
        assertEquals(0, warnings.size());
        assertEquals(Map.of(CreditQualityRating.A, BigDecimal.ONE), actual);
    }

    @Test
    void mapper_checkResult() {
        //SETUP
        final CreditQualityCacheStorage c = mock(CreditQualityCacheStorage.class);

        final Holding h = mock(Holding.class);
        when(h.generateUserIdentifier()).thenReturn("ID");

        final RCreditQuality rc = mock(RCreditQuality.class);
        final Map<String, BigDecimal> map = Map.of(CreditQualityRating.A.name(), BigDecimal.ONE);
        when(rc.getRatings()).thenReturn(map);

        doCallRealMethod().when(c).mapRatings(any(), any(), any());
        doCallRealMethod().when(c).mapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<Holding, Map<CreditQualityRating, BigDecimal>> actual = c.mapper(Map.of(h, rc), warnings);

        //VERIFY
        assertEquals(0, warnings.size());
        assertEquals(Map.of(h, Map.of(CreditQualityRating.A, BigDecimal.ONE)), actual);
    }

}
