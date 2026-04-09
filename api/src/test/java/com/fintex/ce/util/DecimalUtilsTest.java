package com.fintex.ce.util;

import com.fintex.ce.domain.constant.BigDecimalConstants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

import static com.fintex.ce.util.DecimalUtils.INTERNAL_SCALE;
import static com.fintex.ce.util.DecimalUtils.ROUNDING_MODE;
import static com.fintex.ce.util.DecimalUtils.pow;
import static com.fintex.ce.util.DecimalUtils.toScale;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

class DecimalUtilsTest {

  @Test
  void divide_bigDecimalDividedProperly() {
    // SETUP
    final BigDecimal v1 = BigDecimal.valueOf(0.3);
    final BigDecimal two = BigDecimal.valueOf(2);

    // ACT
    BigDecimal actual = DecimalUtils.divide(v1, two);

    // VERIFY
    Assertions.assertEquals(v1.divide(two, INTERNAL_SCALE, ROUNDING_MODE), actual);
  }

  @Test
  void divide_doubleDividedProperly() {
    // SETUP
    final double v1Double = 0.3;
    final BigDecimal two = BigDecimal.valueOf(2);

    // ACT
    BigDecimal actual = DecimalUtils.divide(v1Double, two);

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(v1Double).divide(two, INTERNAL_SCALE, ROUNDING_MODE), actual);
  }

  @Test
  void divide_rightDoubleDividedProperly() {
    // SETUP
    final double v1Double = 0.3;
    final BigDecimal two = BigDecimal.valueOf(2);

    // ACT
    BigDecimal actual = DecimalUtils.divide(two, v1Double);

    // VERIFY
    Assertions.assertEquals(two.divide(BigDecimal.valueOf(v1Double), INTERNAL_SCALE, ROUNDING_MODE), actual);
  }

  @Test
  void divide_allDoublesDividedProperly() {
    // SETUP
    final double v1Double = 0.3;
    final double two = 2.;

    // ACT
    BigDecimal actual = DecimalUtils.divide(two, v1Double);

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(two).divide(BigDecimal.valueOf(v1Double), INTERNAL_SCALE, ROUNDING_MODE),
        actual);
  }

  @Test
  void squareRoot_checkResult() {
    // SETUP
    final BigDecimal v1 = new BigDecimal("0.012345");

    // ACT
    BigDecimal actual = DecimalUtils.squareRoot(v1);

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(0.111108055513541), actual);
  }

  @Test
  void squareRoot_checkResult1() {
    // SETUP
    final BigDecimal v1 = new BigDecimal("0.05");

    // ACT
    BigDecimal actual = DecimalUtils.squareRoot(v1);

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(0.223606797749979), actual);
  }

  @Test
  void squareRoot_checkResult2() {
    // SETUP
    final BigDecimal value = BigDecimal.valueOf(3);

    // ACT
    BigDecimal actual = DecimalUtils.squareRoot(value);

    // VERIFY
    Assertions.assertEquals(toUserScale(BigDecimal.valueOf(1.7320508076)), toUserScale(actual));
  }

  @Test
  void squareRoot_checkArithmeticException() {
    // SETUP
    final BigDecimal v1 = new BigDecimal("-1");

    // VERIFY
    Assertions.assertThrows(ArithmeticException.class, () -> {
      DecimalUtils.squareRoot(v1);
    });
  }

  @Test
  void squareRoot_checkNullPointerException() {
    // VERIFY
    Assertions.assertThrows(NullPointerException.class, () -> {
      DecimalUtils.squareRoot(null);
    });
  }

  @Test
  void setInternalScale_checkResult() {
    // SETUP
    final var v1 = new BigDecimal("0.1234567890123456789");

    // ACT
    final BigDecimal actual = DecimalUtils.setInternalScale(v1, RoundingMode.FLOOR);

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(0.123456789012345), actual);
  }

  @Test
  void setInternalScale_checkNullPointerException() {
    // VERIFY
    Assertions.assertThrows(NullPointerException.class, () -> {
      DecimalUtils.setInternalScale(null, RoundingMode.FLOOR);
    });
  }

  @Test
  void getMinValue_checkResult() {
    // SETUP
    final var map = Map.of(
        LocalDate.now().minusMonths(5), BigDecimalConstants.HUNDRED,
        LocalDate.now().minusMonths(3), BigDecimalConstants.TWELVE,
        LocalDate.now().minusMonths(6), BigDecimalConstants.TEN_THOUSAND,
        LocalDate.now().minusMonths(1), ZERO,
        LocalDate.now().minusMonths(2), BigDecimalConstants.ONE);

    // ACT
    final BigDecimal actual = DecimalUtils.getMinValue(map);

    // VERIFY
    Assertions.assertEquals(ZERO, actual);
  }

  @Test
  void getMinValue_checkNullPointerException() {
    // VERIFY
    Assertions.assertThrows(NullPointerException.class, () -> {
      DecimalUtils.getMinValue(null);
    });
  }

  @Test
  void getMaxValue_checkResult() {
    // SETUP
    final var map = Map.of(
        LocalDate.now().minusMonths(5), BigDecimalConstants.HUNDRED,
        LocalDate.now().minusMonths(3), BigDecimalConstants.TWELVE,
        LocalDate.now().minusMonths(6), BigDecimalConstants.TEN_THOUSAND,
        LocalDate.now().minusMonths(1), ZERO,
        LocalDate.now().minusMonths(2), BigDecimalConstants.ONE);

    // ACT
    final BigDecimal actual = DecimalUtils.getMaxValue(map);

    // VERIFY
    Assertions.assertEquals(BigDecimalConstants.TEN_THOUSAND, actual);
  }

  @Test
  void getMaxValue_checkNullPointerException() {
    // VERIFY
    Assertions.assertThrows(NullPointerException.class, () -> {
      DecimalUtils.getMaxValue(null);
    });
  }

  @Test
  void abs_checkNullPointerException() {
    // VERIFY
    Assertions.assertThrows(NullPointerException.class, () -> {
      DecimalUtils.abs(null);
    });
  }

  @Test
  void abs_checkResult() {
    // SETUP
    final var v1 = new BigDecimal("-0.23123123132");

    // ACT
    final BigDecimal actual = DecimalUtils.abs(v1);

    // VERIFY
    Assertions.assertEquals(new BigDecimal("0.23123123132"), actual);
  }

  @Test
  void pow_checkResult() {
    // SETUP
    final double v1 = 15.23141;
    final double v2 = 12.57681;
    final double expectedPow = Math.pow(v1, v2);

    // ACT
    final BigDecimal givenPow = pow(BigDecimal.valueOf(v1), BigDecimal.valueOf(v2));

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(expectedPow), givenPow);
  }

  @Test
  void toUserScale_checkResult() {
    // SETUP
    final var v = Map.of("", new BigDecimal("0.1234567890123456789"));

    // ACT
    final Map<String, BigDecimal> givenWithUserScale = toUserScale(v);

    // VERIFY
    Assertions.assertEquals(Map.of("", new BigDecimal("0.1234567890")), givenWithUserScale);
  }

  @Test
  void toUserScale_whenMapIsNull() {
    // ACT
    final Map<String, BigDecimal> givenWithUserScale = toUserScale((Map<String, BigDecimal>) null);

    // VERIFY
    Assertions.assertNull(givenWithUserScale);
  }

  @Test
  void toScale_checkResult() {
    // SETUP
    final var v = new BigDecimal("0.1234567890123456789");

    // ACT
    final BigDecimal givenWithScale = toScale(v, 5);

    // VERIFY
    Assertions.assertEquals(new BigDecimal("0.12346"), givenWithScale);
  }

  @Test
  void toScale_whenValueIsNull() {
    // ACT
    final BigDecimal givenWithScale = toScale(null, 5);

    // VERIFY
    Assertions.assertNull(givenWithScale);
  }

  @Test
  void toScale_whenValueIsZero() {
    // ACT
    final BigDecimal givenWithScale = toScale(ZERO, 5);

    // VERIFY
    Assertions.assertEquals(ZERO, givenWithScale);
  }

  @Test
  void toScale_whenValueIsOne() {
    // ACT
    final BigDecimal givenWithScale = toScale(ONE, 5);

    // VERIFY
    Assertions.assertEquals(ONE, givenWithScale);
  }

}