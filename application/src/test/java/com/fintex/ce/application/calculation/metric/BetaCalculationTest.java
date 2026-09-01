package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.model.domain.result.risk.BetaResult;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

@Disabled("metric unsupported")
class BetaCalculationTest {

  @Test
  void shouldDefineResponseType_whenCheckResult() {
    final BetaCalculation beta = mock(BetaCalculation.class);

    final Map<String, BigDecimal> periods = Map.of("2020-01-05", BigDecimal.ONE, "2000-01-12", ZERO);

    final Map<String, BigDecimal> expected = Map.of("2000-01-12", ZERO, "2020-01-05", BigDecimal.ONE);

    doCallRealMethod().when(beta).defineResponseType(anyMap());
    final BetaResult actual = beta.defineResponseType(periods);

    assertEquals(expected, actual.getBeta());
  }

}
