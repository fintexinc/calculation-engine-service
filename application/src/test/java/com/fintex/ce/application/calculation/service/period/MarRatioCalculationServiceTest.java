package com.fintex.ce.application.calculation.service.period;

import com.fintex.ce.application.calculation.metric.TrailingTotalReturnsCalculation;
import com.fintex.ce.application.config.PeriodProperties;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.model.domain.result.MaxDrawdownEntry;
import com.fintex.ce.model.domain.result.risk.MarRatioResult;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class MarRatioCalculationServiceTest {

  private MarRatioCalculationService mockService(MaxDrawdownService maxDrawdownService) {
    return mock(MarRatioCalculationService.class, withSettings()
        .useConstructor(mock(PortfolioMonthlyReturnsContextProvider.class), null, new PeriodProperties(),
            maxDrawdownService));
  }

  @Test
  void shouldReturnNull_whenMaxDrawdownValueIsNull() {
    final var maxDrawdownService = mock(MaxDrawdownService.class);
    final var service = mockService(maxDrawdownService);
    final var ttr = mock(TrailingTotalReturnsCalculation.class);
    final var portfolioReturns = mock(NavigableMap.class);
    final var growth10K = mock(NavigableMap.class);
    final var maxDrawdown = new MaxDrawdownEntry(null, null, null, null, null);

    when(ttr.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));
    when(maxDrawdownService.calculateEntry(anyInt(), any(), any())).thenReturn(maxDrawdown);

    doCallRealMethod().when(service).calculateMarRatioPeriod(anyInt(), any(), any(), any());
    final BigDecimal actual = service.calculateMarRatioPeriod(12, portfolioReturns, growth10K, ttr);

    assertNull(actual);
  }

  @Test
  void shouldReturnMarRatio_whenTrailingReturnAndMaxDrawdownPresent() {
    final var maxDrawdownService = mock(MaxDrawdownService.class);
    final var service = mockService(maxDrawdownService);
    final var ttr = mock(TrailingTotalReturnsCalculation.class);
    final var portfolioReturns = mock(NavigableMap.class);
    final var growth10K = mock(NavigableMap.class);
    final var maxDrawdown = new MaxDrawdownEntry(null, new BigDecimal("0.112"), null, null, null);

    when(ttr.calculatePeriodForNumberOfMonths(anyInt())).thenReturn(new BigDecimal("0.111"));
    when(maxDrawdownService.calculateEntry(anyInt(), any(), any())).thenReturn(maxDrawdown);

    doCallRealMethod().when(service).calculateMarRatioPeriod(anyInt(), any(), any(), any());
    final BigDecimal actual = service.calculateMarRatioPeriod(12, portfolioReturns, growth10K, ttr);

    Assertions.assertEquals(new BigDecimal("0.991071428571429"), actual);
  }

  @Test
  void shouldReturnNull_whenPeriodIsLessThanTwelve() {
    final var service = mockService(mock(MaxDrawdownService.class));
    final var portfolioReturns = mock(NavigableMap.class);
    final var growth10K = mock(NavigableMap.class);
    final var ttr = mock(TrailingTotalReturnsCalculation.class);

    doCallRealMethod().when(service).calculateMarRatioPeriod(anyInt(), any(), any(), any());

    assertNull(service.calculateMarRatioPeriod(10, portfolioReturns, growth10K, ttr));
  }

  @Test
  void shouldBuildResult_whenMappingPeriodPairsToTimeIntervals() {
    final var service = mockService(mock(MaxDrawdownService.class));
    final Map<String, BigDecimal> input = Map.of(
        "2000-01-12", ZERO,
        "2020-01-05", BigDecimal.ONE);

    doCallRealMethod().when(service).buildResult(anyMap());
    final MarRatioResult actual = service.buildResult(input);

    assertEquals(input, actual.getMarRatio());
  }

  @Test
  void shouldReturnNull_whenMaxDrawdownIsZero() {
    final var maxDrawdownService = mock(MaxDrawdownService.class);
    final var service = mockService(maxDrawdownService);
    final var ttr = mock(TrailingTotalReturnsCalculation.class);
    final var portfolioReturns = mock(NavigableMap.class);
    final var growth10K = mock(NavigableMap.class);

    when(ttr.calculatePeriodForNumberOfMonths(12)).thenReturn(new BigDecimal("0.1"));
    when(maxDrawdownService.calculateEntry(12, portfolioReturns, growth10K))
        .thenReturn(new MaxDrawdownEntry(null, ZERO, null, null, null));

    doCallRealMethod().when(service).calculateMarRatioPeriod(anyInt(), any(), any(), any());

    assertNull(service.calculateMarRatioPeriod(12, portfolioReturns, growth10K, ttr));
  }

  @Test
  void shouldReturnNull_whenMaxDrawdownIsNull() {
    final var maxDrawdownService = mock(MaxDrawdownService.class);
    final var service = mockService(maxDrawdownService);
    final var ttr = mock(TrailingTotalReturnsCalculation.class);
    final var portfolioReturns = mock(NavigableMap.class);
    final var growth10K = mock(NavigableMap.class);

    when(ttr.calculatePeriodForNumberOfMonths(12)).thenReturn(new BigDecimal("0.1"));
    when(maxDrawdownService.calculateEntry(12, portfolioReturns, growth10K)).thenReturn(null);

    doCallRealMethod().when(service).calculateMarRatioPeriod(anyInt(), any(), any(), any());

    assertNull(service.calculateMarRatioPeriod(12, portfolioReturns, growth10K, ttr));
  }
}
