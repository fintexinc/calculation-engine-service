package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.calculation.holding.HoldingAggregator;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.correlation.HoldingsKeyResult;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingData;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CommonHoldingsServiceImplTest {

  @Test
  void shouldPerform_whenVerifyCalculateInitialPortfolioWeight() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var command = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(PortfolioHolding.class));

      when(command.getHoldings()).thenReturn(holdings);
      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());

      doCallRealMethod().when(service).perform(any());
      // ACT
      service.perform(command);

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
    }
  }

  @Test
  void shouldPerform_whenVerifyLoad() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var command = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(PortfolioHolding.class));
      final var allocations = Map.of(mock(PortfolioHolding.class), TEN);

      when(command.getHoldings()).thenReturn(holdings);
      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);

      doCallRealMethod().when(service).perform(any());
      // ACT
      service.perform(command);

      // VERIFY
      verify(fetcher).fetch(eq(holdings), any());
    }
  }

  @Test
  void shouldPerform_whenVerifyGetNumOfFundsMin() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var command = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(PortfolioHolding.class));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());
      when(command.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(Map.of());

      doCallRealMethod().when(service).perform(any());
      // ACT
      service.perform(command);

      // VERIFY
      verify(service).getNumOfFundsMin(command);
    }
  }

  @Test
  void shouldPerform_whenVerifyverifyGetAccumulativeTypes() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var command = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(PortfolioHolding.class));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());
      when(command.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(Map.of());
      when(service.getNumOfFundsMin(any())).thenReturn(1);

      doCallRealMethod().when(service).perform(any());
      // ACT
      service.perform(command);

      // VERIFY
      verify(service).getAccumulativeTypes(command);
    }
  }

  @Test
  void shouldPerform_whenVerifyCalculateCalculateTopCommonHoldings() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var command = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(PortfolioHolding.class));

      final var rawCommonHoldings = new CommonTopHoldings(List.of());
      final var holdingsFromSms = Map.of(mock(PortfolioHolding.class), rawCommonHoldings);
      final var allocations = Map.of(mock(PortfolioHolding.class), mock(BigDecimal.class));
      final var accumulativeTypes = Set.of("E");
      final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHolding.class)));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
      when(command.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(holdingsFromSms);
      when(service.getNumOfFundsMin(any())).thenReturn(1);
      when(service.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
      when(service.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);

      doCallRealMethod().when(service).perform(any());
      // ACT
      service.perform(command);

      // VERIFY
      verify(service).calculateTopCommonHoldings(anyMap(), eq(allocations), eq(accumulativeTypes));
    }
  }

  @Test
  void shouldPerform_whenVerifyCalculateFilterTop10Common() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var command = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(PortfolioHolding.class));

      final var rawCommonHoldings = new CommonTopHoldings(List.of());
      final var holdingsFromSms = Map.of(mock(PortfolioHolding.class), rawCommonHoldings);
      final var allocations = Map.of(mock(PortfolioHolding.class), mock(BigDecimal.class));
      final var accumulativeTypes = Set.of("E");
      final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHolding.class)));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
      when(command.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(holdingsFromSms);
      when(service.getNumOfFundsMin(any())).thenReturn(1);
      when(service.getTopCommonHoldingsNumber(any())).thenReturn(123);
      when(service.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
      when(service.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);

      doCallRealMethod().when(service).perform(any());
      // ACT
      service.perform(command);

      // VERIFY
      verify(service).filterTopCommon(1, 123, leaves);
    }
  }

  @Test
  void shouldPerform_whenVerifytoFinalResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var command = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(PortfolioHolding.class));

      final var rawCommonHoldings = new CommonTopHoldings(List.of());
      final var holdingsFromSms = Map.of(mock(PortfolioHolding.class), rawCommonHoldings);
      final var allocations = Map.of(mock(PortfolioHolding.class), mock(BigDecimal.class));
      final var accumulativeTypes = Set.of("E");
      final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHolding.class)));
      final var sortedLeaves = Map.of(mock(HoldingAggregator.class), TEN);

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
      when(command.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(holdingsFromSms);
      when(service.getNumOfFundsMin(any())).thenReturn(1);
      when(service.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
      when(service.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);
      when(service.filterTopCommon(anyInt(), anyInt(), any())).thenReturn(sortedLeaves);

      doCallRealMethod().when(service).perform(any());
      // ACT
      service.perform(command);

      // VERIFY
      verify(service).toFinalResult(leaves, sortedLeaves);
    }
  }

  @Test
  void shouldPerform_whenCheckResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var command = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(PortfolioHolding.class));

      final var rawCommonHoldings = new CommonTopHoldings(List.of());
      final var holdingsFromSms = Map.of(mock(PortfolioHolding.class), rawCommonHoldings);
      final var allocations = Map.of(mock(PortfolioHolding.class), mock(BigDecimal.class));
      final var accumulativeTypes = Set.of("E");
      final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHolding.class)));
      final var sortedLeaves = Map.of(mock(HoldingAggregator.class), TEN);
      final var topCommonHoldings = List.of(mock(TopCommonHoldingData.class));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(allocations);
      when(command.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(holdingsFromSms);
      when(service.getNumOfFundsMin(any())).thenReturn(1);
      when(service.getAccumulativeTypes(any())).thenReturn(accumulativeTypes);
      when(service.calculateTopCommonHoldings(anyMap(), anyMap(), anySet())).thenReturn(leaves);
      when(service.filterTopCommon(anyInt(), anyInt(), any())).thenReturn(sortedLeaves);
      when(service.toFinalResult(anyMap(), anyMap())).thenReturn(topCommonHoldings);

      doCallRealMethod().when(service).perform(any());
      // ACT
      final TopCommonHoldingsResult actual = service.perform(command);

      // VERIFY
      assertEquals(topCommonHoldings, actual.getCommonHoldings());
    }
  }

  @Test
  void shouldToFinalResult_whenVerifyMapToFinalResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHolding.class)));
    final var aggregator = mock(HoldingAggregator.class);
    final var ten = TEN;
    final var sortedLeaves = Map.of(aggregator, ten);
    final var entry = new AbstractMap.SimpleEntry<>(aggregator, ten);

    doCallRealMethod().when(service).toFinalResult(anyMap(), anyMap());
    // ACT
    var actual = service.toFinalResult(leaves, sortedLeaves);

    // VERIFY
    verify(service).mapToFinalResult(leaves, entry);
  }

  @Test
  void shouldToFinalResult_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var leaves = Map.of(mock(HoldingAggregator.class), List.of(mock(CommonHolding.class)));
    final var sortedLeaves = Map.of(mock(HoldingAggregator.class), TEN);
    final var expected = new TopCommonHoldingData();

    when(service.mapToFinalResult(anyMap(), any())).thenReturn(expected);

    doCallRealMethod().when(service).toFinalResult(anyMap(), anyMap());
    // ACT
    final List<TopCommonHoldingData> actual = service.toFinalResult(leaves, sortedLeaves);

    // VERIFY
    assertEquals(expected, actual.get(0));
  }

  @Test
  void shouldGetNumOfFundsMin_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var command = mock(TopCommonHoldingsCommand.class);
    final var expected = 1;

    when(command.getNumOfFundsMin()).thenReturn(null);

    doCallRealMethod().when(service).getNumOfFundsMin(any());
    // ACT
    final int actual = service.getNumOfFundsMin(command);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetNumOfFundsMin_whenCheckResult2() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var command = mock(TopCommonHoldingsCommand.class);
    final var expected = 7;

    when(command.getNumOfFundsMin()).thenReturn(expected);

    doCallRealMethod().when(service).getNumOfFundsMin(any());
    // ACT
    final int actual = service.getNumOfFundsMin(command);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetAccumulativeTypes_whenCheckResult() {
    // SETUP
    final var accumulativeTypes = Set.of("E");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulativeTypes));

    final var command = mock(TopCommonHoldingsCommand.class);

    when(command.getAccumulateHoldingTypes()).thenReturn(accumulativeTypes);

    doCallRealMethod().when(service).getAccumulativeTypes(any());
    // ACT
    final Set<String> actual = service.getAccumulativeTypes(command);

    // VERIFY
    assertEquals(accumulativeTypes, actual);
  }

  @Test
  void shouldGetAccumulativeTypes_whenCheckResult2() {
    // SETUP
    final var accumulativeTypes = Set.of();
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulativeTypes));

    final var command = mock(TopCommonHoldingsCommand.class);

    when(command.getAccumulateHoldingTypes()).thenReturn(Set.of());

    doCallRealMethod().when(service).getAccumulativeTypes(any());
    // ACT
    final Set<String> actual = service.getAccumulativeTypes(command);

    // VERIFY
    assertEquals(accumulativeTypes, actual);

  }

  @Test
  void shouldSecondLevelLeaves_whenCheckResult() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var firstLvlChild = mock(CommonHolding.class);

    when(firstLvlChild.getUnderlyingHoldings()).thenReturn(null);

    doCallRealMethod().when(service).secondLevelLeaves(firstLvlChild);
    // ACT
    final Stream<CommonHolding> actual = service.secondLevelLeaves(firstLvlChild);

    // VERIFY
    assertEquals(firstLvlChild, actual.findFirst().orElseThrow());
  }

  @Test
  void shouldSecondLevelLeaves_whenCheckResult2() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var firstLvlChild = mock(CommonHolding.class);
    final var underlyingHolding = mock(CommonHolding.class);
    final var underlyingHoldings = List.of(underlyingHolding);

    when(underlyingHolding.getCompanyName()).thenReturn(null);
    when(underlyingHolding.getName()).thenReturn(null);
    when(firstLvlChild.getUnderlyingHoldings()).thenReturn(underlyingHoldings);

    doCallRealMethod().when(service).secondLevelLeaves(firstLvlChild);
    // ACT
    final Stream<CommonHolding> actual = service.secondLevelLeaves(firstLvlChild);

    // VERIFY
    assertEquals(Stream.of().findFirst(), actual.findFirst());
  }

  @Test
  void shouldFilterTop10Common_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var holdingAggregator = mock(HoldingAggregator.class);
    final var commonHolding = mock(CommonHolding.class);
    final var leaves = Map.of(holdingAggregator, List.of(commonHolding));
    final var expected = Map.of(holdingAggregator, TEN);

    when(commonHolding.getWeight()).thenReturn(TEN);

    doCallRealMethod().when(service).filterTopCommon(anyInt(), anyInt(), anyMap());
    // ACT
    final Map<HoldingAggregator, BigDecimal> actual = service.filterTopCommon(1, 10, leaves);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldSetParentAndCalculateWeight_whenVerifyIsLeafStock() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var allocations = Map.of(new PortfolioHolding(null, null, null), TEN);
    final var parent = new PortfolioHolding(null, null, null);
    final var child = mock(CommonHolding.class);
    final var expected = new CommonHolding();
    expected.setWeight(TEN);
    expected.setHolding(parent);

    when(child.getValue()).thenReturn(TEN);
    doCallRealMethod().when(service).setParentAndCalculateWeight(anyMap(), any(), any());
    // ACT
    final CommonHolding actual = service.setParentAndCalculateWeight(allocations, parent, child);

    // VERIFY
    verify(service).isLeafStock(parent, child);
  }

  @Test
  void shouldSetParentAndCalculateWeight_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var allocations = Map.of(new PortfolioHolding(null, null, null), TEN);
    final var parent = new PortfolioHolding(null, null, null);
    final var child = new CommonHolding();
    child.setValue(TEN);

    when(service.isLeafStock(any(), any())).thenReturn(true);
    doCallRealMethod().when(service).setParentAndCalculateWeight(anyMap(), any(), any());
    // ACT
    final CommonHolding actual = service.setParentAndCalculateWeight(allocations, parent, child);

    // VERIFY
    assertEquals(0, BigDecimal.TEN.compareTo(actual.getWeight()));
    assertEquals(parent, actual.getHolding());
  }

  @Test
  void shouldSetParentAndCalculateWeight_whenCheckResult2() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var parent = mock(PortfolioHolding.class);
    final var allocations = Map.of(parent, TEN);
    final var child = new CommonHolding();
    child.setValue(TEN);
    child.setCompanyName("Apple Inc");
    child.setType("E");

    when(service.isLeafStock(any(), any())).thenReturn(false);
    doCallRealMethod().when(service).setParentAndCalculateWeight(anyMap(), any(), any());
    // ACT
    final CommonHolding actual = service.setParentAndCalculateWeight(allocations, parent, child);

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(100).compareTo(actual.getWeight()));
    assertEquals(parent, actual.getHolding());
  }

  @Test
  void shouldCalculateWeightWithinSameLeaves_whenVerifyToUserScale() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var commonHolding = mock(CommonHolding.class);
      final var sameLeaves = List.of(commonHolding);
      final var parentHolding = mock(PortfolioHolding.class);
      final var expected = TEN;

      when(commonHolding.getHolding()).thenReturn(parentHolding);
      when(sameLeaves.get(0).getHolding()).thenReturn(parentHolding);
      when(commonHolding.getWeight()).thenReturn(expected);

      doCallRealMethod().when(service).calculateWeightWithinSameLeaves(anyList(), any());
      // ACT
      final BigDecimal actual = service.calculateWeightWithinSameLeaves(sameLeaves, parentHolding);

      // VERIFY
      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(TEN));
    }
  }

  @Test
  void shouldCalculateWeightWithinSameLeaves_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, defaultPeriods));

    final var commonHolding = mock(CommonHolding.class);
    final var sameLeaves = List.of(commonHolding);
    final var parentHolding = mock(PortfolioHolding.class);
    final var expected = TEN;

    when(commonHolding.getHolding()).thenReturn(parentHolding);
    when(sameLeaves.get(0).getHolding()).thenReturn(parentHolding);
    when(commonHolding.getWeight()).thenReturn(expected);

    doCallRealMethod().when(service).calculateWeightWithinSameLeaves(anyList(), any());
    // ACT
    final BigDecimal actual = service.calculateWeightWithinSameLeaves(sameLeaves, parentHolding);

    // VERIFY
    assertEquals(expected.doubleValue(), actual.doubleValue());
  }

  @Test
  void shouldMapToFinalResult_whenVerifyBuildResult() {
    try (var mockedHoldingsKeyResult = Mockito.mockStatic(HoldingsKeyResult.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var holdingAggregator = mock(HoldingAggregator.class);
      final var commonHolding = mock(CommonHolding.class);
      final var leaves = Map.of(holdingAggregator, List.of(commonHolding));
      final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregator, TEN);

      final var holdingsKey = mock(HoldingsKeyResult.class);
      final var holding = mock(PortfolioHolding.class);

      when(commonHolding.getHolding()).thenReturn(holding);
      when(commonHolding.getWeight()).thenReturn(TEN);
      when(service.calculateWeightWithinSameLeaves(anyList(), any())).thenReturn(TEN);
      mockedHoldingsKeyResult.when(() -> HoldingsKeyResult.buildFromHolding(any(), any())).thenReturn(holdingsKey);

      doCallRealMethod().when(service).mapToFinalResult(anyMap(), any());
      // ACT
      service.mapToFinalResult(leaves, sortedLeafEntry);

      // VERIFY
      mockedHoldingsKeyResult.verify(() -> HoldingsKeyResult.buildFromHolding(holding, TEN));
    }
  }

  @Test
  void shouldMapToFinalResult_whenVerifyCalculateWeightWithinSameLeaves() {
    try (var mockedHoldingsKeyResult = Mockito.mockStatic(HoldingsKeyResult.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var holdingAggregator = mock(HoldingAggregator.class);
      final var commonHolding = mock(CommonHolding.class);
      final var leaves = Map.of(holdingAggregator, List.of(commonHolding));
      final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregator, TEN);

      final var holdingsKey = mock(HoldingsKeyResult.class);
      final var holding = mock(PortfolioHolding.class);

      when(commonHolding.getHolding()).thenReturn(holding);
      when(commonHolding.getWeight()).thenReturn(TEN);
      mockedHoldingsKeyResult.when(() -> HoldingsKeyResult.buildFromHolding(any(), any())).thenReturn(holdingsKey);

      doCallRealMethod().when(service).mapToFinalResult(anyMap(), any());
      // ACT
      service.mapToFinalResult(leaves, sortedLeafEntry);

      // VERIFY
      verify(service).calculateWeightWithinSameLeaves(List.of(commonHolding), holding);
    }
  }

  @Test
  void shouldMapToFinalResult_whenCheckResult() {
    try (var mockedHoldingsKeyResult = Mockito.mockStatic(HoldingsKeyResult.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var holdingAggregator = new HoldingAggregator("Tesla", null, null);
      final var commonHolding = mock(CommonHolding.class);
      final var leaves = Map.of(holdingAggregator, List.of(commonHolding));
      final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregator, TEN);

      final var holdingsKey = mock(HoldingsKeyResult.class);
      final var holding = mock(PortfolioHolding.class);
      final var expected = new TopCommonHoldingData("Tesla", null, null, null, toUserScale(TEN), 1, Set.of(
          holdingsKey));

      when(commonHolding.getHolding()).thenReturn(holding);
      when(commonHolding.getWeight()).thenReturn(TEN);
      when(service.calculateWeightWithinSameLeaves(anyList(), any())).thenReturn(TEN);
      mockedHoldingsKeyResult.when(() -> HoldingsKeyResult.buildFromHolding(any(), any())).thenReturn(holdingsKey);

      doCallRealMethod().when(service).mapToFinalResult(anyMap(), any());
      // ACT
      final TopCommonHoldingData actual = service.mapToFinalResult(leaves, sortedLeafEntry);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldMapToFinalResult_whenCheckResult2() {
    try (var mockedHoldingsKeyResult = Mockito.mockStatic(HoldingsKeyResult.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var holdingAggregator = new HoldingAggregator("Tesla", null, null);
      final var commonHolding = mock(CommonHolding.class);
      final var leaves = Map.of(holdingAggregator, List.of(commonHolding));
      final var sortedLeafEntry = new AbstractMap.SimpleEntry<>(holdingAggregator, TEN);

      final var holdingsKey = mock(HoldingsKeyResult.class);
      final var holding = mock(PortfolioHolding.class);
      final var expected = new TopCommonHoldingData("Tesla", "H", null, null, toUserScale(TEN), 1, Set.of(
          holdingsKey));

      when(commonHolding.getTicker()).thenReturn("H");
      when(commonHolding.getHolding()).thenReturn(holding);
      when(commonHolding.getWeight()).thenReturn(TEN);
      when(service.calculateWeightWithinSameLeaves(anyList(), any())).thenReturn(TEN);
      mockedHoldingsKeyResult.when(() -> HoldingsKeyResult.buildFromHolding(any(), any())).thenReturn(holdingsKey);

      doCallRealMethod().when(service).mapToFinalResult(anyMap(), any());
      // ACT
      final TopCommonHoldingData actual = service.mapToFinalResult(leaves, sortedLeafEntry);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldCalculateTopCommonHoldings_whenVerifyFirstLevelLeaves() {
    // SETUP
    final var accumulativeTypes = Set.of("E");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulativeTypes));

    final var holding = mock(PortfolioHolding.class);
    final var commonHolding = mock(CommonHolding.class);
    final var holdings = Map.of(holding, List.of(commonHolding));
    final var allocations = Map.of(holding, TEN);

    doCallRealMethod().when(service).calculateTopCommonHoldings(anyMap(), anyMap(), anySet());
    // ACT
    service.calculateTopCommonHoldings(holdings, allocations, accumulativeTypes);

    // VERIFY
    verify(service).firstLevelLeaves(allocations, holding, List.of(commonHolding));
  }

  @Test
  void shouldCalculateTopCommonHoldings_whenVerifySecondLevelLeaves() {
    // SETUP
    final var accumulativeTypes = Set.of("E");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulativeTypes));

    final var holding = mock(PortfolioHolding.class);
    final var commonHolding = mock(CommonHolding.class);
    final var holdings = Map.of(holding, List.of(commonHolding));
    final var allocations = Map.of(holding, TEN);

    when(service.firstLevelLeaves(anyMap(), any(), anyList())).thenReturn(Stream.of(commonHolding));

    doCallRealMethod().when(service).calculateTopCommonHoldings(anyMap(), anyMap(), anySet());
    // ACT
    service.calculateTopCommonHoldings(holdings, allocations, accumulativeTypes);

    // VERIFY
    verify(service).secondLevelLeaves(commonHolding);
  }

  @Test
  void shouldFirstLevelLeaves_whenVerifySetParentAndCalculateWeight() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var defaultPeriods = Set.of();
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var allocations = Map.of(mock(PortfolioHolding.class), TEN);
    final var parent = mock(PortfolioHolding.class);
    final var child = mock(CommonHolding.class);
    final var firstLevelChildren = List.of(child);

    when(child.getType()).thenReturn("E");
    when(child.getUnderlyingHoldings()).thenReturn(null);
    when(service.setParentAndCalculateWeight(anyMap(), any(), any())).thenReturn(child);

    doCallRealMethod().when(service).firstLevelLeaves(anyMap(), any(), anyList());
    // ACT
    final Stream<CommonHolding> actual = service.firstLevelLeaves(allocations, parent, firstLevelChildren);

    // VERIFY
    assertEquals(1, actual.toList().size());
    verify(service).setParentAndCalculateWeight(allocations, parent, child);
  }

  @Test
  void shouldFirstLevelLeaves_whenCheckResult() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var allocations = Map.of(mock(PortfolioHolding.class), TEN);
    final var parent = mock(PortfolioHolding.class);
    final var child = mock(CommonHolding.class);
    final var firstLevelChildren = List.of(child);
    final var expected = new CommonHolding("Apple Inc", null, TEN, null, null);

    when(child.getType()).thenReturn("E");
    when(child.getCompanyName()).thenReturn("Apple Inc");
    when(child.getValue()).thenReturn(TEN);
    when(child.getUnderlyingHoldings()).thenReturn(null);
    when(service.setParentAndCalculateWeight(anyMap(), any(), any())).thenReturn(child);

    doCallRealMethod().when(service).firstLevelLeaves(anyMap(), any(), anyList());
    // ACT
    final Stream<CommonHolding> actual = service.firstLevelLeaves(allocations, parent, firstLevelChildren);

    // VERIFY
    assertEquals(expected.getWeight(), actual.findFirst().orElseThrow().getWeight());
  }

  @Test
  void shouldSetParentAndCalculateWeightSecondLvlLeaf_whenCheckResult() {
    try (var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      // SETUP
      final var accumulateTypes = Set.of("FE");
      final var fetcher = mock(SecurityDataFetcher.class);
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, accumulateTypes));

      final var firstLvlParent = mock(CommonHolding.class);
      final var child = mock(CommonHolding.class);

      when(child.getValue()).thenReturn(TEN);
      when(child.getWeight()).thenReturn(HUNDRED);
      when(firstLvlParent.getWeight()).thenReturn(TEN);
      mockedDecimalUtils.when(() -> DecimalUtils.toUserScale(TEN)).thenReturn(TEN);
      doCallRealMethod().when(service).setParentAndCalculateWeightSecondLvlLeaf(any(), any());
      // ACT
      final CommonHolding actual = service.setParentAndCalculateWeightSecondLvlLeaf(firstLvlParent, child);

      // VERIFY
      assertEquals(HUNDRED, actual.getWeight());
    }
  }

  @Test
  void shouldIsLeafStock_whenCheckResult() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var parent = mock(PortfolioHolding.class);
    final var child = mock(CommonHolding.class);

    when(parent.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_CANADA);
    when(child.getCompanyName()).thenReturn("test");
    when(child.getType()).thenReturn("E");

    doCallRealMethod().when(service).isLeafStock(any(), any());
    // ACT
    final boolean actual = service.isLeafStock(parent, child);

    // VERIFY
    assertTrue(actual);
  }

  @Test
  void shouldIsLeafStock_whenCheckResult2() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final var parent = mock(PortfolioHolding.class);
    final var child = mock(CommonHolding.class);

    when(parent.getHoldingType()).thenReturn(FinancialInstrumentType.STOCK_CANADA);
    when(child.getCompanyName()).thenReturn("test");
    when(child.getType()).thenReturn("FE");

    doCallRealMethod().when(service).isLeafStock(any(), any());
    // ACT
    final boolean actual = service.isLeafStock(parent, child);

    // VERIFY
    assertFalse(actual);
  }

  @Test
  void shouldGetTopCommonHoldingsNumber_whenReturnDefault10WhenGetNumOfTopCommonHoldingsIsNull() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final TopCommonHoldingsCommand req = mock(TopCommonHoldingsCommand.class);
    doReturn(null).when(req).getNumOfTopCommonHoldings();

    doCallRealMethod().when(service).getTopCommonHoldingsNumber(req);
    // ACT
    final int actual = service.getTopCommonHoldingsNumber(req);

    // VERIFY
    final int expected = 10;
    assertEquals(expected, actual);
  }

  @Test
  void shouldGetTopCommonHoldingsNumber_whenReturnProvidedNumberIfNotNull() {
    // SETUP
    final var accumulateTypes = Set.of("FE");
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
        .useConstructor(fetcher, accumulateTypes));

    final TopCommonHoldingsCommand req = mock(TopCommonHoldingsCommand.class);
    doReturn(11).when(req).getNumOfTopCommonHoldings();

    doCallRealMethod().when(service).getTopCommonHoldingsNumber(req);
    // ACT
    final int actual = service.getTopCommonHoldingsNumber(req);

    // VERIFY
    final int expected = 11;
    assertEquals(expected, actual);
  }

  @Test
  void shouldPerform_whenVerifyGetTopCommonHoldingsNumber() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var defaultPeriods = Set.of();
      final var service = mock(CommonHoldingsServiceImpl.class, withSettings()
          .useConstructor(fetcher, defaultPeriods));

      final var command = mock(TopCommonHoldingsCommand.class);
      final var holdings = List.of(mock(PortfolioHolding.class));

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(Map.of());
      when(command.getHoldings()).thenReturn(holdings);
      when(fetcher.fetch(any(), any())).thenReturn(Map.of());

      doCallRealMethod().when(service).perform(any());
      // ACT
      service.perform(command);

      // VERIFY
      verify(service).getTopCommonHoldingsNumber(command);
    }
  }
}