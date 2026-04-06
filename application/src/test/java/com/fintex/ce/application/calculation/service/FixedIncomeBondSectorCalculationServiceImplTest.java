package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.sm.model.domain.enumeration.FixedIncomeSecuritiesAllocationType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.FixedIncomeSectorResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.ce.util.PortfolioUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

      final var service = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
          .useConstructor(fixedIncomeFetcher, assetAllocationFetcher, assetAllocationDataMapper));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculate(any(), any());
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);

      final var service = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
          .useConstructor(fixedIncomeFetcher, assetAllocationFetcher, assetAllocationDataMapper));

      final var exposures = mock(Map.class);
      final var expected = new FixedIncomeSectorResult();
      expected.setFixedIncomeSector(FixedIncomeBondSectorCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);

      doCallRealMethod().when(service).calculate(any(), any());
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldFetch_whenCheckResult() {
    final var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
    final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
    final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);

    final var service = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
        .useConstructor(fixedIncomeFetcher, assetAllocationFetcher, assetAllocationDataMapper));

    final var holding = mock(Holding.class);
    final var rawData = new FixedIncomeBondSecurities();
    rawData.setFixedIncomeBondSectors(Map.of(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, TEN));

    when(fixedIncomeFetcher.fetch(any(), any())).thenReturn(Map.of(holding, rawData));
    doCallRealMethod().when(service).fetchExposures(any());
    final var result = service.fetchExposures(mock(PortfolioHoldingsCommand.class));
    final var actual = result.allocations();

    assertEquals(1, actual.size());
  }

  @Test
  void shouldCalculate_whenVerifyResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fixedIncomeFetcher = mock(SecurityDataFetcher.class);
      final var assetAllocationFetcher = mock(SecurityDataFetcher.class);
      final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
      final Holding fundSeriesHolding = mock(Holding.class);

      final var service = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
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

      doCallRealMethod().when(service).calculate(any(), any());
      final FixedIncomeSectorResult result = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of(fundSeriesHolding));

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

}
