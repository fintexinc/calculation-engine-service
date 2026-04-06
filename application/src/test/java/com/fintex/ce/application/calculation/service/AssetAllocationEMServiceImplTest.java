package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegionEmType;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.mapping.CountryAllocationMappingService;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.ASIA_PACIFIC_EQUITIES;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.CANADIAN_EQUITIES;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.CASH;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.EM_EQUITIES;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.EUROPEAN_EQUITIES;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.FIXED_INCOME;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.INTERNATIONAL_EQUITIES;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.OTHER;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.UNCLASSIFIED;
import static com.fintex.ce.domain.model.calculation.AssetAllocationRegion.US_EQUITIES;
import static com.fintex.sm.model.DataProvider.MORNINGSTAR;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AssetAllocationEMServiceImplTest {

  @Test
  void shouldCalculateEquityDiff_whenCheckResult() {
    // SETUP
    final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

    final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
        countryAllocationFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

    final Holding h = mock(Holding.class);
    final Map<CountryRegionType, BigDecimal> cRegions = Map.of(CountryRegionType.EMERGING_MARKET, TEN);
    Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocations = Map.of(h, cRegions);

    final Map<AssetAllocationRegion, BigDecimal> aRegions = Map.of(
        CANADIAN_EQUITIES, BigDecimal.valueOf(3),
        CASH, BigDecimal.valueOf(3));
    final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations = Map.of(h, aRegions);

    doCallRealMethod().when(service).calculateEquityDiff(any(), any(), any(), any());
    // ACT
    final BigDecimal actual = service.calculateEquityDiff(countryAllocations, assetAllocations, Set.of(CANADIAN_EQUITIES),
        h);

    // VERIFY
    assertEquals(BigDecimal.valueOf(-7), actual);
  }

  @Test
  void shouldCalculateEquityDiff_whenCheckResult2() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Map<CountryRegionType, BigDecimal> cRegions = Map.of(CountryRegionType.EMERGING_MARKET, TEN);
    Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocations = Map.of(new Holding(null,
        FinancialInstrumentType.CASH, null), cRegions);

    final Map<AssetAllocationRegion, BigDecimal> aRegions = Map.of(
        CANADIAN_EQUITIES, BigDecimal.valueOf(3),
        CASH, BigDecimal.valueOf(3));
    final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations = Map.of(h, aRegions);

    doCallRealMethod().when(a).calculateEquityDiff(any(), any(), any(), any());
    // ACT
    final BigDecimal actual = a.calculateEquityDiff(countryAllocations, assetAllocations, Set.of(CANADIAN_EQUITIES), h);

    // VERIFY
    assertEquals(BigDecimal.valueOf(3), actual);
  }

  @Test
  void shouldCalculateEquityDifference_whenCheckResult1() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Map<CountryRegionType, BigDecimal> cRegions = Map.of(CountryRegionType.EMERGING_MARKET, TEN);
    Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocations = Map.of(new Holding(null,
        FinancialInstrumentType.CASH, null), cRegions);

    final Map<AssetAllocationRegion, BigDecimal> aRegions = Map.of(
        CANADIAN_EQUITIES, BigDecimal.valueOf(3),
        CASH, BigDecimal.valueOf(3));
    final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> assetAllocations = Map.of(h, aRegions);

    doCallRealMethod().when(a).calculateEquityDiff(any(), any(), any(), any());
    doCallRealMethod().when(a).calculateEquityDifference(any(), any(), any());
    // ACT
    final Map<Holding, BigDecimal> actual = a.calculateEquityDifference(List.of(h), countryAllocations,
        assetAllocations);

    // VERIFY
    final Set<AssetAllocationRegion> equities = Set.of(CANADIAN_EQUITIES, US_EQUITIES, EUROPEAN_EQUITIES,
        ASIA_PACIFIC_EQUITIES, EM_EQUITIES, INTERNATIONAL_EQUITIES);
    verify(a).calculateEquityDiff(countryAllocations, assetAllocations, equities, h);
    assertEquals(1, actual.size());
    assertTrue(actual.containsKey(h));
    assertTrue(actual.containsValue(BigDecimal.valueOf(3)));
  }

  @Test
  void shouldEmForInternationalEquity_whenCheckResult() {
    // SETUP
    final AssetAllocationEMServiceImpl a = new AssetAllocationEMServiceImpl(null, null, null, null,
            DEFAULT_DATA_PROPERTIES);

    final Holding h = mock(Holding.class);
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(
        CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40));
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    // ACT
    final BigDecimal actual = a.emForInternationalEquity(h, countryAllocations, equityDifference);

    // VERIFY
    assertEquals(BigDecimal.valueOf(45), actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenCheckResultCASH() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    // if eagle
    final Holding h = mock(Holding.class);
    final BigDecimal expected = BigDecimal.valueOf(6);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        CASH, expected,
        ASIA_PACIFIC_EQUITIES, TEN);

    // if mrstar
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED,
        BigDecimal.valueOf(40));
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final BigDecimal actual = a.getEmergingMarketValue(
        h, Pair.of(DataProvider.MORNINGSTAR, assetAllocations), countryAllocations, equityDifference,
        AssetAllocationRegionEmType.CASH);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenCheckResultFIXEDINCOME() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    // if eagle
    final Holding h = mock(Holding.class);
    final BigDecimal expected = BigDecimal.valueOf(6);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        FIXED_INCOME, expected,
        ASIA_PACIFIC_EQUITIES, TEN);

    // if mrstar
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED,
        BigDecimal.valueOf(40));
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final BigDecimal actual = a.getEmergingMarketValue(
        h, Pair.of(DataProvider.MORNINGSTAR, assetAllocations), countryAllocations, equityDifference,
        AssetAllocationRegionEmType.FIXED_INCOME);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenCheckResultCANADIANEQUITY() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final BigDecimal expected = BigDecimal.valueOf(6);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        CANADIAN_EQUITIES, BigDecimal.valueOf(99),
        ASIA_PACIFIC_EQUITIES, TEN);

    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(
        CountryRegionType.CANADA, expected);
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final BigDecimal actual = a.getEmergingMarketValue(
        h, Pair.of(DataProvider.MORNINGSTAR, assetAllocations), countryAllocations, equityDifference,
        AssetAllocationRegionEmType.CANADIAN_EQUITY);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenCheckResultUSEQUITY() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final BigDecimal expected = BigDecimal.valueOf(6);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        US_EQUITIES, BigDecimal.valueOf(99),
        ASIA_PACIFIC_EQUITIES, TEN);

    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(
        CountryRegionType.UNITED_STATES, expected);
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final BigDecimal actual = a.getEmergingMarketValue(
        h, Pair.of(DataProvider.MORNINGSTAR, assetAllocations), countryAllocations, equityDifference,
        AssetAllocationRegionEmType.US_EQUITY);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenVerifyINTERNATIONALEQUITY() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    // if eagle
    final Holding h = mock(Holding.class);
    final BigDecimal expected = BigDecimal.valueOf(6);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        US_EQUITIES, expected,
        ASIA_PACIFIC_EQUITIES, TEN);

    // if mrstar
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED,
        BigDecimal.valueOf(40));
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    final BigDecimal actual = a.getEmergingMarketValue(
        h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.INTERNATIONAL_EQUITY);

    // VERIFY
    verify(a).emForInternationalEquity(h, countryAllocations, equityDifference);
  }

  @Test
  void shouldGetEmergingMarketValue_whenVerifyCANADAEQUITYFromCountryAllocations() {
    // SETUP
    final var service = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final BigDecimal expected = TEN;
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(CANADIAN_EQUITIES, BigDecimal.valueOf(11),
        US_EQUITIES, BigDecimal.valueOf(100));
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(
        CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40),
        CountryRegionType.CANADA, expected);
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
    final var pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);

    doCallRealMethod().when(service).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final BigDecimal actual = service.getEmergingMarketValue(
        h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.CANADIAN_EQUITY);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenReturnZeroForCANADIANEQUITY_whenNoCanadaInCountryAllocations() {
    // SETUP
    final var service = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(CANADIAN_EQUITIES, BigDecimal.valueOf(6),
        US_EQUITIES, TEN);
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED,
        BigDecimal.valueOf(40));
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
    final var pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);

    doCallRealMethod().when(service).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final BigDecimal actual = service.getEmergingMarketValue(
        h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.CANADIAN_EQUITY);

    // VERIFY
    assertEquals(ZERO, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenVerifyUSEQUITYFromCountryAllocations() {
    // SETUP
    final var service = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final BigDecimal expected = TEN;
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(CANADIAN_EQUITIES, BigDecimal.valueOf(11),
        US_EQUITIES, BigDecimal.valueOf(100));
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(
        CountryRegionType.INTERNATIONAL_DEVELOPED, BigDecimal.valueOf(40),
        CountryRegionType.UNITED_STATES, expected);
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
    final var pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);

    doCallRealMethod().when(service).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final BigDecimal actual = service.getEmergingMarketValue(
        h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.US_EQUITY);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenReturnZeroForUSEQUITY_whenNoUnitedStatesInCountryAllocations() {
    // SETUP
    final var service = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(CANADIAN_EQUITIES, TEN,
        US_EQUITIES, BigDecimal.valueOf(6));
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED,
        BigDecimal.valueOf(40));
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
    final var pair = Pair.of(DataProvider.MORNINGSTAR, assetAllocations);

    doCallRealMethod().when(service).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final BigDecimal actual = service.getEmergingMarketValue(
        h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.US_EQUITY);

    // VERIFY
    assertEquals(ZERO, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenVerifyEMERGINGMARKETEQUITY() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final BigDecimal expected = BigDecimal.valueOf(6);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        EM_EQUITIES, BigDecimal.valueOf(99),
        ASIA_PACIFIC_EQUITIES, TEN);

    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(
        CountryRegionType.EMERGING_MARKET, expected);
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    final BigDecimal actual = a.getEmergingMarketValue(
        h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.EMERGING_MARKET_EQUITY);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenVerifyEMERGINGMARKETEQUITY2() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    // if eagle
    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        EM_EQUITIES, BigDecimal.valueOf(7),
        ASIA_PACIFIC_EQUITIES, TEN);

    // if mrstar
    final BigDecimal expected = BigDecimal.valueOf(6);
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, expected);
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    final BigDecimal actual = a.getEmergingMarketValue(
        h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.EMERGING_MARKET_EQUITY);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenVerifyOTHER() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    // if eagle
    final Holding h = mock(Holding.class);
    final BigDecimal expected = BigDecimal.valueOf(6);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        OTHER, expected,
        ASIA_PACIFIC_EQUITIES, TEN);

    // if mrstar
    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal
        .valueOf(3));
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    doCallRealMethod().when(a).getEmergingMarketValue(any(), any(), any(), any(), any());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    final BigDecimal actual = a.getEmergingMarketValue(
        h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.OTHER);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenVerifyUNCLASSIFIED() {
    // SETUP
    final var a = new AssetAllocationEMServiceImpl(null, null, null, null, DEFAULT_DATA_PROPERTIES);
    final var expected = new BigDecimal("1.1");
    final var map = Map.<AssetAllocationRegion, BigDecimal>of(UNCLASSIFIED, expected);
    final var pair = new ImmutablePair<>(DataProvider.MORNINGSTAR, map);

    // ACT
    final BigDecimal actual = a.getEmergingMarketValue(mock(Holding.class), pair, null, null,
        AssetAllocationRegionEmType.UNCLASSIFIED);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetEmergingMarketValue_whenCheckResult() {
    // SETUP
    final var service = mock(AssetAllocationEMServiceImpl.class);
    final var map = new HashMap<>();
    final var pair = mock(Pair.class);

    when(pair.getValue()).thenReturn(map);
    doCallRealMethod().when(service).getEmergingMarketValue(any(), any(), any(), any(), any());

    // ACT
    final BigDecimal actual = service.getEmergingMarketValue(null, pair, null, null, null);

    // VERIFY
    assertEquals(ZERO, actual);
  }

  @Test
  void shouldCalculateEmergingMarket_whenVerifyGetEmergingMarketValue() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        OTHER, BigDecimal.valueOf(6),
        ASIA_PACIFIC_EQUITIES, TEN);

    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal
        .valueOf(3));
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    when(a.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

    doCallRealMethod().when(a).calculateEmergingMarket(any(), any(), any(), any());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    a.calculateEmergingMarket(
        Map.of(h, pair), Map.of(h, countryAllocations), equityDifference, h);

    // VERIFY
    verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.CASH);
    verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference,
        AssetAllocationRegionEmType.FIXED_INCOME);
    verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference,
        AssetAllocationRegionEmType.CANADIAN_EQUITY);
    verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference,
        AssetAllocationRegionEmType.US_EQUITY);
    verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference,
        AssetAllocationRegionEmType.INTERNATIONAL_EQUITY);
    verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference,
        AssetAllocationRegionEmType.EMERGING_MARKET_EQUITY);
    verify(a).getEmergingMarketValue(h, pair, countryAllocations, equityDifference, AssetAllocationRegionEmType.OTHER);
  }

  @Test
  void shouldCalculateEmergingMarket_whenCheckResult() {
    // SETUP
    final AssetAllocationEMServiceImpl a = mock(AssetAllocationEMServiceImpl.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        OTHER, BigDecimal.valueOf(6),
        ASIA_PACIFIC_EQUITIES, TEN);

    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal
        .valueOf(3));
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));

    when(a.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

    doCallRealMethod().when(a).calculateEmergingMarket(any(), any(), any(), any());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    final Map<AssetAllocationRegionEmType, BigDecimal> actual = a.calculateEmergingMarket(
        Map.of(h, pair), Map.of(h, countryAllocations), equityDifference, h);

    // VERIFY
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
  void shouldCalculateAssetAllocationEMarketMap_whenVerifyLoadWithDataProvidersCheck() {
    // SETUP
    final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

    final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
        countryAllocationFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

    final var providers = mock(List.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        OTHER, BigDecimal.valueOf(6),
        ASIA_PACIFIC_EQUITIES, TEN);

    when(service.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

    doCallRealMethod().when(service).calculateAssetAllocationEMarketMap(any(), any(), anyList());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    final List<Warning> warnings = List.of(mock(Warning.class));
    service.calculateAssetAllocationEMarketMap(List.of(h), Map.of(h, pair), providers);

    // VERIFY
    verify(countryAllocationFetcher).fetch(eq(List.of(h)), eq(providers));
  }

  @Test
  void shouldCalculateAssetAllocationEMarketMap_whenVerifyCalculateEquityDifference() {
    // SETUP
    final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

    final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
        countryAllocationFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

    final var providers = mock(List.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        OTHER, BigDecimal.valueOf(6),
        ASIA_PACIFIC_EQUITIES, TEN);

    when(service.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

    final Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocationsMap = Map.of(h, Map.of(
        CountryRegionType.INTERNATIONAL_DEVELOPED, ONE));
    when(countryAllocationMappingService.mapToCountryRegions(any(), any(), any())).thenReturn(
        countryAllocationsMap);

    doCallRealMethod().when(service).retrieveAssetAllocations(any());
    doCallRealMethod().when(service).calculateAssetAllocationEMarketMap(any(), any(), anyList());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    final List<Warning> warnings = List.of(mock(Warning.class));
    service.calculateAssetAllocationEMarketMap(List.of(h), Map.of(h, pair), providers);

    // VERIFY
    verify(service).calculateEquityDifference(List.of(h), countryAllocationsMap, Map.of(h, assetAllocations));
  }

  @Test
  void shouldCalculateAssetAllocationEMarketMap_whenVerifyCalculateEmergingMarket() {
    // SETUP
    final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

    final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
        countryAllocationFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

    final var providers = mock(List.class);

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        OTHER, BigDecimal.valueOf(6),
        ASIA_PACIFIC_EQUITIES, TEN);

    final Map<CountryRegionType, BigDecimal> countryAllocations = Map.of(CountryRegionType.EMERGING_MARKET, BigDecimal
        .valueOf(3));

    when(service.getEmergingMarketValue(any(), any(), any(), any(), any())).thenReturn(ONE);

    final Map<Holding, Map<CountryRegionType, BigDecimal>> countryAllocationsMap = Map.of(h, Map.of(
        CountryRegionType.INTERNATIONAL_DEVELOPED, ONE));
    when(countryAllocationMappingService.mapToCountryRegions(any(), any(), any())).thenReturn(
        countryAllocationsMap);
    final Map<Holding, BigDecimal> equityDifference = Map.of(h, BigDecimal.valueOf(5));
    when(service.calculateEquityDifference(any(), any(), any())).thenReturn(equityDifference);

    doCallRealMethod().when(service).calculateAssetAllocationEMarketMap(any(), any(), anyList());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    final List<Warning> warnings = List.of(mock(Warning.class));
    service.calculateAssetAllocationEMarketMap(List.of(h), Map.of(h, pair), providers);

    // VERIFY
    verify(service).calculateEmergingMarket(Map.of(h, pair), countryAllocationsMap, equityDifference, h);
  }

  @Test
  void shouldRetrieveAssetAllocations_whenCheckResult() {
    // SETUP
    final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

    final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
        countryAllocationFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

    final Holding h = mock(Holding.class);
    final Map<AssetAllocationRegion, BigDecimal> assetAllocations = Map.of(
        OTHER, BigDecimal.valueOf(6),
        ASIA_PACIFIC_EQUITIES, TEN);

    doCallRealMethod().when(service).retrieveAssetAllocations(any());
    // ACT
    final Pair<DataProvider, Map<AssetAllocationRegion, BigDecimal>> pair = Pair.of(DataProvider.MORNINGSTAR,
        assetAllocations);
    final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> actual = service.retrieveAssetAllocations(Map.of(h, pair));

    // VERIFY
    assertEquals(Map.of(h, assetAllocations), actual);
  }

  @Test
  void shouldPerform_whenVerifyValidateHoldings() {
    // SETUP
    final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

    final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
        countryAllocationFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

    final PortfolioHoldingsCommand reqDto = mock(PortfolioHoldingsCommand.class);
    final List<Holding> holdings = List.of(mock(Holding.class));
    when(reqDto.getHoldings()).thenReturn(holdings);
    when(service.fetchExposures(any())).thenReturn(new ExposureDataHolder<>(Map.of(), List.of()));

    doCallRealMethod().when(service).perform(any());
    // ACT
    service.perform(reqDto);

    // VERIFY
  }

  @Test
  void shouldFetch_whenCheckResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

      final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
          countryAllocationFetcher, assetAllocationFetcher,
          assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

      final var holding = mock(Holding.class);
      final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));
      when(service.calculateAssetAllocationEMarketMap(any(), any(), anyList())).thenReturn(new ExposureDataHolder<>(exposures, List.of()));
      doCallRealMethod().when(service).fetchExposures(any());
      // ACT
      final var result = service.fetchExposures(mock(PortfolioHoldingsCommand.class));
    final var actual = result.allocations();

      // VERIFY
      assertEquals(exposures, actual);
    }
  }

  @Test
  void shouldFetch_whenVerifyMapForAAEM() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

      final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
          countryAllocationFetcher, assetAllocationFetcher,
          assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

      final var req = mock(PortfolioHoldingsCommand.class);
      final List<Warning> warnings = List.of();
      when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());
      when(assetAllocationDataMapper.toRegionExposuresWithProvider(any())).thenReturn(Map.of());

      doCallRealMethod().when(service).fetchExposures(any());
      // ACT
      service.fetchExposures(req);

      // VERIFY
      verify(assetAllocationDataMapper).toRegionExposuresWithProvider(any());
    }
  }

  @Test
  void shouldFetch_whenVerifyValidate() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

      final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
          countryAllocationFetcher, assetAllocationFetcher,
          assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

      final var req = mock(PortfolioHoldingsCommand.class);
      final List<Warning> warnings = List.of();
      when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());
      when(assetAllocationDataMapper.toRegionExposuresWithProvider(any())).thenReturn(Map.of());

      doCallRealMethod().when(service).fetchExposures(any());
      // ACT
      service.fetchExposures(req);

      // VERIFY
      verify(assetAllocationDataMapper).toRegionExposuresWithProvider(any());
    }
  }

  @Test
  void shouldFetch_whenVerifyCheck() {
    // SETUP
    final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

    final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
        countryAllocationFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

    final var req = mock(PortfolioHoldingsCommand.class);
    final var providers = List.of(DataProvider.MORNINGSTAR, DataProvider.MORNINGSTAR);
    when(req.getDataProviders()).thenReturn(providers);
    when(assetAllocationFetcher.fetch(any(), any())).thenReturn(Map.of());
    when(assetAllocationDataMapper.toRegionExposuresWithProvider(any())).thenReturn(Map.of());

    doCallRealMethod().when(service).fetchExposures(any());
    // ACT
    service.fetchExposures(req);

    // VERIFY
    verify(assetAllocationFetcher).fetch(any(), any());
  }

  @Test
  void shouldFetch_whenVerifyLoadWithDataProvidesCheck() {
    // SETUP
    final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);
    final var warnings = new ArrayList<Warning>();

    final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
        countryAllocationFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

    final var holding = mock(Holding.class);
    final var portfolioHoldingsReqDTO = mock(PortfolioHoldingsCommand.class);
    final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));
    final var providers = List.of(DataProvider.MORNINGSTAR);

    when(portfolioHoldingsReqDTO.getHoldings()).thenReturn(List.of(holding));
    when(portfolioHoldingsReqDTO.getDataProviders()).thenReturn(providers);
    when(service.calculateAssetAllocationEMarketMap(any(), any(), anyList())).thenReturn(new ExposureDataHolder<>(exposures, List.of()));
    doCallRealMethod().when(service).fetchExposures(any());

    // ACT
    service.fetchExposures(portfolioHoldingsReqDTO);

    // VERIFY
    verify(assetAllocationFetcher).fetch(eq(List.of(holding)), any());
  }

  @Test
  void shouldFetch_whenVerifyCalculateAssetAllocationEMarketMap() {
    // SETUP
    final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

    final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
        countryAllocationFetcher, assetAllocationFetcher,
        assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

    final var holding = mock(Holding.class);
    final Map assetAllocations = mock(Map.class);
    final PortfolioHoldingsCommand portfolioHoldingsReqDTO = mock(PortfolioHoldingsCommand.class);
    final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));
    final var providers = List.of(DataProvider.MORNINGSTAR);
    final var mappedResult = mock(Map.class);

    when(portfolioHoldingsReqDTO.getHoldings()).thenReturn(List.of(holding));
    when(portfolioHoldingsReqDTO.getDataProviders()).thenReturn(providers);
    when(assetAllocationDataMapper.toRegionExposuresWithProvider(any())).thenReturn(assetAllocations);
    when(service.calculateAssetAllocationEMarketMap(any(), any(), anyList())).thenReturn(new ExposureDataHolder<>(exposures, List.of()));
    when(assetAllocationDataMapper.toRegionExposuresWithProvider(any())).thenReturn(mappedResult);
    doCallRealMethod().when(service).fetchExposures(any());
    // ACT
    service.fetchExposures(portfolioHoldingsReqDTO);

    // VERIFY
    verify(service).calculateAssetAllocationEMarketMap(eq(List.of(holding)), eq(mappedResult), eq(providers));
  }

  @Test
  void shouldFetch_whenVerifyGetSpecifiedIfEmpty() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var countryAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var countryAllocationMappingService = mock(CountryAllocationMappingService.class);

      final var service = mock(AssetAllocationEMServiceImpl.class, withSettings().useConstructor(
          countryAllocationFetcher, assetAllocationFetcher,
          assetAllocationDataMapper, countryAllocationMappingService, DEFAULT_DATA_PROPERTIES));

      final var holding = mock(Holding.class);
      final Map assetAllocations = mock(Map.class);
      final var providers = mock(List.class);
      final PortfolioHoldingsCommand portfolioHoldingsReqDTO = mock(PortfolioHoldingsCommand.class);
      final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));

      when(portfolioHoldingsReqDTO.getDataProviders()).thenReturn(providers);
      when(portfolioHoldingsReqDTO.getHoldings()).thenReturn(List.of(holding));
      when(assetAllocationDataMapper.toRegionExposuresWithProvider(any())).thenReturn(assetAllocations);
      when(service.calculateAssetAllocationEMarketMap(any(), any(), anyList())).thenReturn(new ExposureDataHolder<>(exposures, List.of()));
      doCallRealMethod().when(service).fetchExposures(any());
      // ACT
      service.fetchExposures(portfolioHoldingsReqDTO);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, List.of(MORNINGSTAR)), Mockito.times(2));
    }
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    // SETUP
    final var service = mock(AssetAllocationEMServiceImpl.class);

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.OTHER, TEN));
    doCallRealMethod().when(service).calculate(any(), any());

    // ACT
    service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

    // VERIFY
    verify(service).calculateNetProducts(exposures, holdings, AssetAllocationRegionEmType.values());
  }

  @Test
  void shouldCalculate_whenVerifyToUserScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class);
        var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      // SETUP
      final var service = mock(AssetAllocationEMServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(AssetAllocationRegionEmType.FIXED_INCOME, TEN));
      final var netProducts = mock(Map.class);

      when(service.calculateNetProducts(exposures, holdings, AssetAllocationRegionEmType.values())).thenReturn(netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      // VERIFY
      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(netProducts));
    }
  }

}