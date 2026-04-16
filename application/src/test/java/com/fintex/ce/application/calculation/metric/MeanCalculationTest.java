package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.returns.MeanResult;
import com.fintex.ce.util.CalculationUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeanCalculationTest {

  @Test
  void shouldCalculateAverageForPeriod_whenReturnsContainEnoughData() {
    try (var util = Mockito.mockStatic(CalculationUtils.class)) {
      final var sut = mock(MeanCalculation.class);
      final var returns = mock(NavigableMap.class);
      final var periodStartDate = mock(LocalDate.class);
      final var portfolioTotalReturnsByPeriod = mock(SortedMap.class);

      final var numberOfMonths = 12;
      final var nowDate = LocalDate.now();

      when(sut.getPeriodStartDate(anyInt(), any())).thenReturn(nowDate);
      when(sut.getPortfolioTotalReturns()).thenReturn(returns);
      when(returns.size()).thenReturn(15);
      when(sut.getPeriodStartDate(Mockito.anyInt(), Mockito.any(NavigableMap.class))).thenReturn(periodStartDate);
      when(sut.getSubMapByPeriodStartDate(Mockito.any(), Mockito.any())).thenReturn(portfolioTotalReturnsByPeriod);

      util.when(() -> CalculationUtils.average(Mockito.any())).thenReturn(BigDecimal.ONE);
      doCallRealMethod().when(sut).calculatePeriodForNumberOfMonths(anyInt());

      final BigDecimal result = sut.calculatePeriodForNumberOfMonths(numberOfMonths);

      Assertions.assertEquals(BigDecimal.ONE, result);
    }
  }

  @Test
  void shouldMapIntervalsToMeanResult_whenDefineResponseTypeIsCalled() {
    final MeanCalculation<MeanResult> sut = mock(MeanCalculation.class);
    final var results = mock(Set.class);
    final var timeIntervals = mock(Set.class);

    when(sut.formTimeIntervalResult(results)).thenReturn(timeIntervals);

    doCallRealMethod().when(sut).defineResponseType(any());

    final MeanResult result = sut.defineResponseType(results);

    Assertions.assertEquals(timeIntervals, result.getMean());

  }

}
