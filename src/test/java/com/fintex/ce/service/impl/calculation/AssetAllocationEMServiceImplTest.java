package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegionEmType;
import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.mapper.AssetAllocationDataMapper;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.AssetAllocationCacheStorage;
import com.fintex.ce.service.impl.cache.EquityCountryAllocationCacheStorage;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.data.DataProviderChecker;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.fintex.ce.config.enumeration.DataProvider.DEFAULT_PROVIDERS;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.ASIA_PACIFIC_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.CANADIAN_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.CASH;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.EM_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.EUROPEAN_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.FIXED_INCOME;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.INTERNATIONAL_EQUITIES;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.OTHER;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.UNCLASSIFIED;
import static com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion.US_EQUITIES;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AssetAllocationEMServiceImplTest {

    @Test
    void calculateEquityDiff_checkResult() {
        //SETUP
        final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                assetAllocationDataMapper, dataProviderChecker, requestValidator));

        final Holding h = mock(Holding.class);
        final Map<CountryRegionType, BigDecimal> cRegions = Map.of(CountryRegionType.EMERGING_MARKET, TEN);
        Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocations = Map.of(h, cRegions);

        final Map<AssetAllocationRegion, BigDecimal> aRegions = Map.of(
                AssetAllocationRegion.CANADIAN_EQUITIES, BigDecimal.valueOf(3),
                AssetAllocationRegion.CASH, BigDecimal.valueOf(3)
        );
        final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations = Map.of(h, aRegions);

        doCallRealMethod().when(sut).calculateEquityDiff(any(), any(), any(), any());
        //ACT
        final BigDecimal actual = sut.calculateEquityDiff(countryAllocations, assetAllocations, Set.of(AssetAllocationRegion.CANADIAN_EQUITIES), h);

        //VERIFY
        assertEquals(BigDecimal.valueOf(-7), actual);
    }

    @Test
    void calculateEquityDiff_checkResult2() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        final Holding h = mock(Holding.class);
        final Map<CountryRegionType, BigDecimal> cRegions = Map.of(CountryRegionType.EMERGING_MARKET, TEN);
        Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocations = Map.of(new Holding().setType(HoldingType.CASH), cRegions);

        final Map<AssetAllocationRegion, BigDecimal> aRegions = Map.of(
                AssetAllocationRegion.CANADIAN_EQUITIES, BigDecimal.valueOf(3),
                AssetAllocationRegion.CASH, BigDecimal.valueOf(3)
        );
        final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations = Map.of(h, aRegions);

        doCallRealMethod().when(a).calculateEquityDiff(any(), any(), any(), any());
        //ACT
        final BigDecimal actual = a.calculateEquityDiff(countryAllocations, assetAllocations, Set.of(AssetAllocationRegion.CANADIAN_EQUITIES), h);

        //VERIFY
        assertEquals(BigDecimal.valueOf(3), actual);
    }

    @Test
    void calculateEquityDifference_checkResult1() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        final Holding h = mock(Holding.class);
        final Map<CountryRegionType, BigDecimal> cRegions = Map.of(CountryRegionType.EMERGING_MARKET, TEN);
        Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocations = Map.of(new Holding().setType(HoldingType.CASH), cRegions);

        final Map<AssetAllocationRegion, BigDecimal> aRegions = Map.of(
                AssetAllocationRegion.CANADIAN_EQUITIES, BigDecimal.valueOf(3),
                AssetAllocationRegion.CASH, BigDecimal.valueOf(3)
        );
        final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations = Map.of(h, aRegions);

        doCallRealMethod().when(a).calculateEquityDiff(any(), any(), any(), any());
        doCallRealMethod().when(a).calculateEquityDifference(any(), any(), any());
        //ACT
        final Map<Holding, BigDecimal> actual = a.calculateEquityDifference(List.of(h), countryAllocations, assetAllocations);

        //VERIFY
        final Set<AssetAllocationRegion> equities = Set.of(CANADIAN_EQUITIES, US_EQUITIES, EUROPEAN_EQUITIES, ASIA_PACIFIC_EQUITIES, EM_EQUITIES, INTERNATIONAL_EQUITIES);
        verify(a).calculateEquityDiff(countryAllocations, assetAllocations, equities, h);
        assertEquals(1, actual.size());
        assertTrue(actual.containsKey(h));
        assertTrue(actual.containsValue(BigDecimal.valueOf(3)));
    }

    @Test
    void selectEmergingValueForDataProvider_checkResult() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        final Supplier<BigDecimal> supplierE = mock(Supplier.class);
        when(supplierE.get()).thenReturn(ONE);
        final Supplier<BigDecimal> supplierM = mock(Supplier.class);
        when(supplierM.get()).thenReturn(TEN);

        doCallRealMethod().when(a).selectEmergingValueForDataProvider(any(), any(), any(), any());
        //ACT
        final BigDecimal actual = a.selectEmergingValueForDataProvider(DataProvider.EAGLE, supplierE, supplierM, mock(Holding.class));

        //VERIFY
        assertEquals(actual, supplierE.get());
        verify(supplierM, times(0)).get();
    }

    @Test
    void selectEmergingValueForDataProvider_checkResult2() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        final Supplier<BigDecimal> supplierE = mock(Supplier.class);
        when(supplierE.get()).thenReturn(ONE);
        final Supplier<BigDecimal> supplierM = mock(Supplier.class);
        when(supplierM.get()).thenReturn(TEN);

        doCallRealMethod().when(a).selectEmergingValueForDataProvider(any(), any(), any(), any());
        //ACT
        final BigDecimal actual = a.selectEmergingValueForDataProvider(DataProvider.MORNINGSTAR, supplierE, supplierM, mock(Holding.class));

        //VERIFY
        assertEquals(actual, supplierM.get());
        verify(supplierE, times(0)).get();
    }

    @Test
    void selectEmergingValueForDataProvider_checkResult3() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        final Supplier<BigDecimal> supplierE = mock(Supplier.class);
        when(supplierE.get()).thenReturn(ONE);
        final Supplier<BigDecimal> supplierM = mock(Supplier.class);
        when(supplierM.get()).thenReturn(TEN);

        doCallRealMethod().when(a).selectEmergingValueForDataProvider(any(), any(), any(), any());
        //ACT
        a.selectEmergingValueForDataProvider(DataProvider.EAGLE, supplierE, supplierM, mock(Holding.class));

        //VERIFY
        verify(supplierE, times(1)).get();
    }

    @Test
    void emForInternationalEquity_verifySelectEmergingValueForDataProvider_eagle() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                AssetAllocationRegion.EUROPEAN_EQUITIES, ONE,
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        doCallRealMethod().when(a).emForInternationalEquity(any(), any(), any(), any());
        //ACT
        a.emForInternationalEquity(h, Pair.of(DataProvider.EAGLE, assetAllocations), countryAllocations, equityDifference);

        //VERIFY
        verify(a)
                .selectEmergingValueForDataProvider(
                        eq(DataProvider.EAGLE),
                        argThat(arg -> arg.get().compareTo(BigDecimal.valueOf(1 + 10)) == 0),
                        argThat(arg -> arg.get().compareTo(BigDecimal.valueOf(40 + 5)) == 0),
                        eq(h)
                );
    }

    @Test
    void emForInternationalEquity_checkResult() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                AssetAllocationRegion.EUROPEAN_EQUITIES, ONE,
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        final BigDecimal expected = mock(BigDecimal.class);
        when(a.selectEmergingValueForDataProvider(any(), any(), any(), any())).thenReturn(expected);

        doCallRealMethod().when(a).emForInternationalEquity(any(), any(), any(), any());
        //ACT
        final BigDecimal actual = a.emForInternationalEquity(h, Pair.of(DataProvider.EAGLE, assetAllocations), countryAllocations, equityDifference);

        //VERIFY
        assertSame(expected, actual);
    }

    @Test
    void getEmergingMarketValue_checkResult_CASH() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                CASH, expected,
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final BigDecimal actual = a.getEmergingMarketValue(
                h, Pair.of(DataProvider.EAGLE, assetAllocations), countryAllocations, equityDifference, AssetAllocationRegionEmType.CASH);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_checkResult_FIXED_INCOME() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                FIXED_INCOME, expected,
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final BigDecimal actual = a.getEmergingMarketValue(
                h, Pair.of(DataProvider.EAGLE, assetAllocations), countryAllocations, equityDifference, AssetAllocationRegionEmType.FIXED_INCOME);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_checkResult_CANADIAN_EQUITY() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                CANADIAN_EQUITIES, expected,
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        doCallRealMethod().when(a).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final BigDecimal actual = a.getEmergingMarketValue(
                h, Pair.of(DataProvider.EAGLE, assetAllocations), countryAllocations, equityDifference, AssetAllocationRegionEmType.CANADIAN_EQUITY);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_checkResult_US_EQUITY() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                US_EQUITIES, expected,
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        doCallRealMethod().when(a).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final BigDecimal actual = a.getEmergingMarketValue(
                h, Pair.of(DataProvider.EAGLE, assetAllocations), countryAllocations, equityDifference, AssetAllocationRegionEmType.US_EQUITY);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_verify_INTERNATIONAL_EQUITY() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                US_EQUITIES, expected,
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        doCallRealMethod().when(a).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.EAGLE, assetAllocations);
        final BigDecimal actual = a.getEmergingMarketValue(
                h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.INTERNATIONAL_EQUITY);

        //VERIFY
        verify(a).emForInternationalEquity(h, pair, countryAllocations, equityDifference);
    }

    @Test
    void getEmergingMarketValue_verify_CANADA_EQUITY_fromCountryAllocations() {
        //SETUP
        final var sut = mock(AssetAllocationEMServiceImpl.class);

        final Holding h = mock(Holding.class);
        final BigDecimal expected = TEN;
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(CANADIAN_EQUITIES, BigDecimal.valueOf(11),
                AssetAllocationRegion.US_EQUITIES, BigDecimal.valueOf(100));
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(
                CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40),
                CountryRegionType.CANADA, expected
        );
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
        final var pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);

        doCallRealMethod().when(sut).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(sut).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final BigDecimal actual = sut.getEmergingMarketValue(
                h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.CANADIAN_EQUITY);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_verify_CANADA_EQUITY_fromAssetAllocation() {
        //SETUP
        final var sut = mock(AssetAllocationEMServiceImpl.class);

        final Holding h = mock(Holding.class);
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(CANADIAN_EQUITIES, expected,
                AssetAllocationRegion.US_EQUITIES, TEN);
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED,
                BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
        final var pair = Pair.of(DataProvider.EAGLE, assetAllocations);

        doCallRealMethod().when(sut).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(sut).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final BigDecimal actual = sut.getEmergingMarketValue(
                h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.CANADIAN_EQUITY);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_verify_US_EQUITY_fromCountryAllocations() {
        //SETUP
        final var sut = mock(AssetAllocationEMServiceImpl.class);

        final Holding h = mock(Holding.class);
        final BigDecimal expected = TEN;
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(CANADIAN_EQUITIES, BigDecimal.valueOf(11),
                AssetAllocationRegion.US_EQUITIES, BigDecimal.valueOf(100));
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(
                CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40),
                CountryRegionType.UNITED_STATES, expected
        );
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
        final var pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);

        doCallRealMethod().when(sut).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(sut).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final BigDecimal actual = sut.getEmergingMarketValue(
                h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.US_EQUITY);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_verify_US_EQUITY_fromAssetAllocation() {
        //SETUP
        final var sut = mock(AssetAllocationEMServiceImpl.class);

        final Holding h = mock(Holding.class);
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(CANADIAN_EQUITIES, TEN,
                AssetAllocationRegion.US_EQUITIES, expected);
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED,
                BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
        final var pair = Pair.of(DataProvider.EAGLE, assetAllocations);

        doCallRealMethod().when(sut).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(sut).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final BigDecimal actual = sut.getEmergingMarketValue(
                h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.US_EQUITY);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_verify_EMERGING_MARKET_EQUITY() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                EM_EQUITIES, expected,
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        doCallRealMethod().when(a).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.EAGLE, assetAllocations);
        final BigDecimal actual = a.getEmergingMarketValue(
                h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.EMERGING_MARKET_EQUITY);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_verify_EMERGING_MARKET_EQUITY_2() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                EM_EQUITIES, BigDecimal.valueOf(7),
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, expected);
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        doCallRealMethod().when(a).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);
        final BigDecimal actual = a.getEmergingMarketValue(
                h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.EMERGING_MARKET_EQUITY);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_verify_OTHER() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        // if eagle
        final Holding h = mock(Holding.class);
        final BigDecimal expected = BigDecimal.valueOf(6);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                OTHER, expected,
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        // if mrstar
        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal.valueOf(3));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        doCallRealMethod().when(a).selectEmergingValueForDataProvider(any(), any(), any(), any());
        doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);
        final BigDecimal actual = a.getEmergingMarketValue(
                h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.OTHER);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void getEmergingMarketValue_verifyUNCLASSIFIED() {
        //SETUP
        final var sut = mock(AssetAllocationEMServiceImpl.class);
        final var unclassifiedRegion = AssetAllocationRegionEmType.UNCLASSIFIED;
        final var bigDecimal = new BigDecimal("1.1");
        final var map = Map.of(UNCLASSIFIED, bigDecimal);
        final var dataProvider = DataProvider.MORNINGSTAR;
        final var holding = mock(Holding.class);
        final var pair = new ImmutablePair<>(dataProvider, map);

        doCallRealMethod().when(sut).getEmergingMarketValue(any(), any(), any(), any(), any());

        //ACT
        sut.getEmergingMarketValue(holding, pair, null, null, unclassifiedRegion);

        //VERIFY
        verify(sut).selectEmergingValueForDataProvider(eq(dataProvider), argThat(arg -> arg.get().equals(bigDecimal)),
                argThat(arg -> arg.get().equals(bigDecimal)), eq(holding));
    }

    @Test
    void getEmergingMarketValue_checkResult() {
        //SETUP
        final var sut = mock(AssetAllocationEMServiceImpl.class);
        final var map = new HashMap<>();
        final var pair = mock(Pair.class);

        when(pair.getValue()).thenReturn(map);
        doCallRealMethod().when(sut).getEmergingMarketValue(any(), any(), any(), any(), any());

        //ACT
        final BigDecimal actual = sut.getEmergingMarketValue(null, pair, null, null, null);

        //VERIFY
        assertEquals(ZERO, actual);
    }

    @Test
    void calculateEmergingMarket_verifyGetEmergingMarketValue() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                OTHER, BigDecimal.valueOf(6),
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal.valueOf(3));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        when(a.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

        doCallRealMethod().when(a).calculateEmergingMarket(any(), any(), any(), any());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);
        a.calculateEmergingMarket(
                Map.of(h, pair), Map.of(h, countryAllocations), equityDifference, h);

        //VERIFY
        verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.CASH);
        verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.FIXED_INCOME);
        verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.CANADIAN_EQUITY);
        verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.US_EQUITY);
        verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.INTERNATIONAL_EQUITY);
        verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.EMERGING_MARKET_EQUITY);
        verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.OTHER);
    }

    @Test
    void calculateEmergingMarket_checkResult() {
        //SETUP
        final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                OTHER, BigDecimal.valueOf(6),
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal.valueOf(3));
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

        when(a.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

        doCallRealMethod().when(a).calculateEmergingMarket(any(), any(), any(), any());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);
        final Map<AssetAllocationRegionEmType, BigDecimal> actual = a.calculateEmergingMarket(
                Map.of(h, pair), Map.of(h, countryAllocations), equityDifference, h);

        //VERIFY
        Map<AssetAllocationRegionEmType, BigDecimal> expected = new HashMap<>();
        expected.put(AssetAllocationRegionEmType.CASH, ONE);
        expected.put(AssetAllocationRegionEmType.FIXED_INCOME, ONE);
        expected.put(AssetAllocationRegionEmType.CANADIAN_EQUITY, ONE);
        expected.put(AssetAllocationRegionEmType.US_EQUITY, ONE);
        expected.put(AssetAllocationRegionEmType.INTERNATIONAL_EQUITY, ONE);
        expected.put(AssetAllocationRegionEmType.EMERGING_MARKET_EQUITY, ONE);
        expected.put(AssetAllocationRegionEmType.OTHER, ONE);
        expected.put(AssetAllocationRegionEmType.UNCLASSIFIED, ONE);

        assertEquals(expected, actual);
    }

    @Test
    void calculateAssetAllocationEMarketMap_verifyLoadWithDataProvidersCheck() {
        //SETUP
        final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                assetAllocationDataMapper, dataProviderChecker, requestValidator));

        final var providers = mock(List.class);

        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                OTHER, BigDecimal.valueOf(6),
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        when(sut.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

        doCallRealMethod().when(sut).calculateAssetAllocationEMarketMap(any(), any(), anyList(), anyList());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);
        final List<Warning> warnings = List.of(mock(Warning.class));
        sut.calculateAssetAllocationEMarketMap(List.of(h), Map.of(h, pair), providers, warnings);

        //VERIFY
        verify(equityCountryAllocationCacheStorage).loadWithDataProvidersCheck(List.of(h), providers, warnings);
    }

    @Test
    void calculateAssetAllocationEMarketMap_verifyCalculateEquityDifference() {
        //SETUP
        final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                assetAllocationDataMapper, dataProviderChecker, requestValidator));

        final var providers = mock(List.class);

        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                OTHER, BigDecimal.valueOf(6),
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        when(sut.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

        final Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocationsMap = Map.of(h, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, ONE));
        when(equityCountryAllocationCacheStorage.loadWithDataProvidersCheck(anyList(), anyList(), anyList())).thenReturn(countryAllocationsMap);

        doCallRealMethod().when(sut).retrieveAssetAllocations(any());
        doCallRealMethod().when(sut).calculateAssetAllocationEMarketMap(any(), any(), anyList(), anyList());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);
        final List<Warning> warnings = List.of(mock(Warning.class));
        sut.calculateAssetAllocationEMarketMap(List.of(h), Map.of(h, pair), providers, warnings);

        //VERIFY
        verify(sut).calculateEquityDifference(List.of(h), countryAllocationsMap, Map.of(h, assetAllocations));
    }

    @Test
    void calculateAssetAllocationEMarketMap_verifyCalculateEmergingMarket() {
        //SETUP
        final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                assetAllocationDataMapper, dataProviderChecker, requestValidator));

        final var providers = mock(List.class);

        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                OTHER, BigDecimal.valueOf(6),
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal.valueOf(3));

        when(sut.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

        final Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocationsMap = Map.of(h, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, ONE));
        when(equityCountryAllocationCacheStorage.loadWithDataProvidersCheck(anyList(), anyList(), anyList())).thenReturn(countryAllocationsMap);
        final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
        when(sut.calculateEquityDifference(any(), any(), any())).thenReturn(equityDifference);

        doCallRealMethod().when(sut).calculateAssetAllocationEMarketMap(any(), any(), anyList(), anyList());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);
        final List<Warning> warnings = List.of(mock(Warning.class));
        sut.calculateAssetAllocationEMarketMap(List.of(h), Map.of(h, pair), providers, warnings);

        //VERIFY
        verify(sut).calculateEmergingMarket(Map.of(h, pair), countryAllocationsMap, equityDifference, h);
    }

    @Test
    void retrieveAssetAllocations_checkResult() {
        //SETUP
        final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                assetAllocationDataMapper, dataProviderChecker, requestValidator));

        final Holding h = mock(Holding.class);
        final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
                OTHER, BigDecimal.valueOf(6),
                AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, TEN
        );

        doCallRealMethod().when(sut).retrieveAssetAllocations(any());
        //ACT
        final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);
        final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> actual = sut.retrieveAssetAllocations(Map.of(h, pair));

        //VERIFY
        assertEquals(Map.of(h, assetAllocations), actual);
    }

    @Test
    void perform_verifyValidateHoldings() {
        //SETUP
        final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
        final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
        final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
        final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
        final var dataProviderChecker = mock(DataProviderChecker.class);
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

        final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                assetAllocationDataMapper, dataProviderChecker, requestValidator));

        final PortfolioHoldingsReqDTO reqDto = mock(PortfolioHoldingsReqDTO.class);
        final List<Holding> holdings = List.of(mock(Holding.class));
        when(reqDto.getHoldings()).thenReturn(holdings);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDto);

        //VERIFY
        requestValidator.validate(reqDto);
    }

    @Test
    void getLoadFromCacheStorage_checkResult() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                    equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                    assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var holding = mock(Holding.class);
            final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));
            when(sut.calculateAssetAllocationEMarketMap(any(), any(), anyList(), anyList())).thenReturn(exposures);
            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            final var actual = sut.getLoadFromCacheStorage(mock(PortfolioHoldingsReqDTO.class), List.of());

            //VERIFY
            assertEquals(exposures, actual);
        }
    }

    @Test
    void getLoadFromCacheStorage_verifyMapForAAEM() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                    equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                    assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var req = mock(PortfolioHoldingsReqDTO.class);
            final List<Warning> warnings = List.of();
            final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
            when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(assetAllocationDataDto);

            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            sut.getLoadFromCacheStorage(req, warnings);

            //VERIFY
            verify(assetAllocationDataMapper).mapForAAEM(assetAllocationDataDto);
        }
    }

    @Test
    void getLoadFromCacheStorage_verifyValidate() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                    equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                    assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var req = mock(PortfolioHoldingsReqDTO.class);
            final List<Warning> warnings = List.of();
            final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
            when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(assetAllocationDataDto);

            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            sut.getLoadFromCacheStorage(req, warnings);

            //VERIFY
            verify(assetAllocationDataValidator).validate(assetAllocationDataDto, warnings);
        }
    }

    @Test
    void getLoadFromCacheStorage_verifyCheck() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                    equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                    assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var req = mock(PortfolioHoldingsReqDTO.class);
            when(req.getDataProviders()).thenReturn(null);
            final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
            when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(assetAllocationDataDto);
            final var defaultProviders = List.of(DEFAULT_PROVIDERS);
            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(any(), any())).thenReturn(defaultProviders);

            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            sut.getLoadFromCacheStorage(req, List.of());

            //VERIFY
            verify(dataProviderChecker).check(defaultProviders, assetAllocationDataDto);
        }
    }

    @Test
    void getLoadFromCacheStorage_verifyLoadWithDataProvidesCheck() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var warnings = new ArrayList<Warning>();

            final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                    equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                    assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var holding = mock(Holding.class);
            final var portfolioHoldingsReqDTO = mock(PortfolioHoldingsReqDTO.class);
            final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));
            final var defaultProviders = mock(List.class);

            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(anyList(), any())).thenReturn(defaultProviders);
            when(portfolioHoldingsReqDTO.getHoldings()).thenReturn(List.of(holding));
            when(sut.calculateAssetAllocationEMarketMap(any(), any(), anyList(), anyList())).thenReturn(exposures);
            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());

            //ACT
            sut.getLoadFromCacheStorage(portfolioHoldingsReqDTO, warnings);

            //VERIFY
            verify(assetAllocationCacheStorage).loadWithDataProvidesCheck(List.of(holding), defaultProviders, warnings);
        }
    }

    @Test
    void getLoadFromCacheStorage_verifyCalculateAssetAllocationEMarketMap() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                    equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                    assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var holding = mock(Holding.class);
            final var assetAllocations = mock(AssetAllocationDataDTO.class);
            final PortfolioHoldingsReqDTO portfolioHoldingsReqDTO = mock(PortfolioHoldingsReqDTO.class);
            final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));
            final var defaultProviders = mock(List.class);
            final var mappedResult = mock(Map.class);

            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(anyList(), any())).thenReturn(defaultProviders);
            when(portfolioHoldingsReqDTO.getHoldings()).thenReturn(List.of(holding));
            when(assetAllocationCacheStorage.loadWithDataProvidesCheck(any(), any(), any())).thenReturn(assetAllocations);
            when(sut.calculateAssetAllocationEMarketMap(any(), any(), anyList(), anyList())).thenReturn(exposures);
            when(assetAllocationDataMapper.mapForAAEM(assetAllocations)).thenReturn(mappedResult);
            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            sut.getLoadFromCacheStorage(portfolioHoldingsReqDTO, List.of());

            //VERIFY
            verify(sut).calculateAssetAllocationEMarketMap(List.of(holding), mappedResult, defaultProviders, List.of());
        }
    }

    @Test
    void getLoadFromCacheStorage_verifyGetSpecifiedIfEmpty() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var equityCountryAllocationCacheStorage = mock(EquityCountryAllocationCacheStorage.class);
            final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final var dataProviderChecker = mock(DataProviderChecker.class);
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);

            final var sut = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
                    equityCountryAllocationCacheStorage, assetAllocationCacheStorage, assetAllocationDataValidator,
                    assetAllocationDataMapper, dataProviderChecker, requestValidator));

            final var holding = mock(Holding.class);
            final var assetAllocations = mock(AssetAllocationDataDTO.class);
            final var providers = mock(List.class);
            final PortfolioHoldingsReqDTO portfolioHoldingsReqDTO = mock(PortfolioHoldingsReqDTO.class);
            final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));

            when(portfolioHoldingsReqDTO.getDataProviders()).thenReturn(providers);
            when(portfolioHoldingsReqDTO.getHoldings()).thenReturn(List.of(holding));
            when(assetAllocationCacheStorage.loadWithDataProvidesCheck(any(), any(), any())).thenReturn(assetAllocations);
            when(sut.calculateAssetAllocationEMarketMap(any(), any(), anyList(), anyList())).thenReturn(exposures);
            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            sut.getLoadFromCacheStorage(portfolioHoldingsReqDTO, List.of());

            //VERIFY
            mockedFilterUtils.verify(Mockito.times(3), () -> FilterUtils.getSpecifiedIfEmpty(providers, DEFAULT_PROVIDERS));
        }
    }

    @Test
    void calculate_verifyCalculateNetProducts() {
        //SETUP
        final var sut = mock(AssetAllocationEMServiceImpl.class);

        final var holding = mock(Holding.class);
        final var holdings = List.of(holding);
        final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));
        doCallRealMethod().when(sut).calculate(any(), any(), any());

        //ACT
        sut.calculate(exposures, holdings, List.of());

        //VERIFY
        verify(sut).calculateNetProducts(exposures, holdings, AssetAllocationRegionEmType.values());
    }

    @Test
    void calculate_verifyToUserScale() {
        try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
             var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class);
             var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
            //SETUP
            final var sut = mock(AssetAllocationEMServiceImpl.class);

            final var holding = mock(Holding.class);
            final var holdings = List.of(holding);
            final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.FIXED_INCOME, TEN));
            final var netProducts = mock(Map.class);

            when(sut.calculateNetProducts(exposures, holdings, AssetAllocationRegionEmType.values())).thenReturn(netProducts);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, holdings, List.of());

            //VERIFY
            mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(netProducts));
        }
    }

}