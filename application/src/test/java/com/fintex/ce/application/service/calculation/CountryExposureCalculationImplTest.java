package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.CountryExposureResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.CountryExposureResult;
import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.util.PortfolioUtils;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class CountryExposureCalculationImplTest {

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var sut = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper));

      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesInMapEmpty(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var sut = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper));

      final var exposures = mock(Map.class);
      final var expected = new CountryExposureResult();
      expected.setCountryExposure(CountryExposureCalculationImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final var actual = sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldGetLoadFromCacheStorage_whenCheckResult() {
    // SETUP
    final var storage = mock(HoldingDataLoader.class);
    final var responseMapper = mock(CountryExposureResponseMapper.class);
    final var sut = mock(CountryExposureCalculationImpl.class,
        withSettings().useConstructor(storage, responseMapper));

    final var holding = mock(Holding.class);
    final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));

    when(storage.load(any(), any(), any(), any())).thenReturn(exposures);
    doCallRealMethod().when(sut).fetchExposures(any(), any());
    // ACT
    final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

    // VERIFY
    assertEquals(exposures, actual);
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var sut = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(false);
      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      verify(sut).calculateNetProducts(exposures, holdings, CountryRegionType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var sut = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(false);
      when(sut.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      verify(responseMapper).fromNetProducts(any(), any());
    }
  }

  @Test
  void shouldCalculate_whenVerifyResponseMapperFromNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var storage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(CountryExposureResponseMapper.class);
      final var sut = mock(CountryExposureCalculationImpl.class,
          withSettings().useConstructor(storage, responseMapper));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(CountryRegionType.INTERNATIONAL_DEVELOPED, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesInMapEmpty(any())).thenReturn(false);
      when(sut.calculateNetProducts(exposures, holdings, CountryRegionType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      verify(responseMapper).fromNetProducts(any(), any());
    }
  }

}