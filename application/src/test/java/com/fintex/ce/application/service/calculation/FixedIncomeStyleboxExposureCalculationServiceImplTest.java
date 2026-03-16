package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.FixedIncomeStyleboxExposureResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.FixedIncomeStyleboxType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeStyleboxExposureCalculationServiceImplTest {

  @Test
  void fetchExposures_checkResult() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
    final var sut = mock(FixedIncomeStyleboxExposureCalculationServiceImpl.class, withSettings()
        .useConstructor( cacheStorage, responseMapper));

    final var holding = mock(Holding.class);
    final var exposures = Map.of(holding, Map.of(FixedIncomeStyleboxType.HIGH_EXTENSIVE, BigDecimal.TEN));

    when(cacheStorage.load(any(), any(), any(), any())).thenReturn(exposures);
    doCallRealMethod().when(sut).fetchExposures(any(), any());
    // ACT
    final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

    // VERIFY
    Assertions.assertEquals(exposures, actual);
  }

  @Test
  void calculate_verifyCalculateNetProducts() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
    final var sut = mock(FixedIncomeStyleboxExposureCalculationServiceImpl.class, withSettings()
        .useConstructor( cacheStorage, responseMapper));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(FixedIncomeStyleboxType.HIGH_EXTENSIVE, BigDecimal.TEN));

    doCallRealMethod().when(sut).calculate(any(), any(), any());
    // ACT
    sut.calculate(exposures, holdings, List.of());

    // VERIFY
    verify(sut).calculateNetProducts(exposures, holdings, FixedIncomeStyleboxType.values());
  }

  @Test
  void calculate_verifyResponseMapperFromNetProducts() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
    final var sut = mock(FixedIncomeStyleboxExposureCalculationServiceImpl.class, withSettings()
        .useConstructor( cacheStorage, responseMapper));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(FixedIncomeStyleboxType.HIGH_EXTENSIVE, BigDecimal.TEN));
    final var netProducts = mock(Map.class);
    when(sut.calculateNetProducts(exposures, holdings, FixedIncomeStyleboxType.values())).thenReturn(netProducts);

    doCallRealMethod().when(sut).calculate(any(), any(), any());
    // ACT
    sut.calculate(exposures, holdings, List.of());

    // VERIFY
    verify(responseMapper).fromNetProducts(any(), any());
  }

  @Test
  void calculate_verifyAreAllValuesEmptyInMapOfExposure() {
    // SETUP
    try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
      final var sut = mock(FixedIncomeStyleboxExposureCalculationServiceImpl.class, withSettings()
          .useConstructor( cacheStorage, responseMapper));
      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void calculate_checkResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var cacheStorage = mock(HoldingDataLoader.class);
      final var responseMapper = mock(FixedIncomeStyleboxExposureResponseMapper.class);
      final var sut = mock(FixedIncomeStyleboxExposureCalculationServiceImpl.class, withSettings()
          .useConstructor( cacheStorage, responseMapper));

      final var exposures = mock(Map.class);
      final var expected = new FixedIncomeStyleboxExposureResult();
      expected.setFixedIncomeStyleboxExposure(Map.of());
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