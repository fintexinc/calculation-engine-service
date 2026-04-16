package com.fintex.ce.application.calculation.service;

import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocation;
import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocationType;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.domain.result.allocation.ClassificationAllocationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.ExposureDataHolder;
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

class ClassificationAllocationCalculationServiceImplTest {

  @Test
  void shouldFetch_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(ClassificationAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher));

    final var holding = mock(Holding.class);
    final var classificationAllocation = new ClassificationAllocation()
        .setSecurityClassificationValues(Map.of(
            ClassificationAllocationType.CASH_AND_CASH_EQUIVALENTS__INTERNATIONAL, BigDecimal.TEN));
    final var rawData = Map.of(holding, classificationAllocation);

    when(fetcher.fetch(any(), any())).thenReturn(rawData);
    doCallRealMethod().when(service).fetchExposures(any());
    // ACT
    final var result = service.fetchExposures(mock(PortfolioHoldingsCommand.class));
    final var actual = result.allocations();

    // VERIFY
    Assertions.assertEquals(1, actual.size());
    Assertions.assertTrue(actual.containsKey(holding));
    Assertions.assertEquals(BigDecimal.TEN, actual.get(holding).get(
        ClassificationAllocationType.CASH_AND_CASH_EQUIVALENTS__INTERNATIONAL));
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    // SETUP
    final var service = mock(ClassificationAllocationCalculationServiceImpl.class);

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(ClassificationAllocationType.ALTERNATIVE_INVESTMENTS__INTERNATIONAL,
        BigDecimal.TEN));

    doCallRealMethod().when(service).calculate(any(), any());
    // ACT
    service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

    // VERIFY
    verify(service).calculateNetProducts(exposures, holdings, ClassificationAllocationType.values());
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    // SETUP
    try (final var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      final var service = mock(ClassificationAllocationCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(ClassificationAllocationType.ALTERNATIVE_INVESTMENTS__INTERNATIONAL,
          BigDecimal.TEN));
      final var netProducts = mock(Map.class);
      when(service.calculateNetProducts(exposures, holdings, ClassificationAllocationType.values())).thenReturn(
          netProducts);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);

      // VERIFY
      mockedCalculationUtils.verify(() -> CalculationUtils.reScale(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    // SETUP
    try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var exposures = mock(Map.class);
      final var service = mock(ClassificationAllocationCalculationServiceImpl.class);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
    }
  }

  @Test
  void shouldCalculate_whenCheckResultWhenExposureIsAllZeroValuesMap() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var fetcher = mock(SecurityDataFetcher.class);
      final var service = mock(ClassificationAllocationCalculationServiceImpl.class, withSettings()
          .useConstructor(fetcher));

      final var exposures = mock(Map.class);
      final var expected = new ClassificationAllocationResult();
      expected.setClassificationAllocation(ClassificationAllocationCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);

      doCallRealMethod().when(service).calculate(any(), any());
      // ACT
      final var actual = service.calculate(new ExposureDataHolder<>(exposures, List.of()), List.of());

      // VERIFY
      Assertions.assertEquals(expected, actual);
    }
  }
}