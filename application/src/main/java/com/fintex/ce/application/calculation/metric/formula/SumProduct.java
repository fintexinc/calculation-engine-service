package com.fintex.ce.application.calculation.metric.formula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.function.UnaryOperator;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;

/**
 * Calculating the sumproduct for two maps
 *
 * @param <E>
 */
public class SumProduct<E, M extends Map<LocalDate, BigDecimal>> {

  private final Map<E, M> map1;
  private final Map<E, M> map2;

  private UnaryOperator<LocalDate> func;

  public SumProduct(Map<E, M> map1, Map<E, M> map2) {
    this.map1 = map1;
    this.map2 = map2;
    this.func = date -> date;
  }

  public SumProduct<E, M> setMap2KeyFinder(final UnaryOperator<LocalDate> func) {
    this.func = func;
    return this;
  }

  public NavigableMap<LocalDate, BigDecimal> calculate() {
    final Map<LocalDate, BigDecimal> row = map1.values().stream().findFirst().orElseThrow();
    final Set<E> keys = map1.keySet();
    return row.keySet().stream().collect(toTreeMap(e -> e, e -> calculateSumOf(keys, e)));
  }

  private BigDecimal calculateSumOf(final Set<E> keys, final LocalDate date) {
    return keys.stream().map(key -> multiplyValues(date, key)).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal multiplyValues(final LocalDate date, final E key) {
    final BigDecimal v1 = map1.get(key).get(date);
    final BigDecimal v2 = map2.get(key).get(func.apply(date));
    return v1.multiply(v2);
  }

}
