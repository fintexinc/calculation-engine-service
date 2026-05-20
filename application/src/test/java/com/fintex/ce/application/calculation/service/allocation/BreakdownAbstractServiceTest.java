package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class BreakdownAbstractServiceTest {

  @Test
  void shouldCalculateNetProducts_whenVerifyCalculateInitialPortfolioWeight() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(BreakdownAbstractService.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);

      doCallRealMethod().when(service).calculateNetProducts(any(), any(), any());
      // ACT
      service.calculateNetProducts(exposures, holdings, EquityMarketCapitalizationType.values());

      mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
    }
  }

  @Test
  void shouldCalculateNetProducts_whenVerifyCalculateNetProductForEachEquityMarketCapitalizationType() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      final var service = mock(BreakdownAbstractService.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var weights = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(weights);

      doCallRealMethod().when(service).calculateNetProducts(any(), any(), any());
      // ACT
      service.calculateNetProducts(exposures, holdings, EquityMarketCapitalizationType.values());

      // VERIFY
      for (EquityMarketCapitalizationType type : EquityMarketCapitalizationType.values()) {
        verify(service).calculateNetProduct(type, exposures, weights);
      }
    }
  }

  @Test
  void shouldCalculateNetProducts_whenCheckResult() {
    final var service = mock(BreakdownAbstractService.class);

    final var holdings = mock(List.class);
    final var exposures = mock(Map.class);
    final var expected = stream(EquityMarketCapitalizationType.values()).collect(toMap(identity(), e -> TEN));

    when(service.calculateNetProduct(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(service).calculateNetProducts(any(), any(), any());
    // ACT
    final var actual = service.calculateNetProducts(exposures, holdings, EquityMarketCapitalizationType.values());

    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }

  @Test
  void shouldCalculateNetProduct_whenVerifySumProduct() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      final var service = mock(BreakdownAbstractService.class);

      final var holding = mock(PortfolioHolding.class);
      final var exposures = Map.of(
          holding, Map.of(EquityMarketCapitalizationType.SMALL, TEN),
          mock(PortfolioHolding.class), Map.of(EquityMarketCapitalizationType.MEDIUM, BigDecimal.ONE));
      final var weights = Map.of(holding, TEN);
      final var typeExposures = Map.of(holding, TEN);

      doCallRealMethod().when(service).calculateNetProduct(any(), any(), any());
      // ACT
      service.calculateNetProduct(EquityMarketCapitalizationType.SMALL, exposures, weights);

      mockedCalculationUtils.verify(() -> CalculationUtils.sumProduct(typeExposures, weights));
    }
  }

  @Test
  void shouldCalculateNetProduct_whenCheckResult() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      final var service = mock(BreakdownAbstractService.class);

      final var expectedResult = BigDecimal.TEN;
      final var exposures = mock(Map.class);
      final var weights = mock(Map.class);

      mockedCalculationUtils.when(() -> CalculationUtils.sumProduct(any(), any())).thenReturn(expectedResult);

      doCallRealMethod().when(service).calculateNetProduct(any(), any(), any());
      // ACT
      final var actual = service.calculateNetProduct(EquityMarketCapitalizationType.MEDIUM, exposures, weights);

      assertSame(expectedResult, actual);
    }
  }

  @Test
  void shouldPerform_whenVerifyFetch() {
    final var service = mock(BreakdownAbstractService.class, withSettings().useConstructor());

    final var holdings = List.of(mock(PortfolioHolding.class));
    final var req = mock(PortfolioHoldingsCommand.class);

    when(req.getHoldings()).thenReturn(holdings);
    when(service.fetchExposures(any())).thenReturn(new ExposureDataHolder<>(Map.of(), List.of()));

    doCallRealMethod().when(service).perform(any());
    service.perform(req);

    verify(service).fetchExposures(req);

  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    final var service = mock(BreakdownAbstractService.class, withSettings().useConstructor());

    final var holdings = List.of(mock(PortfolioHolding.class));
    final var req = mock(PortfolioHoldingsCommand.class);
    final Map exposures = mock(Map.class);

    when(req.getHoldings()).thenReturn(holdings);
    when(service.fetchExposures(any())).thenReturn(new ExposureDataHolder<>(exposures, List.of()));

    doCallRealMethod().when(service).perform(any());
    service.perform(req);

    verify(service).calculate(new ExposureDataHolder<>(exposures, List.of()), holdings);
  }

}
