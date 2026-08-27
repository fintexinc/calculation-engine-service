package ca.tangerine.pce.application.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

import static ca.tangerine.pce.application.util.CollectorUtils.toMap;
import static ca.tangerine.pce.model.util.BigDecimalConstants.INTERNAL_SCALE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.MATH_CONTEXT;
import static ca.tangerine.pce.model.util.BigDecimalConstants.OUTPUT_SCALE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.ROUNDING_MODE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

public class DecimalUtils {

  private DecimalUtils() {
  }

  public static BigDecimal divide(final BigDecimal v1, final BigDecimal v2) {
    return Objects.requireNonNull(v1).divide(Objects.requireNonNull(v2), INTERNAL_SCALE, ROUNDING_MODE);
  }

  public static BigDecimal pow(final BigDecimal v1, final BigDecimal v2) {
    double pow = Math.pow(Objects.requireNonNull(v1).doubleValue(), Objects.requireNonNull(v2).doubleValue());
    return BigDecimal.valueOf(pow);
  }

  /**
   * Project-wide power policy for fractional financial exponents. Keep the same double-backed precision as
   * {@link #pow(BigDecimal, BigDecimal)} and round only at response boundaries unless a metric explicitly requires
   * earlier user-scale rounding.
   */
  public static BigDecimal annualizedReturn(final BigDecimal returnFactorProduct, final BigDecimal exponent) {
    return pow(returnFactorProduct, exponent).subtract(ONE);
  }

  public static BigDecimal divide(final double v1, final BigDecimal v2) {
    return divide(BigDecimal.valueOf(v1), v2);
  }

  public static BigDecimal divide(final BigDecimal v1, final double v2) {
    return divide(v1, BigDecimal.valueOf(v2));
  }

  public static BigDecimal divide(final double v1, final double v2) {
    return divide(BigDecimal.valueOf(v1), BigDecimal.valueOf(v2));
  }

  public static BigDecimal squareRoot(final BigDecimal v1) {
    return Objects.requireNonNull(v1).sqrt(MATH_CONTEXT);
  }

  public static BigDecimal toUserScale(final BigDecimal value) {
    return toScale(value, OUTPUT_SCALE);
  }

  public static BigDecimal toScale(final BigDecimal value, final int scale) {
    if (value == null) {
      return null;
    }
    if (ZERO.compareTo(value) == 0) {
      return ZERO;
    }
    if (ONE.compareTo(value) == 0) {
      return ONE;
    }
    return value.setScale(scale, ROUNDING_MODE);
  }

  public static <K> Map<K, BigDecimal> toUserScale(final Map<K, BigDecimal> map) {
    if (map == null) {
      return null;
    }
    return map.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> toUserScale(e.getValue())));
  }

  public static BigDecimal setInternalScale(final BigDecimal v, final RoundingMode roundingMode) {
    return Objects.requireNonNull(v).setScale(INTERNAL_SCALE, roundingMode);
  }

  public static <K> BigDecimal getMinValue(final Map<K, BigDecimal> map) {
    return Objects.requireNonNull(map.values()).stream().min(BigDecimal::compareTo).orElseThrow();
  }

  public static <K> BigDecimal getMaxValue(final Map<K, BigDecimal> map) {
    return Objects.requireNonNull(map.values()).stream().max(BigDecimal::compareTo).orElseThrow();
  }

  public static BigDecimal abs(final BigDecimal v) {
    return Objects.requireNonNull(v).abs();
  }
}
