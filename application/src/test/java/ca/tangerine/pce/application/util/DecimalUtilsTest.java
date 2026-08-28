package ca.tangerine.pce.application.util;

import ca.tangerine.pce.model.util.BigDecimalConstants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

import static ca.tangerine.pce.application.util.DecimalUtils.pow;
import static ca.tangerine.pce.application.util.DecimalUtils.toScale;
import static ca.tangerine.pce.application.util.DecimalUtils.toUserScale;
import static ca.tangerine.pce.model.util.BigDecimalConstants.INTERNAL_SCALE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.ROUNDING_MODE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

class DecimalUtilsTest {

  @Test
  void divide_bigDecimalDividedProperly() {
    // SETUP
    BigDecimal v1 = BigDecimal.valueOf(0.3);
    BigDecimal two = BigDecimal.valueOf(2);

    // ACT
    BigDecimal actual = DecimalUtils.divide(v1, two);

    // VERIFY
    Assertions.assertEquals(v1.divide(two, INTERNAL_SCALE, ROUNDING_MODE), actual);
  }

  @Test
  void divide_doubleDividedProperly() {
    // SETUP
    double v1Double = 0.3;
    BigDecimal two = BigDecimal.valueOf(2);

    // ACT
    BigDecimal actual = DecimalUtils.divide(v1Double, two);

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(v1Double).divide(two, INTERNAL_SCALE, ROUNDING_MODE), actual);
  }

  @Test
  void divide_rightDoubleDividedProperly() {
    // SETUP
    double v1Double = 0.3;
    BigDecimal two = BigDecimal.valueOf(2);

    // ACT
    BigDecimal actual = DecimalUtils.divide(two, v1Double);

    // VERIFY
    Assertions.assertEquals(two.divide(BigDecimal.valueOf(v1Double), INTERNAL_SCALE, ROUNDING_MODE), actual);
  }

  @Test
  void divide_allDoublesDividedProperly() {
    // SETUP
    double v1Double = 0.3;
    double two = 2.;

    // ACT
    BigDecimal actual = DecimalUtils.divide(two, v1Double);

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(two).divide(BigDecimal.valueOf(v1Double), INTERNAL_SCALE, ROUNDING_MODE),
        actual);
  }

  @Test
  void squareRoot_checkResult() {
    // SETUP
    BigDecimal v1 = new BigDecimal("0.012345");

    // ACT
    BigDecimal actual = DecimalUtils.squareRoot(v1);

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(0.111108055513541), actual);
  }

  @Test
  void squareRoot_checkResult1() {
    // SETUP
    BigDecimal v1 = new BigDecimal("0.05");

    // ACT
    BigDecimal actual = DecimalUtils.squareRoot(v1);

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(0.223606797749979), actual);
  }

  @Test
  void squareRoot_checkResult2() {
    // SETUP
    BigDecimal value = BigDecimal.valueOf(3);

    // ACT
    BigDecimal actual = DecimalUtils.squareRoot(value);

    // VERIFY
    Assertions.assertEquals(toUserScale(BigDecimal.valueOf(1.7320508076)), toUserScale(actual));
  }

  @Test
  void squareRoot_checkArithmeticException() {
    // SETUP
    BigDecimal v1 = new BigDecimal("-1");

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
    var v1 = new BigDecimal("0.1234567890123456789");

    // ACT
    BigDecimal actual = DecimalUtils.setInternalScale(v1, RoundingMode.FLOOR);

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
    var map = Map.of(
        LocalDate.now().minusMonths(5), BigDecimalConstants.HUNDRED,
        LocalDate.now().minusMonths(3), BigDecimalConstants.TWELVE,
        LocalDate.now().minusMonths(6), BigDecimalConstants.TEN_THOUSAND,
        LocalDate.now().minusMonths(1), ZERO,
        LocalDate.now().minusMonths(2), BigDecimalConstants.ONE);

    // ACT
    BigDecimal actual = DecimalUtils.getMinValue(map);

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
    var map = Map.of(
        LocalDate.now().minusMonths(5), BigDecimalConstants.HUNDRED,
        LocalDate.now().minusMonths(3), BigDecimalConstants.TWELVE,
        LocalDate.now().minusMonths(6), BigDecimalConstants.TEN_THOUSAND,
        LocalDate.now().minusMonths(1), ZERO,
        LocalDate.now().minusMonths(2), BigDecimalConstants.ONE);

    // ACT
    BigDecimal actual = DecimalUtils.getMaxValue(map);

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
    var v1 = new BigDecimal("-0.23123123132");

    // ACT
    BigDecimal actual = DecimalUtils.abs(v1);

    // VERIFY
    Assertions.assertEquals(new BigDecimal("0.23123123132"), actual);
  }

  @Test
  void pow_checkResult() {
    // SETUP
    double v1 = 15.23141;
    double v2 = 12.57681;
    double expectedPow = Math.pow(v1, v2);

    // ACT
    BigDecimal givenPow = pow(BigDecimal.valueOf(v1), BigDecimal.valueOf(v2));

    // VERIFY
    Assertions.assertEquals(BigDecimal.valueOf(expectedPow), givenPow);
  }

  @Test
  void annualizedReturn_usesCentralPowerPolicy() {
    // SETUP
    BigDecimal returnFactorProduct = new BigDecimal("1.123456789012345");
    BigDecimal exponent = new BigDecimal("0.5");

    // ACT
    BigDecimal actual = DecimalUtils.annualizedReturn(returnFactorProduct, exponent);

    // VERIFY
    Assertions.assertEquals(pow(returnFactorProduct, exponent).subtract(ONE), actual);
  }

  @Test
  void toUserScale_checkResult() {
    // SETUP
    var v = Map.of("", new BigDecimal("0.1234567890123456789"));

    // ACT
    Map<String, BigDecimal> givenWithUserScale = toUserScale(v);

    // VERIFY
    Assertions.assertEquals(Map.of("", new BigDecimal("0.1234567890")), givenWithUserScale);
  }

  @Test
  void toUserScale_whenMapIsNull() {
    // ACT
    Map<String, BigDecimal> givenWithUserScale = toUserScale((Map<String, BigDecimal>) null);

    // VERIFY
    Assertions.assertNull(givenWithUserScale);
  }

  @Test
  void toScale_checkResult() {
    // SETUP
    var v = new BigDecimal("0.1234567890123456789");

    // ACT
    BigDecimal givenWithScale = toScale(v, 5);

    // VERIFY
    Assertions.assertEquals(new BigDecimal("0.12346"), givenWithScale);
  }

  @Test
  void toScale_whenValueIsNull() {
    // ACT
    BigDecimal givenWithScale = toScale(null, 5);

    // VERIFY
    Assertions.assertNull(givenWithScale);
  }

  @Test
  void toScale_whenValueIsZero() {
    // ACT
    BigDecimal givenWithScale = toScale(ZERO, 5);

    // VERIFY
    Assertions.assertEquals(ZERO, givenWithScale);
  }

  @Test
  void toScale_whenValueIsOne() {
    // ACT
    BigDecimal givenWithScale = toScale(ONE, 5);

    // VERIFY
    Assertions.assertEquals(ONE, givenWithScale);
  }

}
