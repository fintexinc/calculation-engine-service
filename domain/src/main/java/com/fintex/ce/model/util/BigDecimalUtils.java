package com.fintex.ce.model.util;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.model.util.BigDecimalConstants.INVERSE_SCALE;
import static com.fintex.ce.model.util.BigDecimalConstants.ONE;
import static com.fintex.ce.model.util.BigDecimalConstants.ROUNDING_MODE;

@UtilityClass
public final class BigDecimalUtils {

  public static boolean bigDecimalEquals(BigDecimal a, BigDecimal b) {
    if (a == null) return b == null;
    if (b == null) return false;
    return a.compareTo(b) == 0;
  }

  public static int bigDecimalHashCode(BigDecimal value) {
    return value != null ? value.stripTrailingZeros().hashCode() : 0;
  }

  /**
   * Returns {@code 1 / rate} at INVERSE_SCALE precision with ROUNDING_MODE rounding. Single source of truth for FX-rate
   * inversion so adapters that fall back to the inverse pair internally and caching layers that canonicalize on read
   * produce bit-identical output.
   */
  public static BigDecimal invert(BigDecimal rate) {
    return ONE.divide(rate, INVERSE_SCALE, ROUNDING_MODE);
  }

}
