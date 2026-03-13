package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.FixedIncomeSectorType;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.FixedIncomeSectorResult;
import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.port.output.cache.AssetAllocationCachePort;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeBondSectorCalculationServiceImplTest {

  @Test
  void shouldCalculate_whenVerifyAreAllValuesZerosInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fixedIncomeBondSectorCacheStorage = mock(HoldingDataLoader.class);
      final AssetAllocationCachePort assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
      final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final AssetAllocationDataValidator assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);

      final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
          .useConstructor(fixedIncomeBondSectorCacheStorage, assetAllocationCacheStorage,
              assetAllocationDataMapper, assetAllocationDataValidator));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fixedIncomeBondSectorCacheStorage = mock(HoldingDataLoader.class);
      final AssetAllocationCachePort assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
      final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final AssetAllocationDataValidator assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);

      final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
          .useConstructor(fixedIncomeBondSectorCacheStorage, assetAllocationCacheStorage,
              assetAllocationDataMapper, assetAllocationDataValidator));

      final var exposures = mock(Map.class);
      final var expected = new FixedIncomeSectorResult();
      expected.setFixedIncomeSector(FixedIncomeBondSectorCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final var actual = sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldGetLoadFromCacheStorage_whenCheckResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fixedIncomeBondSectorCacheStorage = mock(HoldingDataLoader.class);
      final AssetAllocationCachePort assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
      final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final AssetAllocationDataValidator assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);

      final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
          .useConstructor(fixedIncomeBondSectorCacheStorage, assetAllocationCacheStorage,
              assetAllocationDataMapper, assetAllocationDataValidator));

      final var holding = mock(Holding.class);
      final var exposures = Map.of(holding, Map.of(FixedIncomeSectorType.CORPORATE_BONDS, TEN));

      when(fixedIncomeBondSectorCacheStorage.load(any(), any(), any(), any())).thenReturn(exposures);
      doCallRealMethod().when(sut).fetchExposures(any(), any());
      // ACT
      final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

      // VERIFY
      assertEquals(exposures, actual);
    }
  }

  @Test
  void shouldCalculate_whenVerifyResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fixedIncomeBondSectorCacheStorage = mock(HoldingDataLoader.class);
      final AssetAllocationCachePort assetAllocationCacheStorage = mock(AssetAllocationCachePort.class);
      final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final AssetAllocationDataValidator assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
      final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
      final AssetAllocationDataDTO assetAllocationDataDTO = mock(AssetAllocationDataDTO.class);

      final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
          .useConstructor(fixedIncomeBondSectorCacheStorage, assetAllocationCacheStorage,
              assetAllocationDataMapper, assetAllocationDataValidator));

      final var exposures = mock(Map.class);

      Mockito.when(assetAllocationCacheStorage.load(any(), any(), any(), any()))
          .thenReturn(assetAllocationDataDTO);
      Mockito.when(assetAllocationDataMapper.mapForAA(assetAllocationDataDTO))
          .thenReturn(Map.of(
              fundSeriesHolding,
              Map.of(
                  AssetAllocationRegion.FIXED_INCOME, BigDecimal.ONE,
                  AssetAllocationRegion.CASH, BigDecimal.ONE)));

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final FixedIncomeSectorResult result = sut.calculate(exposures, List.of(fundSeriesHolding), List.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

}