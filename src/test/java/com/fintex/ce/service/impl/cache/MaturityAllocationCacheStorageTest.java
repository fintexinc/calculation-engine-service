package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.calculation.MaturityAllocationType;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RMaturityAllocation;
import com.fintex.ce.repository.graphql.query.MaturityAllocationSMRepository;
import com.fintex.ce.repository.redis.MaturityAllocationRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.FilterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_MA_MA_001;
import static com.fintex.ce.service.impl.cache.MaturityAllocationCacheStorage.DEFAULT_MAP;
import static com.fintex.ce.util.FilterUtils.CANADA_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_HEDGE_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.CANADA_POOLED_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_ETF_PREDICATE;
import static com.fintex.ce.util.FilterUtils.US_MUTUAL_FUND_PREDICATE;
import static com.fintex.ce.util.FilterUtils.FIXED_INCOME_PREDICATE;
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

class MaturityAllocationCacheStorageTest {

    MaturityAllocationCacheStorage maturityAllocationCacheStorage;
    Holding holding;
    List<Holding> holdings;
    List<Warning> warnings;
    RMaturityAllocation rMaturityAllocation;
    Map.Entry<Holding, RMaturityAllocation> entry;

    @BeforeEach
    void setUp() {
        maturityAllocationCacheStorage = mock(MaturityAllocationCacheStorage.class);
        holding = mock(Holding.class);
        holdings = List.of(holding);
        warnings = List.of(mock(Warning.class));
        rMaturityAllocation = mock(RMaturityAllocation.class);
        entry = mock(Map.Entry.class);
    }

    @Test
    void load_verifyFilters() {
        //SETUP
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            final var fdsRepo = mock(MaturityAllocationSMRepository.class);
            final var fundCanadaCacheRepo = mock(MaturityAllocationRepository.class);
            final var etfCanadaCacheRepo = mock(MaturityAllocationRepository.class);
            final var etfUsCacheRepo = mock(MaturityAllocationRepository.class);
            final var cacheStatisticService = mock(CacheStatisticService.class);
            final MaturityAllocationCacheStorage m = mock(MaturityAllocationCacheStorage.class, withSettings()
                    .useConstructor(fdsRepo, fundCanadaCacheRepo, etfCanadaCacheRepo, etfUsCacheRepo, cacheStatisticService));
            doCallRealMethod().when(m).load(any(), any(), any(), any());

            //ACT
            Map<Holding, RMaturityAllocation> holdingExposureMap = holdings.stream()
                    .collect(Collectors.toMap(holding -> holding, holding -> rMaturityAllocation));
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
            verify(m, times((7))).mapForNoneStock(anyMap(), anyList());
        }
    }

    @Test
    void load_verifyLoadBenchOfFundCanada() {
        //SETUP
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            final List<FundSeriesHolding> filtered = List.of(mock(FundSeriesHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_MUTUAL_PREDICATE))).thenReturn(filtered);
            doCallRealMethod().when(maturityAllocationCacheStorage).load(any(), any(), any(), any());

            //ACT
            maturityAllocationCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(maturityAllocationCacheStorage).loadBenchOfFundCanada(filtered, List.of());
        }
    }


    @Test
    void load_verifyLoadForBenchOfEtfUs() {
        //SETUP
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(US_ETF_PREDICATE))).thenReturn(filtered);
            doCallRealMethod().when(maturityAllocationCacheStorage).load(any(), any(), any(), any());

            //ACT
            maturityAllocationCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(maturityAllocationCacheStorage).loadForBenchOfEtfUs(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadForBenchOfEtfCanada() {
        //SETUP
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            final List<EtfHolding> filtered = List.of(mock(EtfHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(CANADA_ETF_PREDICATE))).thenReturn(filtered);
            doCallRealMethod().when(maturityAllocationCacheStorage).load(any(), any(), any(), any());

            //ACT
            maturityAllocationCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(maturityAllocationCacheStorage).loadForBenchOfEtfCanada(filtered, List.of());
        }
    }

    @Test
    void load_verifyLoadBenchOfFixedIncomes() {
        //SETUP
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            final List<FixedIncomeHolding> filtered = List.of(mock(FixedIncomeHolding.class));
            mockedFilterUtils.when(() -> FilterUtils.filterHoldings(eq(holdings), eq(FIXED_INCOME_PREDICATE))).thenReturn(filtered);
            doCallRealMethod().when(maturityAllocationCacheStorage).load(any(), any(), any(), any());

            //ACT
            maturityAllocationCacheStorage.load(holdings, List.of(), warnings, new ParamHolderDTO());

            //VERIFY
            verify(maturityAllocationCacheStorage).loadBenchOfFixedIncomes(filtered, List.of());
        }
    }


    @Test
    void mapForNoneStock_verifyMaturityAllocationMapper() {
        //SETUP
        final Map<Holding, RMaturityAllocation> holdingMaturityAllocationMap = Map.of(holding, rMaturityAllocation);

        doCallRealMethod().when(maturityAllocationCacheStorage).mapForNoneStock(any(), any());
        //ACT

        maturityAllocationCacheStorage.mapForNoneStock(holdingMaturityAllocationMap, warnings);

        //VERIFY
        verify(maturityAllocationCacheStorage).maturityAllocationMapper(
                argThat(arg -> arg.getKey() == holding && arg.getValue() == rMaturityAllocation),
                eq(warnings)
        );
    }

    @Test
    void mapForNoneStock_checkResult() {
        //SETUP
        final Map<Holding, RMaturityAllocation> holdingMaturityAllocationMap = Map.of(holding, rMaturityAllocation);
        final Map<MaturityAllocationType, BigDecimal> actualValue = Map.of(MaturityAllocationType.FIVE_TO_SEVEN_YEARS, BigDecimal.TEN);
        when(maturityAllocationCacheStorage.maturityAllocationMapper(any(), any())).thenReturn(actualValue);
        doCallRealMethod().when(maturityAllocationCacheStorage).mapForNoneStock(any(), any());

        //ACT
        final Map<Holding, Map<MaturityAllocationType, BigDecimal>> actual = maturityAllocationCacheStorage.mapForNoneStock(holdingMaturityAllocationMap, warnings);

        //VERIFY
        assertEquals(Map.of(holding, actualValue), actual);
    }

    @Test
    void maturityAllocationMapper_checkResult() {
        //SETUP
        when(entry.getKey()).thenReturn(holding);

        final Map<String, BigDecimal> emptyMap = Map.of();

        when(rMaturityAllocation.getMaturityDurationValues()).thenReturn(emptyMap);

        when(entry.getValue()).thenReturn(rMaturityAllocation);

        doCallRealMethod().when(maturityAllocationCacheStorage).maturityAllocationMapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<MaturityAllocationType, BigDecimal> actual = maturityAllocationCacheStorage.maturityAllocationMapper(entry, warnings);

        //VERIFY
        assertEquals(DEFAULT_MAP, actual);
        assertEquals(1, warnings.size());
        assertEquals(WRN_MA_MA_001.getMessage(), warnings.get(0).getMessage());
        assertEquals(WRN_MA_MA_001.name(), warnings.get(0).getCode());
    }

    @Test
    void maturityAllocationMapper_checkResult2() {
        //SETUP
        when(entry.getKey()).thenReturn(holding);

        final Map<String, BigDecimal> emptyMap = Map.of();

        when(rMaturityAllocation.getMaturityDurationValues()).thenReturn(emptyMap);
        when(entry.getValue()).thenReturn(rMaturityAllocation);
        when(rMaturityAllocation.getMaturityDurationValues()).thenReturn(Map.of("TEST", BigDecimal.ONE));

        doCallRealMethod().when(maturityAllocationCacheStorage).maturityAllocationMapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<MaturityAllocationType, BigDecimal> actual = maturityAllocationCacheStorage.maturityAllocationMapper(entry, warnings);

        //VERIFY
        assertEquals(DEFAULT_MAP, actual);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).getMessage().contains("TEST"));
    }

    @Test
    void maturityAllocationMapper_checkResult3() {
        //SETUP
        when(entry.getKey()).thenReturn(holding);

        final Map<String, BigDecimal> emptyMap = Map.of();

        when(rMaturityAllocation.getMaturityDurationValues()).thenReturn(emptyMap);
        when(entry.getValue()).thenReturn(rMaturityAllocation);
        when(rMaturityAllocation.getMaturityDurationValues()).thenReturn(Map.of(MaturityAllocationType.FIVE_TO_SEVEN_YEARS.name(), BigDecimal.ONE));

        doCallRealMethod().when(maturityAllocationCacheStorage).maturityAllocationMapper(any(), any());
        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<MaturityAllocationType, BigDecimal> actual = maturityAllocationCacheStorage.maturityAllocationMapper(entry, warnings);

        //VERIFY
        final HashMap<MaturityAllocationType, BigDecimal> expected = new HashMap<>(DEFAULT_MAP);
        expected.put(MaturityAllocationType.FIVE_TO_SEVEN_YEARS, BigDecimal.ONE);

        assertEquals(expected, actual);
        assertEquals(0, warnings.size());
    }

    @Test
    void maturityAllocationMapper_checkConsolidatedResult() {
        //SETUP
        final Map<String, BigDecimal> maturityValues = getMaturityValuesMap();

        when(entry.getKey()).thenReturn(holding);
        when(rMaturityAllocation.getMaturityDurationValues()).thenReturn(maturityValues);
        when(entry.getValue()).thenReturn(rMaturityAllocation);
        doCallRealMethod().when(maturityAllocationCacheStorage).maturityAllocationMapper(any(), any());

        //ACT
        final List<Warning> warnings = new ArrayList<>();
        final Map<MaturityAllocationType, BigDecimal> actual = maturityAllocationCacheStorage.maturityAllocationMapper(entry, warnings);

        //VERIFY
        final HashMap<MaturityAllocationType, BigDecimal> expected = new HashMap<>(DEFAULT_MAP);
        expected.put(MaturityAllocationType.UNDER_ONE_YEAR, new BigDecimal(5));
        expected.put(MaturityAllocationType.MORE_THAN_TWENTY_YEARS, new BigDecimal(2));
        assertEquals(expected, actual);
        assertEquals(0, warnings.size());
    }

    private Map<String, BigDecimal> getMaturityValuesMap() {
        final var value = BigDecimal.ONE;
        final var maturityAllocationTypes = List.of(
                MaturityAllocationType.ONE_TO_SEVEN_DAYS,
                MaturityAllocationType.EIGHT_TO_THIRTY_DAYS,
                MaturityAllocationType.THIRTYONE_TO_NINTY_DAYS,
                MaturityAllocationType.NINTYONE_TO_182_DAYS,
                MaturityAllocationType.ONEHUNDREDANDEIGHTYTHREE_TO_364_DAYS,
                MaturityAllocationType.TWENTY_TO_THIRTY_YEARS,
                MaturityAllocationType.MORE_THAN_THIRTY_YEARS
        );

        return maturityAllocationTypes
                .stream()
                .map(maturityAllocationType -> Map.entry(maturityAllocationType.name(), value))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}