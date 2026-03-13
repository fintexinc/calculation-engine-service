package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.EquityMarketCapResult;
import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.application.service.calculation.EquityMarketCapCalculationServiceImpl.DEFAULT_MAP;
import static com.fintex.ce.application.service.calculation.EquityMarketCapCalculationServiceImpl.GROUPS;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.GIANT;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.LARGE;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.MEDIUM;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.MICRO;
import static com.fintex.ce.domain.enumeration.calculation.EquityMarketCapType.SMALL;
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
    final var marketCapCacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
        .useConstructor(marketCapCacheStorage));

    final var holdings = List.of(mock(Holding.class));
    final var req = mock(PortfolioHoldingsCommand.class);

    when(req.getHoldings()).thenReturn(holdings);

    doCallRealMethod().when(sut).fetchExposures(any(), any());
    // ACT
    sut.fetchExposures(req, List.of());

    // VERIFY
    verify(marketCapCacheStorage).load(req.getHoldings(), List.of(), List.of(), new ParamHolderDTO());
  }

  @Test
  void shouldPerform_whenVerifyAreAllValuesZerosInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var marketCapCacheStorage = mock(HoldingDataLoader.class);
      final var sut = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
          .useConstructor(marketCapCacheStorage));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldPerform_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var marketCapCacheStorage = mock(HoldingDataLoader.class);
      final var sut = mock(EquityMarketCapCalculationServiceImpl.class, withSettings()
          .useConstructor(marketCapCacheStorage));

      final var exposures = mock(Map.class);
      final var expected = new EquityMarketCapResult();
      expected.setEquityMarketCapitalization(EquityMarketCapCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(true);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final var actual = sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      Assertions.assertEquals(expected, actual);
    }
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {

      // SETUP
      final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquityMarketCapType.SMALL, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      verify(sut).calculateNetProducts(exposures, holdings, EquityMarketCapType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquityMarketCapType.SMALL, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      when(sut.calculateNetProducts(exposures, holdings, EquityMarketCapType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyGroupedResults() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var reScaled = mock(Map.class);
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(netProducts)).thenReturn(reScaled);
      when(sut.calculateNetProducts(exposures, holdings, EquityMarketCapType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      verify(sut).groupedResults(reScaled);
    }
  }

  @Test
  void shouldCalculate_whenVerifyToUserScale() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var groupedResults = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      when(sut.groupedResults(any())).thenReturn(groupedResults);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(groupedResults));
    }
  }

  @Test
  void shouldCalculate_whenCheckResult() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP

      final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var groupedResults = mock(Map.class);
      final var expected = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      mockedDecimalUtils.when(() -> DecimalUtils.toUserScale(groupedResults)).thenReturn(expected);
      when(sut.groupedResults(any())).thenReturn(groupedResults);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final var actual = sut.calculate(exposures, holdings, List.of());

      // VERIFY
      Assertions.assertNotNull(actual);
      ComparisonUtils.compareMaps(expected, actual.getEquityMarketCapitalization());
    }
  }

  @Test
  void shouldGroupedResults_whenVerifyCalculateSumWithingTheSameGroupForEachOfGROUPS() {
    // SETUP
    final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = mock(Map.class);

    when(sut.calculateSumWithinTheSameGroup(any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).groupedResults(any());
    // ACT
    sut.groupedResults(netProducts);

    // VERIFY
    for (var entry : GROUPS.entrySet()) {
      verify(sut).calculateSumWithinTheSameGroup(netProducts, entry);
    }
  }

  @Test
  void shouldGroupedResults_whenCheckResult() {
    // SETUP
    final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = Map.of(EquityMarketCapType.SMALL, TEN);
    final var expectedResult = GROUPS.keySet().stream().collect(toMap(e -> e, e -> TEN));

    when(sut.calculateSumWithinTheSameGroup(any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).groupedResults(any());
    // ACT
    final var actualResult = sut.groupedResults(netProducts);

    // VERIFY
    Assertions.assertNotNull(actualResult);
    ComparisonUtils.compareMaps(expectedResult, actualResult);
  }

  @Test
  void shouldCalculateSumWithinTheSameGroup_whenCheckResult1() {
    // SETUP
    final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = Map.of(
        EquityMarketCapType.MICRO, BigDecimal.valueOf(5),
        EquityMarketCapType.SMALL, BigDecimal.valueOf(6));
    final var expected = Map.of(EquityMarketCapType.SMALL, BigDecimal.valueOf(11));
    final var entry = new AbstractMap.SimpleEntry<>(SMALL, Set.of(SMALL, MICRO));

    doCallRealMethod().when(sut).calculateSumWithinTheSameGroup(any(), any());
    // ACT
    final var actual = sut.calculateSumWithinTheSameGroup(netProducts, entry);

    // VERIFY
    assertEquals(expected.get(SMALL), actual);
  }

  @Test
  void shouldCalculateSumWithinTheSameGroup_whenCheckResult2() {
    // SETUP
    final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = Map.of(EquityMarketCapType.MEDIUM, ZERO);
    final var expected = Map.of(EquityMarketCapType.MEDIUM, ZERO);
    final var entry = new AbstractMap.SimpleEntry<>(MEDIUM, Set.of(MEDIUM));

    doCallRealMethod().when(sut).calculateSumWithinTheSameGroup(any(), any());
    // ACT
    final var actual = sut.calculateSumWithinTheSameGroup(netProducts, entry);

    // VERIFY
    assertEquals(expected.get(MEDIUM), actual);
  }

  @Test
  void shouldCalculateSumWithinTheSameGroup_whenCheckResult3() {
    // SETUP
    final var sut = mock(EquityMarketCapCalculationServiceImpl.class);

    final var netProducts = Map.of(
        EquityMarketCapType.LARGE, BigDecimal.valueOf(7),
        EquityMarketCapType.GIANT, BigDecimal.valueOf(8));
    final var expected = Map.of(EquityMarketCapType.LARGE, BigDecimal.valueOf(15));
    final var entry = new AbstractMap.SimpleEntry<>(LARGE, Set.of(LARGE, GIANT));

    doCallRealMethod().when(sut).calculateSumWithinTheSameGroup(any(), any());
    // ACT
    final var actual = sut.calculateSumWithinTheSameGroup(netProducts, entry);

    // VERIFY
    assertEquals(expected.get(LARGE), actual);
  }

}