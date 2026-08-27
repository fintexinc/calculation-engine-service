package ca.tangerine.pce.model.util;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

import static ca.tangerine.pce.model.util.BigDecimalConstants.HUNDRED;
import static ca.tangerine.pce.model.util.BigDecimalConstants.INVERSE_SCALE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.MATH_CONTEXT;
import static ca.tangerine.pce.model.util.BigDecimalConstants.ONE;
import static ca.tangerine.pce.model.util.BigDecimalConstants.ROUNDING_MODE;

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

  /**
   * Converts a percentage-form value (e.g. {@code 1.51} meaning 1.51%) to ratio form ({@code 0.0151}). Returns
   * {@code null} when the input is {@code null}. Used at adapter boundaries where upstream data providers report fees
   * or rates as percentages and the engine needs them as ratios.
   */
  public static BigDecimal percentageToRatio(BigDecimal percentage) {
    return percentage == null ? null : percentage.divide(HUNDRED, MATH_CONTEXT);
  }

}
