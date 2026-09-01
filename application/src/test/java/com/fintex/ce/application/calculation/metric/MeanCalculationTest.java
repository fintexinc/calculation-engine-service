package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.CalculationUtils;
import com.fintex.ce.model.domain.result.returns.MeanResult;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled("metric unsupported")
class MeanCalculationTest {

  @Test
  void shouldCalculateAverageForPeriod_whenReturnsContainEnoughData() {
    try (var util = Mockito.mockStatic(CalculationUtils.class)) {
      final var calculation = mock(MeanCalculation.class);
      final var returns = mock(NavigableMap.class);
      final var periodStartDate = mock(LocalDate.class);
      final var portfolioTotalReturnsByPeriod = mock(SortedMap.class);

      final var numberOfMonths = 12;
      final var nowDate = LocalDate.now();

      when(calculation.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);
      when(calculation.getPortfolioTotalReturns()).thenReturn(returns);
      when(returns.size()).thenReturn(15);
      when(calculation.getPeriodStartDate(Mockito.anyInt(), Mockito.any(NavigableMap.class))).thenReturn(
          periodStartDate);
      when(calculation.getSubMapByPeriodStartDate(Mockito.any(), Mockito.any())).thenReturn(
          portfolioTotalReturnsByPeriod);

      util.when(() -> CalculationUtils.average(Mockito.any())).thenReturn(BigDecimal.ONE);
      doCallRealMethod().when(calculation).calculatePeriodForNumberOfMonths(anyInt());

      final BigDecimal result = calculation.calculatePeriodForNumberOfMonths(numberOfMonths);

      Assertions.assertEquals(BigDecimal.ONE, result);
    }
  }

  @Test
  void shouldMapIntervalsToMeanResult_whenDefineResponseTypeIsCalled() {
    final MeanCalculation<MeanResult> calculation = mock(MeanCalculation.class);
    final Map<String, BigDecimal> results = Map.of("ONE_YR", BigDecimal.ONE);

    doCallRealMethod().when(calculation).defineResponseType(anyMap());

    final MeanResult result = calculation.defineResponseType(results);

    Assertions.assertEquals(results, result.getMean());

  }

}
