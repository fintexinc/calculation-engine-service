package com.fintex.ce.application.util;

import com.fintex.ce.model.domain.calculation.DateRange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public class MapUtils {

  private MapUtils() {
  }

  public static <K, V> Map<K, Map<LocalDate, V>> filterWithinRange(final DateRange dateRange,
      final Map<K, Map<LocalDate, V>> map) {
    if (dateRange.isUnbounded()) {
      return map;
    }
    return map.entrySet().stream().collect(CollectorUtils.toMap(Map.Entry::getKey, entry -> filterDatesWithinRange(
        dateRange, entry.getValue())));
  }

  public static <V> Map<LocalDate, V> filterDatesWithinRange(final DateRange dateRange,
      final Map<LocalDate, V> map) {
    return map.entrySet().stream().filter(entry -> dateRange.contains(entry.getKey())).collect(CollectorUtils
        .toTreeMap());
  }

  /**
   * Replaces {@param defaultMap} values with {@param userMap} values. New map will have the same size and the same keys
   * as {@param defaultMap} but will contain new values from {@param userMap} if exist
   *
   * @param defaultMap
   *          default values
   * @param userMap
   *          user values
   * @param <T>
   *          type
   * @return new map
   */
  public static <T> Map<T, BigDecimal> overrideDefaultValues(final Map<T, BigDecimal> defaultMap,
      final Map<T, BigDecimal> userMap) {
    if (userMap.isEmpty()) {
      return userMap;
    }
    final Map<T, BigDecimal> newMap = new HashMap<>(defaultMap);
    newMap.putAll(userMap);
    return newMap;
  }

  public static <K, V> Map<K, V> copy(final Map<K, V> originalMap, final Supplier<Map<K, V>> mapCreator) {
    final Map<K, V> newMap = mapCreator.get();
    if (originalMap != null && !originalMap.isEmpty()) {
      newMap.putAll(originalMap);
    }
    return newMap;
  }

  public static <K, V> TreeMap<K, V> copyTreeMap(final Map<K, V> originalMap,
      final Supplier<TreeMap<K, V>> mapCreator) {
    final TreeMap<K, V> newMap = mapCreator.get();
    if (originalMap != null && !originalMap.isEmpty()) {
      newMap.putAll(originalMap);
    }
    return newMap;
  }
}
