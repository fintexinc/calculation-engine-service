package com.fintex.ce.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.DecimalUtils.divide;
import static java.math.BigDecimal.ZERO;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class CalculationUtils {

  private CalculationUtils() {
  }

  public static <K> BigDecimal sum(final Map<K, BigDecimal> m) {
    return m.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static <K> BigDecimal average(final Map<K, BigDecimal> map) {
    return divide(sum(map), map.size());
  }

  public static <K> BigDecimal product(final Map<K, BigDecimal> m) {
    return m.values().stream().reduce(BigDecimal.ONE, BigDecimal::multiply);
  }

  public static <K> BigDecimal sumProduct(final Map<K, BigDecimal> m1, final Map<K, BigDecimal> m2) {
    return m1.entrySet().stream()
        .map(e -> Objects.requireNonNull(m2.get(e.getKey())).multiply(e.getValue()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static <K> BigDecimal sumProduct(final Map<K, BigDecimal> m1, final Map<K, BigDecimal> m2,
      final Map<K, BigDecimal> m3) {
    return m1.entrySet().stream()
        .map(e -> e.getValue()
            .multiply(Objects.requireNonNull(m2.get(e.getKey())))
            .multiply(Objects.requireNonNull(m3.get(e.getKey()))))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public static <K> Map<K, BigDecimal> reScaleAbs(final Map<K, BigDecimal> map) {
    final BigDecimal productSum = map.values().stream().map(BigDecimal::abs).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (ZERO.compareTo(productSum) == 0) {
      return map;
    }
    return map.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> divide(e.getValue(), productSum)));
  }

  public static <K> Map<K, BigDecimal> reScale(final Map<K, BigDecimal> map) {
    final BigDecimal productSum = map.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    if (ZERO.compareTo(productSum) == 0) {
      return map;
    }
    return map.entrySet()
        .stream()
        .collect(toMap(
            Map.Entry::getKey,
            e -> divide(e.getValue(), productSum)));
  }

  public static boolean isNegativeNumeric(final String value) {
    return value.startsWith("-") && isNumeric(value.substring(1));
  }

}
