package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.calculation.ClassificationAllocationType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.ClassificationAllocationResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.util.CalculationUtils;
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

class ClassificationAllocationCalculationServiceImplTest {

  @Test
  void shouldFetch_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(ClassificationAllocationCalculationServiceImpl.class, withSettings()
        .useConstructor(fetcher));

    final var holding = mock(Holding.class);
    final var classificationAllocation = new ClassificationAllocation()
        .setSecurityClassificationValues(Map.of(
            ClassificationAllocationType.CASH_AND_CASH_EQUIVALENTS__INTERNATIONAL.name(), BigDecimal.TEN));
    final var rawData = Map.of(holding, classificationAllocation);

    when(fetcher.fetch(any(), any())).thenReturn(rawData);
    doCallRealMethod().when(sut).fetchExposures(any(), any());
    // ACT
    final var actual = sut.fetchExposures(mock(PortfolioHoldingsCommand.class), List.of());

    // VERIFY
    Assertions.assertEquals(1, actual.size());
    Assertions.assertTrue(actual.containsKey(holding));
    Assertions.assertEquals(BigDecimal.TEN, actual.get(holding).get(ClassificationAllocationType.CASH_AND_CASH_EQUIVALENTS__INTERNATIONAL));
  }

  @Test
  void shouldCalculate_whenVerifyCalculateNetProducts() {
    // SETUP
    final var sut = mock(ClassificationAllocationCalculationServiceImpl.class);

    final var holding = mock(Holding.class);
    final var holdings = List.of(holding);
    final var exposures = Map.of(holding, Map.of(ClassificationAllocationType.ALTERNATIVE_INVESTMENTS__INTERNATIONAL,
        BigDecimal.TEN));

    doCallRealMethod().when(sut).calculate(any(), any(), any());
    // ACT
    sut.calculate(exposures, holdings, List.of());

    // VERIFY
    verify(sut).calculateNetProducts(exposures, holdings, ClassificationAllocationType.values());
  }

  @Test
  void shouldCalculate_whenVerifyReScale() {
    // SETUP
    try (final var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      final var sut = mock(ClassificationAllocationCalculationServiceImpl.class);

      final var holding = mock(Holding.class);
      final var holdings = List.of(holding);
      final var exposures = Map.of(holding, Map.of(ClassificationAllocationType.ALTERNATIVE_INVESTMENTS__INTERNATIONAL,
          BigDecimal.TEN));
      final var netProducts = mock(Map.class);
      when(sut.calculateNetProducts(exposures, holdings, ClassificationAllocationType.values())).thenReturn(
          netProducts);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      sut.calculate(exposures, holdings, List.of());

      // VERIFY
      mockedCalculationUtils.verify(() -> CalculationUtils.reScale(netProducts));
    }
  }

  @Test
  void shouldCalculate_whenVerifyAreAllValuesEmptyInMapOfExposure() {
    // SETUP
    try (final var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var exposures = mock(Map.class);
      final var sut = mock(ClassificationAllocationCalculationServiceImpl.class);

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
      final var fetcher = mock(SecurityDataFetcher.class);
      final var sut = mock(ClassificationAllocationCalculationServiceImpl.class, withSettings()
          .useConstructor( fetcher));

      final var exposures = mock(Map.class);
      final var expected = new ClassificationAllocationResult();
      expected.setClassificationAllocation(ClassificationAllocationCalculationServiceImpl.DEFAULT_MAP);
      expected.setWarnings(List.of());

      mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);

      doCallRealMethod().when(sut).calculate(any(), any(), any());
      // ACT
      final var actual = sut.calculate(exposures, List.of(), List.of());

      // VERIFY
      Assertions.assertEquals(expected, actual);
    }
  }
}