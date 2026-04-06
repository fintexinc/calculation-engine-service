package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.calculation.EquityMarketCapType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquityMarketCapResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.application.calculation.service.EquityMarketCapCalculationServiceImpl.DEFAULT_MAP;
import static com.fintex.ce.application.calculation.service.EquityMarketCapCalculationServiceImpl.GROUPS;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.GIANT;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.LARGE;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.MEDIUM;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.MICRO;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.SMALL;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityMarketCapCalculationServiceImplTest {

  @Test
  void shouldStaticFieldsInitialization_whenVerifyGROUPS() {
    // SETUP
    final var groupsExpected = Map.of(
        LARGE, Set.of(LARGE, GIANT),
        MEDIUM, Set.of(MEDIUM),
        SMALL, Set.of(SMALL, MICRO));

    // ACT

    // VERIFY
    Assertions.assertNotNull(groupsExpected);
    ComparisonUtils.compareMaps(groupsExpected, GROUPS);
  }

  @Test
  void shouldStaticFieldsInitialization_whenVerifyDEFAULTMAP() {
    // SETUP
    final Map<EquityMarketCapType, BigDecimal> defaultMapExpected = new HashMap<>();
    defaultMapExpected.put(LARGE, null);
    defaultMapExpected.put(MEDIUM, null);
    defaultMapExpected.put(SMALL, null);

    // ACT

    // VERIFY
    Assertions.assertNotNull(defaultMapExpected);
    ComparisonUtils.compareMaps(defaultMapExpected, DEFAULT_MAP);
  }

  @Test
  void shouldPerform_whenVerifyLoad() {
    // SETUP
    final var marketCapFetcher = mock(SecurityDataFetcher.class);
    final var service = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
        .useConstructor(marketCapFetcher));

    final var holdings = List.of(mock(Holding.class));
    final var req = mock(PortfolioHoldingsCommand.class);

    when(req.getHoldings()).thenReturn(holdings);

    doCallRealMethod().when(service).fetchExposures(any());
    // ACT
    service.fetchExposures(req);

    // VERIFY
    verify(marketCapFetcher).fetch(any(), any());
  }

  @Test
  void shouldPerform_whenVerifyAreAllValuesZerosInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var marketCapFetcher = mock(SecurityDataFetcher.class);
      final var service = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
          .useConstructor(marketCapFetcher));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldPerform_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var marketCapFetcher = mock(SecurityDataFetcher.class);
      final var service = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
          .useConstructor(marketCapFetcher));

      final var exposures = mock(Map.class);
      final var expected = new EquityMarketCapResult();
      expected.setEquityMarketCapitalization(EquityMarketCapCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(true);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      // VERIFY
      Assertions.assertEquals(expected, actual);
    }
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {

      // SETUP
      final var service = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquityMarketCapType.SMALL, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      // VERIFY
      verify(service).calculateNetProducts(exposures, holdings, EquityMarketCapType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var service = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquityMarketCapType.SMALL, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      when(service.calculateNetProducts(exposures, holdings, EquityMarketCapType.values())).thenReturn(netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      // VERIFY
      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyGroupedResults() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var service = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var reScaled = mock(Map.class);
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(netProducts)).thenReturn(reScaled);
      when(service.calculateNetProducts(exposures, holdings, EquityMarketCapType.values())).thenReturn(netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      // VERIFY
      verify(service).groupedResults(reScaled);
    }
  }

  @Test
  void shouldCalculate_whenVerifyToUserScale() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var service = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var groupedResults = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      when(service.groupedResults(any())).thenReturn(groupedResults);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      // VERIFY
      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(groupedResults));
    }
  }

  @Test
  void shouldCalculate_whenCheckResult() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP

      final var service = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var groupedResults = mock(Map.class);
      final var expected = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      mockedDecimalUtils.when(() -> DecimalUtils.toUserScale(groupedResults)).thenReturn(expected);
      when(service.groupedResults(any())).thenReturn(groupedResults);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      // VERIFY
      Assertions.assertNotNull(actual);
      ComparisonUtils.compareMaps(expected, actual.getEquityMarketCapitalization());
    }
  }

  @Test
  void shouldGroupedResults_whenVerifyCalculateSumWithingTheSameGroupForEachOfGROUPS() {
    // SETUP
    final var service = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = mock(Map.class);

    when(service.calculateSumWithinTheSameGroup(any(), any())).thenReturn(TEN);

    doCallRealMethod().when(service).groupedResults(any());
    // ACT
    service.groupedResults(netProducts);

    // VERIFY
    for (var entry : GROUPS.entrySet()) {
      verify(service).calculateSumWithinTheSameGroup(netProducts, entry);
    }
  }

  @Test
  void shouldGroupedResults_whenCheckResult() {
    // SETUP
    final var service = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = Map.of(EquityMarketCapType.SMALL, TEN);
    final var expectedResult = GROUPS.keySet().stream().collect(toMap(e -> e, e -> TEN));

    when(service.calculateSumWithinTheSameGroup(any(), any())).thenReturn(TEN);

    doCallRealMethod().when(service).groupedResults(any());
    // ACT
    final var actualResult = service.groupedResults(netProducts);

    // VERIFY
    Assertions.assertNotNull(actualResult);
    ComparisonUtils.compareMaps(expectedResult, actualResult);
  }

  @Test
  void shouldCalculateSumWithinTheSameGroup_whenCheckResult1() {
    // SETUP
    final var service = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = Map.of(
        EquityMarketCapType.MICRO, BigDecimal.valueOf(5),
        EquityMarketCapType.SMALL, BigDecimal.valueOf(6));
    final var expected = Map.of(EquityMarketCapType.SMALL, BigDecimal.valueOf(11));
    final var entry = new AbstractMap.SimpleEntry<>(SMALL, Set.of(SMALL, MICRO));

    doCallRealMethod().when(service).calculateSumWithinTheSameGroup(any(), any());
    // ACT
    final var actual = service.calculateSumWithinTheSameGroup(netProducts, entry);

    // VERIFY
    assertEquals(expected.get(SMALL), actual);
  }

  @Test
  void shouldCalculateSumWithinTheSameGroup_whenCheckResult2() {
    // SETUP
    final var service = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = Map.of(EquityMarketCapType.MEDIUM, ZERO);
    final var expected = Map.of(EquityMarketCapType.MEDIUM, ZERO);
    final var entry = new AbstractMap.SimpleEntry<>(MEDIUM, Set.of(MEDIUM));

    doCallRealMethod().when(service).calculateSumWithinTheSameGroup(any(), any());
    // ACT
    final var actual = service.calculateSumWithinTheSameGroup(netProducts, entry);

    // VERIFY
    assertEquals(expected.get(MEDIUM), actual);
  }

  @Test
  void shouldCalculateSumWithinTheSameGroup_whenCheckResult3() {
    // SETUP
    final var service = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = Map.of(
        EquityMarketCapType.LARGE, BigDecimal.valueOf(7),
        EquityMarketCapType.GIANT, BigDecimal.valueOf(8));
    final var expected = Map.of(EquityMarketCapType.LARGE, BigDecimal.valueOf(15));
    final var entry = new AbstractMap.SimpleEntry<>(LARGE, Set.of(LARGE, GIANT));

    doCallRealMethod().when(service).calculateSumWithinTheSameGroup(any(), any());
    // ACT
    final var actual = service.calculateSumWithinTheSameGroup(netProducts, entry);

    // VERIFY
    assertEquals(expected.get(LARGE), actual);
  }

}