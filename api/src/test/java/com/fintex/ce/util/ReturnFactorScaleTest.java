package com.fintex.ce.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReturnFactorScaleTest {

  @Test
  void SCALE_OF_ONE_checkResult() {
    // SETUP
    final var formula = ReturnFactorScale.SCALE_OF_ONE.getFormula();
    final var v = new BigDecimal("20");

    // ACT
    final BigDecimal givenValue = formula.apply(Map.entry(LocalDate.MIN, v));

    // VERIFY
    assertEquals(0, new BigDecimal("0.2").compareTo(givenValue));
  }

  @Test
  void SCALE_OF_TWO_checkResult() {
    // SETUP
    final var formula = ReturnFactorScale.SCALE_OF_TWO.getFormula();
    final var v = new BigDecimal("20");

    // ACT
    final BigDecimal givenValue = formula.apply(Map.entry(LocalDate.MIN, v));

    // VERIFY
    assertEquals(0, new BigDecimal("1.2").compareTo(givenValue));
  }

  @Test
  void AS_IS_checkResult() {
    // SETUP
    final var formula = ReturnFactorScale.AS_IS.getFormula();
    final var v = new BigDecimal("20");

    // ACT
    final BigDecimal givenValue = formula.apply(Map.entry(LocalDate.MIN, v));

    // VERIFY
    assertEquals(new BigDecimal("20"), givenValue);
  }
}