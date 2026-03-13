package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.EquitySectorResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.EquitySectorResult;
import com.fintex.ce.port.output.HoldingDataLoader;
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

class EquitySectorCalculationImplTest {

  @Test
  void shouldGetLoadFromCacheStorage_whenCheckResult() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var sut = mock(EquitySectorCalculationImpl.class, withSettings()
          .useConstructor(cacheStorage, responseMapper));

      final var holding = mock(Holding.class);
      final var exposures = Map.of(holding, Map.of(EquitySectorAllocationType.CONSUMER_DEFENSIVE, TEN));

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
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var sut = mock(EquitySectorCalculationImpl.class, withSettings()
          .useConstructor(cacheStorage, responseMapper));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquitySectorAllocationType.CONSUMER_DEFENSIVE, TEN));

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      verify(sut).calculateNetProducts(exposures, holdings, EquitySectorAllocationType.values());
    }
  }

  @Test
  void shouldCalculate_whenVerifyResponseMapperFromNetProducts() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var sut = mock(EquitySectorCalculationImpl.class, withSettings()
          .useConstructor(cacheStorage, responseMapper));

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(EquitySectorAllocationType.CONSUMER_DEFENSIVE, TEN));
      final var netProducts = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(false);
      when(sut.calculateNetProducts(exposures, holdings, EquitySectorAllocationType.values())).thenReturn(netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      verify(responseMapper).fromNetProducts(any(), any());
    }
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var sut = mock(EquitySectorCalculationImpl.class, withSettings()
          .useConstructor(cacheStorage, responseMapper));

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
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(EquitySectorResponseMapper.class);
      final var sut = mock(EquitySectorCalculationImpl.class, withSettings()
          .useConstructor(cacheStorage, responseMapper));

      final var exposures = mock(Map.class);
      final var expected = new EquitySectorResult();
      expected.setEquitySector(Map.of());
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final var actual = sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      Assertions.assertEquals(expected, actual);
      verify(responseMapper).toEmptyResponse(any());
    }
  }

}