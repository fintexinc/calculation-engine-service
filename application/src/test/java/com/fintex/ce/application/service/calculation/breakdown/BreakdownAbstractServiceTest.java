package com.fintex.ce.application.service.calculation.breakdown;

import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.calculation.EquityMarketCapType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.CalculationUtils;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
      // SETUP
      final var sut = mock(BreakdownAbstractService.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);

      doCallRealMethod().when(sut).calculateNetProducts(any(), any(), any());
      // ACT
      sut.calculateNetProducts(exposures, holdings, EquityMarketCapType.values());

      // VERIFY
      mockedPortfolioUtils.verify(() -> PortfolioUtils.calculateInitialPortfolioWeight(holdings));
    }
  }

  @Test
  void shouldCalculateNetProducts_whenVerifyCalculateNetProductForEachEquityMarketCapType() {
    try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
      // SETUP
      final var sut = mock(BreakdownAbstractService.class);

      final var holdings = mock(List.class);
      final var exposures = mock(Map.class);
      final var weights = mock(Map.class);

      mockedPortfolioUtils.when(() -> PortfolioUtils.calculateInitialPortfolioWeight(any())).thenReturn(weights);

      doCallRealMethod().when(sut).calculateNetProducts(any(), any(), any());
      // ACT
      sut.calculateNetProducts(exposures, holdings, EquityMarketCapType.values());

      // VERIFY
      for (EquityMarketCapType type : EquityMarketCapType.values()) {
        verify(sut).calculateNetProduct(type, exposures, weights);
      }
    }
  }

  @Test
  void shouldCalculateNetProducts_whenCheckResult() {
    // SETUP
    final var sut = mock(BreakdownAbstractService.class);

    final var holdings = mock(List.class);
    final var exposures = mock(Map.class);
    final var expected = stream(EquityMarketCapType.values()).collect(toMap(identity(), e -> TEN));

    when(sut.calculateNetProduct(any(), any(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculateNetProducts(any(), any(), any());
    // ACT
    final var actual = sut.calculateNetProducts(exposures, holdings, EquityMarketCapType.values());

    // VERIFY
    Assertions.assertNotNull(actual);
    ComparisonUtils.compareMaps(expected, actual);
  }

  @Test
  void shouldCalculateNetProduct_whenVerifySumProduct() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      // SETUP
      final var sut = mock(BreakdownAbstractService.class);

      final var holding = mock(Holding.class);
      final var exposures = Map.of(
          holding, Map.of(EquityMarketCapType.SMALL, TEN),
          mock(Holding.class), Map.of(EquityMarketCapType.MEDIUM, BigDecimal.ONE));
      final var weights = Map.of(holding, TEN);
      final var typeExposures = Map.of(holding, TEN);

      doCallRealMethod().when(sut).calculateNetProduct(any(), any(), any());
      // ACT
      sut.calculateNetProduct(EquityMarketCapType.SMALL, exposures, weights);

      // VERIFY
      mockedCalculationUtils.verify(() -> CalculationUtils.sumProduct(typeExposures, weights));
    }
  }

  @Test
  void shouldCalculateNetProduct_whenCheckResult() {
    try (var mockedCalculationUtils = Mockito.mockStatic(CalculationUtils.class)) {
      // SETUP
      final var sut = mock(BreakdownAbstractService.class);

      final var expectedResult = BigDecimal.TEN;
      final var exposures = mock(Map.class);
      final var weights = mock(Map.class);

      mockedCalculationUtils.when(() -> CalculationUtils.sumProduct(any(), any())).thenReturn(expectedResult);

      doCallRealMethod().when(sut).calculateNetProduct(any(), any(), any());
      // ACT
      final var actual = sut.calculateNetProduct(EquityMarketCapType.MEDIUM, exposures, weights);

      // VERIFY
      assertSame(expectedResult, actual);
    }
  }

  @Test
  void shouldPerform_whenVerifyFetch() {
    // SETUP
    final var sut = mock(BreakdownAbstractService.class, withSettings().useConstructor());

    final var holdings = List.of(mock(Holding.class));
    final var req = mock(PortfolioHoldingsCommand.class);

    when(req.getHoldings()).thenReturn(holdings);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(req);

    // VERIFY
    verify(sut).fetchExposures(req, List.of());

  }

  @Test
  void shouldPerform_whenVerifyCalculate() {
    // SETUP
    final var sut = mock(BreakdownAbstractService.class, withSettings().useConstructor());

    final var holdings = List.of(mock(Holding.class));
    final var req = mock(PortfolioHoldingsCommand.class);
    final Map exposures = mock(Map.class);

    when(req.getHoldings()).thenReturn(holdings);
    when(sut.fetchExposures(any(), any())).thenReturn(exposures);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(req);

    // VERIFY
    verify(sut).calculate(exposures, holdings, List.of());
  }

}
