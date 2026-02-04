package com.fintex.ce.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static com.fintex.ce.util.DecimalUtils.abs;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculationUtilsTest {

  @Test
  void sumProduct_checkResult() {
    // SETUP
    final Map<String, BigDecimal> m1 = Map.of("1", BigDecimal.valueOf(2), "2", BigDecimal.valueOf(5));
    final Map<String, BigDecimal> m2 = Map.of("1", BigDecimal.valueOf(3), "2", BigDecimal.valueOf(4));

    // ACT
    final BigDecimal actual = CalculationUtils.sumProduct(m1, m2);

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(26).compareTo(actual));
  }

  @Test
  void product_checkResult() {
    // SETUP
    final Map<String, BigDecimal> m1 = Map.of("1", BigDecimal.valueOf(2), "2", BigDecimal.valueOf(5));

    // ACT
    final BigDecimal actual = CalculationUtils.product(m1);

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(10).compareTo(actual));
  }

  @Test
  void sumProduct2_checkResult() {
    // SETUP
    final Map<String, BigDecimal> m1 = Map.of("1", BigDecimal.valueOf(2), "2", BigDecimal.valueOf(5));
    final Map<String, BigDecimal> m2 = Map.of("1", BigDecimal.valueOf(3), "2", BigDecimal.valueOf(6));
    final Map<String, BigDecimal> m3 = Map.of("1", BigDecimal.valueOf(4), "2", BigDecimal.valueOf(7));

    // ACT
    final BigDecimal actual = CalculationUtils.sumProduct(m1, m2, m3);

    // VERIFY
    assertEquals(0, BigDecimal.valueOf(((2 * 3 * 4) + (5 * 6 * 7))).compareTo(actual));
  }

  @Test
  void reScaleAbs_checkResult() {
    // SETUP
    final BigDecimal v1 = BigDecimal.valueOf(2);
    final BigDecimal v2 = BigDecimal.valueOf(5);
    final BigDecimal sum = v1.add(v2);

    final Map<String, BigDecimal> m1 = Map.of("1", v1, "2", v2);

    // ACT
    final Map<String, BigDecimal> actual = CalculationUtils.reScaleAbs(m1);

    // VERIFY
    assertEquals(Map.of("1", divide(v1, sum), "2", divide(v2, sum)), actual);
  }

  @Test
  void reScaleAbsWithNegative_checkResult() {
    // SETUP
    final BigDecimal v1 = BigDecimal.valueOf(2);
    final BigDecimal v2 = BigDecimal.valueOf(-5);
    final BigDecimal sum = abs(v1).add(abs(v2));

    final Map<String, BigDecimal> m1 = Map.of("1", v1, "2", v2);

    // ACT
    final Map<String, BigDecimal> actual = CalculationUtils.reScaleAbs(m1);

    // VERIFY
    assertEquals(Map.of("1", divide(v1, sum), "2", divide(v2, sum)), actual);
  }

  @Test
  void reScaleAbs_whenMapIsEmpty() {
    // SETUP
    final Map<String, BigDecimal> origin = Map.of();

    // ACT
    final Map<String, BigDecimal> actual = CalculationUtils.reScaleAbs(origin);

    // VERIFY
    assertEquals(origin, actual);
  }

  @Test
  void reScale_checkResult() {
    // SETUP
    final BigDecimal v1 = BigDecimal.valueOf(2);
    final BigDecimal v2 = BigDecimal.valueOf(5);
    final BigDecimal sum = v1.add(v2);

    final Map<String, BigDecimal> m1 = Map.of("1", v1, "2", v2);

    // ACT
    final Map<String, BigDecimal> actual = CalculationUtils.reScale(m1);

    // VERIFY
    assertEquals(Map.of("1", divide(v1, sum), "2", divide(v2, sum)), actual);
  }

  @Test
  void reScaleWithNegative_checkResult() {
    // SETUP
    final BigDecimal v1 = BigDecimal.valueOf(2);
    final BigDecimal v2 = BigDecimal.valueOf(-5);
    final BigDecimal sum = v1.add(v2);

    final Map<String, BigDecimal> m1 = Map.of("1", v1, "2", v2);

    // ACT
    final Map<String, BigDecimal> actual = CalculationUtils.reScale(m1);

    // VERIFY
    assertEquals(Map.of("1", divide(v1, sum), "2", divide(v2, sum)), actual);
  }

  @Test
  void reScale_whenMapIsEmpty() {
    // SETUP
    final Map<String, BigDecimal> origin = Map.of();

    // ACT
    final Map<String, BigDecimal> actual = CalculationUtils.reScale(origin);

    // VERIFY
    assertEquals(origin, actual);
  }
  @Test
  void sum_checkResult() {
    // SETUP
    final BigDecimal v1 = BigDecimal.valueOf(2);
    final BigDecimal v2 = BigDecimal.valueOf(5);
    final BigDecimal sum = v1.add(v2);

    final Map<String, BigDecimal> m1 = Map.of("1", v1, "2", v2);

    // ACT
    final BigDecimal actual = CalculationUtils.sum(m1);

    // VERIFY
    assertEquals(sum, actual);
  }

  @Test
  void average_checkResult() {
    // SETUP
    final BigDecimal v1 = BigDecimal.valueOf(2);
    final BigDecimal v2 = BigDecimal.valueOf(5);
    final BigDecimal average = v1.add(v2).divide(v1);

    final Map<String, BigDecimal> m1 = Map.of("1", v1, "2", v2);

    // ACT
    final BigDecimal actual = CalculationUtils.average(m1);

    // VERIFY
    assertEquals(toUserScale(average), toUserScale(actual));
  }

  @Test
  void isNegativeNumeric_checkResult1() {
    // SETUP
    final String value = "-1";

    // ACT
    final boolean actual = CalculationUtils.isNegativeNumeric(value);

    // VERIFY
    assertTrue(actual);
  }

  @Test
  void isNegativeNumeric_checkResult2() {
    // SETUP
    final String value = "2";

    // ACT
    final boolean actual = CalculationUtils.isNegativeNumeric(value);

    // VERIFY
    assertFalse(actual);
  }

  @Test
  void isNegativeNumeric_checkResult3() {
    // SETUP
    final String value = "asdfasd";

    // ACT
    final boolean actual = CalculationUtils.isNegativeNumeric(value);

    // VERIFY
    assertFalse(actual);
  }

}