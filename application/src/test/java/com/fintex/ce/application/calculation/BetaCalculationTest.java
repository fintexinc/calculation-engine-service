package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.BetaCalculation;
import com.fintex.ce.port.input.result.BetaResult;
import com.fintex.ce.port.input.result.core.TimeIntervalResult;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BetaCalculationTest {

  @Test
  void shouldDefineResponseType_whenVerifyFormTimeIntervalResult() {
    final BetaCalculation alpha = mock(BetaCalculation.class);

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2000-01-12", ZERO), Pair.of("2020-01-05",
        BigDecimal.ONE));

    doCallRealMethod().when(alpha).defineResponseType(anySet());
    alpha.defineResponseType(pairs);

    verify(alpha).formTimeIntervalResult(pairs);
  }

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final BetaCalculation beta = mock(BetaCalculation.class);

    final Set<Pair<String, BigDecimal>> pairs = Set.of(Pair.of("2020-01-05", BigDecimal.ONE), Pair.of("2000-01-12",
        ZERO));

    final Set<TimeIntervalResult> expected = Set.of(
        new TimeIntervalResult("2000-01-12", ZERO),
        new TimeIntervalResult("2020-01-05", BigDecimal.ONE));
    when(beta.formTimeIntervalResult(anySet())).thenReturn(expected);

    doCallRealMethod().when(beta).defineResponseType(anySet());
    final BetaResult actual = beta.defineResponseType(pairs);

    assertEquals(expected, actual.getBeta());
  }

}
