package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.application.mapping.response.AssetAllocationResponseMapper;
import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.AssetAllocationResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ExposureDataHolder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AssetAllocationServiceImplTest {

  @Test
  void shouldGetLoadFromCacheStorage_whenCheckResult() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataFetcher.class);

    final var service = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var holding = mock(Holding.class);
    final var allocations = Map.of(holding, mock(HoldingAssetAllocation.class));
    final var expected = Map.of(holding, Map.of(AssetAllocationRegion.OTHER, TEN));

    when(securityDataPort.fetch(anyList(), anyList())).thenReturn(allocations);
    when(assetAllocationDataMapper.toRegionExposures(allocations)).thenReturn(expected);
    doCallRealMethod().when(service).fetchExposures(any());

    // ACT
    final var result = service.fetchExposures(mock(PortfolioHoldingsCommand.class));
    final var actual = result.allocations();

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void fetchExposures_verifyMapFromAllocations() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataFetcher.class);

    final var service = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var allocations = Map.of(mock(Holding.class), mock(HoldingAssetAllocation.class));
    when(securityDataPort.fetch(anyList(), anyList())).thenReturn(allocations);

    doCallRealMethod().when(service).fetchExposures(any());

    // ACT
    service.fetchExposures(mock(PortfolioHoldingsCommand.class));

    // VERIFY
    verify(assetAllocationDataMapper).toRegionExposures(allocations);
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataFetcher.class);

    final var service = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(AssetAllocationRegion.OTHER, TEN));
    doCallRealMethod().when(service).calculate(any(), any());

    // ACT
    service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

    // VERIFY
    verify(service).calculateNetProducts(exposures, holdings, AssetAllocationRegion.values());
  }

  @Test
  void shouldCalculate_whenVerifyFromNetProducts() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataFetcher.class);

    final var service = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final Map netProducts = mock(Map.class);
    final List<Warning> warnings = List.of();
    final var exposures = Map.of(holding, Map.of(AssetAllocationRegion.OTHER, TEN));
    when(service.calculateNetProducts(any(), any(), any())).thenReturn(netProducts);
    doCallRealMethod().when(service).calculate(any(), any());

    // ACT
    service.calculate(new ExposureDataHolder<>(exposures, warnings), holdings);

    // VERIFY
    verify(responseMapper).fromNetProducts(any(), any());
  }

  @Test
  void shouldCalculate_whenCheckResult() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataFetcher.class);

    final var service = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(AssetAllocationRegion.FIXED_INCOME, TEN));
    final var netProducts = mock(Map.class);
    final var expected = mock(AssetAllocationResult.class);
    final List<Warning> warnings = List.of();

    when(service.calculateNetProducts(exposures, holdings, AssetAllocationRegion.values())).thenReturn(netProducts);
    when(responseMapper.fromNetProducts(any(), any())).thenReturn(expected);

    doCallRealMethod().when(service).calculate(any(), any());

    // ACT
    final var actual = service.calculate(new ExposureDataHolder<>(exposures, warnings), holdings);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void shouldCalculateAssetAllocationResponse_whenCheckResult() {
    // SETUP
    final AssetAllocationServiceImpl m = mock(AssetAllocationServiceImpl.class);
    final Map<AssetAllocationRegionType, BigDecimal> expected = Stream.of(AssetAllocationRegionType.values())
        .filter(type -> !AssetAllocationRegionType.INTERNATIONAL_EQUITY.equals(type))
        .collect(Collectors.toMap(k -> k, v -> TEN));
    expected.put(AssetAllocationRegionType.INTERNATIONAL_EQUITY, BigDecimal.valueOf(40));
    expected.put(AssetAllocationRegionType.OTHER, BigDecimal.valueOf(20));
    doCallRealMethod().when(m).calculateAssetAllocationResponse(anyMap());

    // ACT
    final Map<AssetAllocationRegion, BigDecimal> allocations = Stream.of(AssetAllocationRegion.values())
        .collect(Collectors.toMap(k -> k, v -> TEN));
    final Map<AssetAllocationRegionType, BigDecimal> actual = m.calculateAssetAllocationResponse(allocations);

    // VERIFY
    assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }
}
