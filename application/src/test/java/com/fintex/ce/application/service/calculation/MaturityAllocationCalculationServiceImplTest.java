package com.fintex.ce.application.service.calculation;

import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.application.mapper.response.MaturityAllocationResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.MaturityAllocationType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.MaturityAllocationResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.util.PortfolioUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MaturityAllocationCalculationServiceImplTest {

  @Test
  void getLoadFromCacheStorage_checkResult() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor( cacheStorage, responseMapper));

    final var holding = mock(Holding.class);
    final var exposures = Map.of(holding, Map.of(MaturityAllocationType.FIVE_TO_SEVEN_YEARS, BigDecimal.TEN));

    when(cacheStorage.load(any(), any(), any(), any())).thenReturn(exposures);
    doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
    // ACT
    final var actual = sut.getLoadFromCacheStorage(mock(PortfolioHoldingsCommand.class), List.of());

    // VERIFY
    Assertions.assertEquals(exposures, actual);
  }

  @Test
  void calculate_verifyCalculateNetProducts() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor( cacheStorage, responseMapper));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(MaturityAllocationType.FIVE_TO_SEVEN_YEARS, BigDecimal.TEN));

    doCallRealMethod().when(sut).calculate(any(), any(), any());
    // ACT
    sut.calculate(exposures, holdings, List.of());

    // VERIFY
    verify(sut).calculateNetProducts(exposures, holdings, MaturityAllocationType.values());
  }

  @Test
  void calculate_verifyFromNetProducts() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor( cacheStorage, responseMapper));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(MaturityAllocationType.FIVE_TO_SEVEN_YEARS, BigDecimal.TEN));
    final var netProducts = mock(Map.class);
    final List<Warning> warnings = List.of();
    when(sut.calculateNetProducts(exposures, holdings, MaturityAllocationType.values())).thenReturn(netProducts);

    doCallRealMethod().when(sut).calculate(any(), any(), any());
    // ACT
    sut.calculate(exposures, holdings, warnings);

    // VERIFY
    verify(responseMapper).fromNetProducts(any(), any());
  }

  @Test
  void calculate_verifyAreAllValuesEmptyInMapOfExposure() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor( cacheStorage, responseMapper));
    try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
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
      final var responseMapper = mock(MaturityAllocationResponseMapper.class);
      final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
          .useConstructor( cacheStorage, responseMapper));

      final var exposures = mock(Map.class);
      final var expected = new MaturityAllocationResult();
      expected.setMaturityAllocation(MaturityAllocationCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final var actual = sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      Assertions.assertEquals(expected, actual);
    }
  }
}