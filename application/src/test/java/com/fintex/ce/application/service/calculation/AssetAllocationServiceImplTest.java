package com.fintex.ce.application.service.calculation;

import com.fintex.ce.adapter.cache.AssetAllocationCacheStorage;
import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.application.mapper.response.AssetAllocationResponseMapper;
import com.fintex.ce.application.service.calculation.AssetAllocationServiceImpl;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.application.result.AssetAllocationResult;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.data.DataProviderChecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.domain.enumeration.DataProvider.DEFAULT_PROVIDERS;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AssetAllocationServiceImplTest {

  @Test
  void getLoadFromCacheStorage_checkResult() {
    // SETUP
    final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var dataProviderChecker = mock(DataProviderChecker.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker,
        responseMapper));
    final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
    final var expected = mock(Map.class);
    when(assetAllocationDataMapper.mapForAA(assetAllocationDataDto)).thenReturn(expected);

    when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(
        assetAllocationDataDto);
    doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
    // ACT
    final var actual = sut.getLoadFromCacheStorage(mock(PortfolioHoldingsCommand.class), List.of());

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void getLoadFromCacheStorage_verifyMapForAA() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
      final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var dataProviderChecker = mock(DataProviderChecker.class);
      final var responseMapper = mock(AssetAllocationResponseMapper.class);

      final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
          assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker,
          responseMapper));
      final var req = mock(PortfolioHoldingsCommand.class);
      final List<Warning> warnings = List.of();
      final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
      when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(
          assetAllocationDataDto);

      doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
      // ACT
      sut.getLoadFromCacheStorage(req, warnings);

      // VERIFY
      verify(assetAllocationDataMapper).mapForAA(assetAllocationDataDto);
    }
  }

  @Test
  void getLoadFromCacheStorage_verifyValidate() {
    // SETUP
    final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var dataProviderChecker = mock(DataProviderChecker.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker,
        responseMapper));

    final var req = mock(PortfolioHoldingsCommand.class);
    final List<Warning> warnings = List.of();
    final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
    when(assetAllocationCacheStorage.loadWithDataProvidesCheck(anyList(), anyList(), anyList())).thenReturn(
        assetAllocationDataDto);

    doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
    // ACT
    sut.getLoadFromCacheStorage(req, warnings);

    // VERIFY
    verify(assetAllocationDataValidator).validate(assetAllocationDataDto, warnings);
  }

  @Test
  void getLoadFromCacheStorage_verifyDtaProviderCheckerCheck() {
    // SETUP
    final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var dataProviderChecker = mock(DataProviderChecker.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker,
        responseMapper));

    final var req = mock(PortfolioHoldingsCommand.class);
    final var assetAllocationDataDto = mock(AssetAllocationDataDTO.class);
    when(assetAllocationCacheStorage.loadWithDataProvidesCheck(any(), any(), any())).thenReturn(assetAllocationDataDto);

    doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
    // ACT
    sut.getLoadFromCacheStorage(req, List.of());

    // VERIFY
    verify(dataProviderChecker).check(any(), eq(assetAllocationDataDto));
  }

  @Test
  void getLoadFromCacheStorage_verifyGetSpecifiedIfEmpty() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
      final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
      final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final var dataProviderChecker = mock(DataProviderChecker.class);
      final var responseMapper = mock(AssetAllocationResponseMapper.class);

      final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
          assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker,
          responseMapper));

      final var reqDTO = mock(PortfolioHoldingsCommand.class);
      final var holdings = mock(List.class);
      final List<Warning> warnings = List.of();
      final var providers = mock(List.class);

      when(reqDTO.getHoldings()).thenReturn(holdings);
      when(reqDTO.getDataProviders()).thenReturn(providers);

      doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
      // ACT
      sut.getLoadFromCacheStorage(reqDTO, warnings);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, DEFAULT_PROVIDERS), Mockito.times(2));
    }
  }

  @Test
  void getLoadFromCacheStorage_verifyLoadWithDataProvidesCheck1() {
    // SETUP
    final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var dataProviderChecker = mock(DataProviderChecker.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker,
        responseMapper));

    final var reqDTO = mock(PortfolioHoldingsCommand.class);
    final var holdings = mock(List.class);
    final List<Warning> warnings = List.of();

    when(reqDTO.getHoldings()).thenReturn(holdings);

    doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
    // ACT
    sut.getLoadFromCacheStorage(reqDTO, warnings);

    // VERIFY
    verify(assetAllocationCacheStorage).loadWithDataProvidesCheck(eq(holdings), any(), eq(warnings));
  }

  @Test
  void calculate_verifyCalculateNetProducts() {
    // SETUP
    final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var dataProviderChecker = mock(DataProviderChecker.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker,
        responseMapper));

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
    final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var dataProviderChecker = mock(DataProviderChecker.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker,
        responseMapper));

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
    final var assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
    final var assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
    final var assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
    final var dataProviderChecker = mock(DataProviderChecker.class);
    final var responseMapper = mock(AssetAllocationResponseMapper.class);

    final var sut = mock(AssetAllocationServiceImpl.class, withSettings().useConstructor(
        assetAllocationCacheStorage, assetAllocationDataValidator, assetAllocationDataMapper, dataProviderChecker,
        responseMapper));

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
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }

}
