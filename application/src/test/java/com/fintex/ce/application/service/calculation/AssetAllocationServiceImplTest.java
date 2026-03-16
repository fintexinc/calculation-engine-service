package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.application.mapper.response.AssetAllocationResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.AssetAllocationResult;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.port.output.sm.dto.AssetAllocationDto;
import com.fintex.ce.util.ComparisonUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

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
  void fetchExposures_checkResult() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataPort.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var holding = mock(Holding.class);
    final var allocations = Map.of(holding, mock(AssetAllocationDto.class));
    final var expected = Map.of(holding, Map.of(AssetAllocationRegion.OTHER, TEN));

    when(securityDataPort.fetch(anyList(), anyList())).thenReturn(allocations);
    when(assetAllocationDataMapper.toRegionExposures(allocations)).thenReturn(expected);
    doCallRealMethod().when(sut).fetchExposures(any(), any());

    // ACT
    final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void fetchExposures_verifySecurityDataPortFetch() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataPort.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var reqDTO = mock(PortfolioHoldingsCommand.class);
    final var holdings = List.of(mock(Holding.class));
    when(reqDTO.getHoldings()).thenReturn(holdings);

    doCallRealMethod().when(sut).fetchExposures(any(), any());

    // ACT
    sut.fetchExposures(reqDTO, List.of());

    // VERIFY
    verify(securityDataPort).fetch(any(), anyList());
  }

  @Test
  void fetchExposures_verifyMapFromAllocations() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataPort.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var allocations = Map.of(mock(Holding.class), mock(AssetAllocationDto.class));
    when(securityDataPort.fetch(anyList(), anyList())).thenReturn(allocations);

    doCallRealMethod().when(sut).fetchExposures(any(), any());

    // ACT
    sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

    // VERIFY
    verify(assetAllocationDataMapper).toRegionExposures(allocations);
  }

  @Test
  void calculate_verifyCalculateNetProducts() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataPort.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(AssetAllocationRegion.OTHER, TEN));
    doCallRealMethod().when(sut).calculate(any(), any(), any());

    // ACT
    sut.calculate(exposures, holdings, List.of());

    // VERIFY
    verify(sut).calculateNetProducts(exposures, holdings, AssetAllocationRegion.values());
  }

  @Test
  void calculate_verifyFromNetProducts() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataPort.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final Map netProducts = mock(Map.class);
    final List<Warning> warnings = List.of();
    final var exposures = Map.of(holding, Map.of(AssetAllocationRegion.OTHER, TEN));
    when(sut.calculateNetProducts(any(), any(), any())).thenReturn(netProducts);
    doCallRealMethod().when(sut).calculate(any(), any(), any());

    // ACT
    sut.calculate(exposures, holdings, warnings);

    // VERIFY
    verify(responseMapper).fromNetProducts(any(), any());
  }

  @Test
  void calculate_checkResult() {
    // SETUP
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);
    final var securityDataPort = mock(SecurityDataPort.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationDataMapper, responseMapper, securityDataPort));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(AssetAllocationRegion.FIXED_INCOME, TEN));
    final var netProducts = mock(Map.class);
    final var expected = mock(AssetAllocationResult.class);
    final List<Warning> warnings = List.of();

    when(sut.calculateNetProducts(exposures, holdings, AssetAllocationRegion.values())).thenReturn(netProducts);
    when(responseMapper.fromNetProducts(any(), any())).thenReturn(expected);

    doCallRealMethod().when(sut).calculate(any(), any(), any());

    // ACT
    final var actual = sut.calculate(exposures, holdings, warnings);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void calculateAssetAllocationResponse_checkResult() {
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
