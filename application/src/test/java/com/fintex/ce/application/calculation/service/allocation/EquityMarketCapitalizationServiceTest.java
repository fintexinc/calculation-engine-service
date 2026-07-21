package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.HoldingEquityMarketCap;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.EquityMarketCapResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fintex.ce.application.calculation.service.allocation.EquityMarketCapitalizationService.DEFAULT_MAP;
import static com.fintex.ce.application.calculation.service.allocation.EquityMarketCapitalizationService.GROUPS;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.GIANT;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.LARGE;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.MEDIUM;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.MICRO;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.SMALL;
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

class EquityMarketCapitalizationServiceTest {

  @Test
  void shouldStaticFieldsInitialization_whenVerifyGROUPS() {
    final var groupsExpected = Map.of(
        LARGE, Set.of(LARGE, GIANT),
        MEDIUM, Set.of(MEDIUM),
        SMALL, Set.of(SMALL, MICRO));

    Assertions.assertNotNull(groupsExpected);
    ComparisonUtils.compareMaps(groupsExpected, GROUPS);
  }

  @Test
  void shouldStaticFieldsInitialization_whenVerifyDEFAULTMAP() {
    // SETUP
    final Map<EquityMarketCapitalizationType, BigDecimal> defaultMapExpected = new HashMap<>();
    defaultMapExpected.put(LARGE, null);
    defaultMapExpected.put(MEDIUM, null);
    defaultMapExpected.put(SMALL, null);

    Assertions.assertNotNull(defaultMapExpected);
    ComparisonUtils.compareMaps(defaultMapExpected, DEFAULT_MAP);
  }

  @Test
  void shouldPerform_whenVerifyLoad() {
    final var service = mock(EquityMarketCapitalizationService.class);

    final var holding = mock(PortfolioHolding.class);
    final var req = mock(PortfolioHoldingsCommand.class);

    when(req.getHoldings()).thenReturn(List.of(holding));
    final var marketCap = HoldingEquityMarketCap.builder()
        .ratings(Map.of(EquityMarketCapitalizationType.SMALL, TEN))
        .build();
    final var data = Map.of(holding, marketCap);

    doCallRealMethod().when(service).fetchExposures(any(), any());
    final var result = service.fetchExposures(req, data);

    Assertions.assertTrue(result.allocations().containsKey(holding));
    assertEquals(TEN, result.allocations().get(holding).get(EquityMarketCapitalizationType.SMALL));
  }

  @Test
  void shouldPerform_whenVerifyAreAllValuesZerosInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityMarketCapitalizationService.class);

      final var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldPerform_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityMarketCapitalizationService.class);

      final var exposures = mock(Map.class);
      final var expected = EquityMarketCapResult.builder()
          .equityMarketCapitalization(EquityMarketCapitalizationService.DEFAULT_MAP)
          .warnings(List.of())
          .build();
      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(true);

      doCallRealMethod().when(service).calculate(any(), any());
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      Assertions.assertEquals(expected, actual);
    }
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {

      final var service = mock(EquityMarketCapitalizationService.class);

      final var holding = mock(PortfolioHolding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquityMarketCapitalizationType.SMALL, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      // VERIFY
      verify(service).calculateNetProducts(exposures, holdings, EquityMarketCapitalizationType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityMarketCapitalizationService.class);

      final var holding = mock(PortfolioHolding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquityMarketCapitalizationType.SMALL, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      when(service.calculateNetProducts(exposures, holdings, EquityMarketCapitalizationType.values())).thenReturn(
          netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyGroupedResults() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityMarketCapitalizationService.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var reScaled = mock(Map.class);
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(netProducts)).thenReturn(reScaled);
      when(service.calculateNetProducts(exposures, holdings, EquityMarketCapitalizationType.values())).thenReturn(
          netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      verify(service).groupedResults(reScaled);
    }
  }

  @Test
  void shouldCalculate_whenVerifyToUserScale() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(EquityMarketCapitalizationService.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var groupedResults = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      when(service.groupedResults(any())).thenReturn(groupedResults);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(groupedResults));
    }
  }

  @Test
  void shouldCalculate_whenCheckResult() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {

      final var service = mock(EquityMarketCapitalizationService.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var groupedResults = mock(Map.class);
      final var expected = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(anyMap())).thenReturn(false);
      mockedDecimalUtils.when(() -> DecimalUtils.toUserScale(groupedResults)).thenReturn(expected);
      when(service.groupedResults(any())).thenReturn(groupedResults);

      doCallRealMethod().when(service).calculate(any(), any());
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      Assertions.assertNotNull(actual);
      ComparisonUtils.compareMaps(expected, actual.getEquityMarketCapitalization());
    }
  }

  @Test
  void shouldGroupedResults_whenVerifyCalculateSumWithingTheSameGroupForEachOfGROUPS() {
    final var service = mock(EquityMarketCapitalizationService.class);

    final var netProducts = mock(Map.class);

    when(service.calculateSumWithinTheSameGroup(any(), any())).thenReturn(TEN);

    doCallRealMethod().when(service).groupedResults(any());
    service.groupedResults(netProducts);

    for (var entry : GROUPS.entrySet()) {
      verify(service).calculateSumWithinTheSameGroup(netProducts, entry);
    }
  }

  @Test
  void shouldGroupedResults_whenCheckResult() {
    final var service = mock(EquityMarketCapitalizationService.class);

    final var netProducts = Map.of(EquityMarketCapitalizationType.SMALL, TEN);
    final var expectedResult = GROUPS.keySet().stream().collect(toMap(e -> e, e -> TEN));

    when(service.calculateSumWithinTheSameGroup(any(), any())).thenReturn(TEN);

    doCallRealMethod().when(service).groupedResults(any());
    final var actualResult = service.groupedResults(netProducts);

    Assertions.assertNotNull(actualResult);
    ComparisonUtils.compareMaps(expectedResult, actualResult);
  }

  @Test
  void shouldCalculateSumWithinTheSameGroup_whenCheckResult1() {
    final var service = mock(EquityMarketCapitalizationService.class);

    final var netProducts = Map.of(
        EquityMarketCapitalizationType.MICRO, BigDecimal.valueOf(5),
        EquityMarketCapitalizationType.SMALL, BigDecimal.valueOf(6));
    final var expected = Map.of(EquityMarketCapitalizationType.SMALL, BigDecimal.valueOf(11));
    final var entry = new AbstractMap.SimpleEntry<>(SMALL, Set.of(SMALL, MICRO));

    doCallRealMethod().when(service).calculateSumWithinTheSameGroup(any(), any());
    final var actual = service.calculateSumWithinTheSameGroup(netProducts, entry);

    assertEquals(expected.get(SMALL), actual);
  }

  @Test
  void shouldCalculateSumWithinTheSameGroup_whenCheckResult2() {
    final var service = mock(EquityMarketCapitalizationService.class);

    final var netProducts = Map.of(EquityMarketCapitalizationType.MEDIUM, ZERO);
    final var expected = Map.of(EquityMarketCapitalizationType.MEDIUM, ZERO);
    final var entry = new AbstractMap.SimpleEntry<>(MEDIUM, Set.of(MEDIUM));

    doCallRealMethod().when(service).calculateSumWithinTheSameGroup(any(), any());
    final var actual = service.calculateSumWithinTheSameGroup(netProducts, entry);

    assertEquals(expected.get(MEDIUM), actual);
  }

  @Test
  void shouldCalculateSumWithinTheSameGroup_whenCheckResult3() {
    final var service = mock(EquityMarketCapitalizationService.class);

    final var netProducts = Map.of(
        EquityMarketCapitalizationType.LARGE, BigDecimal.valueOf(7),
        EquityMarketCapitalizationType.GIANT, BigDecimal.valueOf(8));
    final var expected = Map.of(EquityMarketCapitalizationType.LARGE, BigDecimal.valueOf(15));
    final var entry = new AbstractMap.SimpleEntry<>(LARGE, Set.of(LARGE, GIANT));

    doCallRealMethod().when(service).calculateSumWithinTheSameGroup(any(), any());
    final var actual = service.calculateSumWithinTheSameGroup(netProducts, entry);

    assertEquals(expected.get(LARGE), actual);
  }

}