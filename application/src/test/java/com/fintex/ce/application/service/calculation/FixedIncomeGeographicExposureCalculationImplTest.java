package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.service.calculation.FixedIncomeGeographicExposureCalculationImpl;
import com.fintex.ce.domain.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.application.result.GeographicExposureResult;
import com.fintex.ce.adapter.cache.FixedIncomeGeographicExposureCacheStorage;
import com.fintex.ce.util.PortfolioUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeGeographicExposureCalculationImplTest {

  @Test
  void calculate_verifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(FixedIncomeGeographicExposureCacheStorage.class);
      final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
          withSettings().useConstructor(storage));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(exposures));
    }
  }

  @Test
  void calculate_checkResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(FixedIncomeGeographicExposureCacheStorage.class);
      final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
          withSettings().useConstructor(storage));

      final var exposures = mock(Map.class);
      final var expected = new GeographicExposureResult();
      expected.setEquityGeographicExposure(FixedIncomeGeographicExposureCalculationImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(true);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final var actual = sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void getLoadFromCacheStorage_checkResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(FixedIncomeGeographicExposureCacheStorage.class);
      final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
          withSettings().useConstructor(storage));

      final var holding = mock(Holding.class);
      final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));

      when(storage.load(any(), any(), any(), any())).thenReturn(exposures);
      doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
      // ACT
      final var actual = sut.getLoadFromCacheStorage(mock(PortfolioHoldingsCommand.class), List.of());

      // VERIFY
      assertEquals(exposures, actual);
    }
  }

  @Test
  void calculate_verifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(FixedIncomeGeographicExposureCacheStorage.class);
      final var sut = mock(FixedIncomeGeographicExposureCalculationImpl.class,
          withSettings().useConstructor(storage));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      verify(sut).calculateNetProducts(exposures, holdings, GeographicRegionType.values());
    }
  }

}