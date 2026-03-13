package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.PortfolioUtils;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static java.math.BigDecimal.TEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EquityGeographicExposureCalculationServiceImplTest {

  @Test
  void shouldGetLoadFromCacheStorage_whenCheckResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var sut = mock(EquityGeographicExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(cacheStorage));

      final var holding = mock(Holding.class);
      final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));

      when(cacheStorage.load(any(), any(), any(), any())).thenReturn(exposures);
      doCallRealMethod().when(sut).fetchExposures(any(), any());
      // ACT
      final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

      // VERIFY
      Assertions.assertEquals(exposures, actual);
    }
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {

      // SETUP
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var sut = mock(EquityGeographicExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(cacheStorage));

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

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var sut = mock(EquityGeographicExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(cacheStorage));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(sut.calculateNetProducts(exposures, holdings, GeographicRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      mockedCalculationUtils.verify(() -> CalculationUtils.reScaleAbs(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyToUserScale() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class);
        var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class);
        var mockedDecimalUtils = Mockito.mockStatic(DecimalUtils.class)) {
      // SETUP
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var sut = mock(EquityGeographicExposureCalculationServiceImpl.class, withSettings()
          .useConstructor(cacheStorage));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(GeographicRegionType.CANADA, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      mockedCalculationUtils.when(() -> CalculationUtils.reScaleAbs(any())).thenReturn(netProducts);

      when(sut.calculateNetProducts(exposures, holdings, GeographicRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      mockedDecimalUtils.verify(() -> DecimalUtils.toUserScale(netProducts));
    }
  }

}
