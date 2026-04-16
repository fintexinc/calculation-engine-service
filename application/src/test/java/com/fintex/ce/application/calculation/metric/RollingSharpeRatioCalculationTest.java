package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.IntervalResult;
import com.fintex.ce.model.domain.result.RollingIntervalResult;
import com.fintex.ce.model.domain.result.rolling.RollingSharpeRatioResult;
import com.fintex.ce.model.dto.calculation.CalculationDTO;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static com.fintex.ce.model.util.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.model.util.BigDecimalConstants.TWO;
import static java.math.BigDecimal.TEN;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class RollingSharpeRatioCalculationTest {

  private static NavigableMap<LocalDate, BigDecimal> portfolioReturns;

  @BeforeAll
  static void setUp() {
    portfolioReturns = new TreeMap<>();
    portfolioReturns.put(LocalDate.now().minusMonths(12), HUNDRED);
    portfolioReturns.put(LocalDate.now().minusMonths(11), TEN);
    portfolioReturns.put(LocalDate.now().minusMonths(10), HUNDRED);
    portfolioReturns.put(LocalDate.now().minusMonths(9), ONE);
    portfolioReturns.put(LocalDate.now().minusMonths(8), TWO);
    portfolioReturns.put(LocalDate.now().minusMonths(7), TEN_THOUSAND);
    portfolioReturns.put(LocalDate.now().minusMonths(6), ONE);
    portfolioReturns.put(LocalDate.now().minusMonths(5), HUNDRED);
    portfolioReturns.put(LocalDate.now().minusMonths(4), TEN_THOUSAND);
    portfolioReturns.put(LocalDate.now().minusMonths(3), ONE);
    portfolioReturns.put(LocalDate.now().minusMonths(2), HUNDRED);
    portfolioReturns.put(LocalDate.now().minusMonths(1), TEN_THOUSAND);
  }

  @Test
  void shouldReturnSharpeRatioValue_whenCalculatingRollingValue() {
    final var calculationDTO = mock(CalculationDTO.class);
    final var sharpeRatioCalculation = mock(SharpeRatioCalculation.class);
    final var sut = mock(RollingSharpeRatioCalculation.class, withSettings().useConstructor(calculationDTO, Set.of(),
        sharpeRatioCalculation));
    final int numberOfMonths = 12;

    when(sharpeRatioCalculation.calculatePeriodForNumberOfMonths(anyInt(), any())).thenReturn(TEN);

    doCallRealMethod().when(sut).calculateRollingValue(numberOfMonths, portfolioReturns);
    final BigDecimal actual = sut.calculateRollingValue(numberOfMonths, portfolioReturns);

    Assertions.assertEquals(TEN, actual);
  }

  @Test
  void shouldDelegateToRollingIntervalResults_whenDefiningResponseType() {
    final var sut = mock(RollingSharpeRatioCalculation.class);
    final var result = Set.of(Pair.of("12", portfolioReturns));

    doCallRealMethod().when(sut).defineResponseType(result);
    sut.defineResponseType(result);

    verify(sut).getRollingIntervalResults(result);
  }

  @Test
  void shouldMapRollingSharpeRatioResult_whenDefiningResponseType() {
    final var sut = mock(RollingSharpeRatioCalculation.class);
    final var result = Set.of(Pair.of("12", portfolioReturns));

    final LinkedHashSet<IntervalResult> res = new LinkedHashSet<>();
    res.add(new IntervalResult(LocalDate.now().minusMonths(3), TEN));
    final var resDTO = new RollingIntervalResult("12", res);
    final var expected = new RollingSharpeRatioResult().setRollingSharpeRatio(Set.of(resDTO));

    when(sut.getRollingIntervalResults(anySet())).thenReturn(Set.of(resDTO));

    doCallRealMethod().when(sut).defineResponseType(result);
    final RollingSharpeRatioResult actual = sut.defineResponseType(result);

    Assertions.assertEquals(expected.getRollingSharpeRatio(), actual.getRollingSharpeRatio());
  }

  @AfterAll
  static void tearDown() {
    portfolioReturns.clear();
  }
}