package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.MaturityAllocationResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.domain.model.calculation.MaturityAllocationType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.MaturityAllocationResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
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

@SuppressWarnings("unchecked")
class MaturityAllocationCalculationServiceImplTest {

  @Test
  void fetchExposures_checkResult() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(Holding.class);
    final var maturityAllocation = new MaturityAllocation();
    maturityAllocation.setMaturityDurationValues(Map.of("FIVE_TO_SEVEN_YEARS", BigDecimal.TEN));
    final var rawData = Map.of(holding, maturityAllocation);

    when(fetcher.fetch(any(), any())).thenReturn(rawData);
    doCallRealMethod().when(sut).fetchExposures(any(), any());
    final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), new java.util.ArrayList<>());

    Assertions.assertEquals(1, actual.size());
    Assertions.assertTrue(actual.containsKey(holding));
  }

  @Test
  void calculate_verifyCalculateNetProducts() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(MaturityAllocationType.FIVE_TO_SEVEN_YEARS, BigDecimal.TEN));

    doCallRealMethod().when(sut).calculate(any(), any(), any());
    sut.calculate(exposures, holdings, List.of());

    verify(sut).calculateNetProducts(exposures, holdings, MaturityAllocationType.values());
  }

  @Test
  void calculate_verifyFromNetProducts() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(MaturityAllocationType.FIVE_TO_SEVEN_YEARS, BigDecimal.TEN));
    final var netProducts = mock(Map.class);
    final List<Warning> warnings = List.of();
    when(sut.calculateNetProducts(exposures, holdings, MaturityAllocationType.values())).thenReturn(netProducts);

    doCallRealMethod().when(sut).calculate(any(), any(), any());
    sut.calculate(exposures, holdings, warnings);

    verify(responseMapper).fromNetProducts(any(), any());
  }

  @Test
  void calculate_verifyAreAllValuesEmptyInMapOfExposure() {
    final var fetcher = mock(SecurityDataFetcher.class);
    final var responseMapper = mock(MaturityAllocationResponseMapper.class);
    final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher, responseMapper));
    try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      sut.calculate(exposures, List.of(), List.of());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var fetcher = mock(SecurityDataFetcher.class);
      final var responseMapper = mock(MaturityAllocationResponseMapper.class);
      final var sut = mock(MaturityAllocationCalculationServiceImpl.class, withSettings()
          .useConstructor(fetcher, responseMapper));

      final var exposures = mock(Map.class);
      final var expected = new MaturityAllocationResult();
      expected.setMaturityAllocation(MaturityAllocationCalculationServiceImpl.ALLOCATION_DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);
      when(responseMapper.toEmptyResponse(any())).thenReturn(expected);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      final var actual = sut.calculate(exposures, List.of(), List.of());

      Assertions.assertEquals(expected, actual);
    }
  }
}
