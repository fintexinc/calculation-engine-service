package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.FixedIncomeSectorResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.util.PortfolioUtils;
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

@SuppressWarnings("unchecked")
class FixedIncomeBondSectorCalculationServiceImplTest {

  @Test
  void shouldCalculate_whenVerifyAreAllValuesZerosInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);

      final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
          .useConstructor(fixedIncomeFetcher, assetAllocationFetcher, assetAllocationDataMapper));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, List.of(), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);

      final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
          .useConstructor(fixedIncomeFetcher, assetAllocationFetcher, assetAllocationDataMapper));

      final var exposures = mock(Map.class);
      final var expected = new FixedIncomeSectorResult();
      expected.setFixedIncomeSector(FixedIncomeBondSectorCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final var actual = sut.calculate(exposures, List.of(), List.of());

      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldFetch_whenCheckResult() {
    final var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);

    final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
        .useConstructor(fixedIncomeFetcher, assetAllocationFetcher, assetAllocationDataMapper));

    final var holding = mock(Holding.class);
    final var rawData = new FixedIncomeBondSecurities();
    rawData.setFixedIncomeBondSectors(Map.of("CORPORATE_BONDS", TEN));

    when(fixedIncomeFetcher.fetch(any(), any())).thenReturn(Map.of(holding, rawData));
    doCallRealMethod().when(sut).fetchExposures(any(), any());
    final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), new java.util.ArrayList<>());

    assertEquals(1, actual.size());
  }

  @Test
  void shouldCalculate_whenVerifyResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final Holding fundSeriesHolding = mock(Holding.class);

      final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
          .useConstructor(fixedIncomeFetcher, assetAllocationFetcher, assetAllocationDataMapper));

      final var exposures = mock(Map.class);

      final HoldingAssetAllocation assetAllocation = mock(HoldingAssetAllocation.class);
      Mockito.when(assetAllocationFetcher.fetch(any(), any()))
          .thenReturn(Map.of(fundSeriesHolding, assetAllocation));
      Mockito.when(assetAllocationDataMapper.toRegionExposures(any()))
          .thenReturn(Map.of(
              fundSeriesHolding,
              Map.of(
                  AssetAllocationRegion.FIXED_INCOME, BigDecimal.ONE,
                  AssetAllocationRegion.CASH, BigDecimal.ONE)));

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final FixedIncomeSectorResult result = sut.calculate(exposures, List.of(fundSeriesHolding), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

}
