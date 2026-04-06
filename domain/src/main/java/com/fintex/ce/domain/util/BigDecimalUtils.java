package com.fintex.ce.domain.util;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

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

}
