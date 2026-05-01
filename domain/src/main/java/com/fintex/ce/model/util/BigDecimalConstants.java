package com.fintex.ce.model.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import lombok.experimental.UtilityClass;

/**
 * Shared {@link BigDecimal} value constants and decimal-precision settings used across the calculation engine. Keeps
 * scale, rounding-mode and {@link MathContext} configuration in a single place so that arithmetic, output formatting
 * and FX-rate inversion stay consistent and bit-identical across modules.
 */
@UtilityClass
public class BigDecimalConstants {

  public static final BigDecimal ONE = BigDecimal.valueOf(1);
  public static final BigDecimal TWO = BigDecimal.valueOf(2);
  public static final BigDecimal TWELVE = BigDecimal.valueOf(12);
  public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  public static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);

  public static final int INTERNAL_SCALE = 15;
  public static final int OUTPUT_SCALE = 10;
  public static final int INVERSE_SCALE = 10;
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
  public static final MathContext MATH_CONTEXT = new MathContext(INTERNAL_SCALE);

}
