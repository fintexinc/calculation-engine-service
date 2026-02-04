package com.fintex.ce.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectorUtils {
  private CollectorUtils() {
  }

  public static <T, K> Collector<Map.Entry<T, K>, ?, LinkedHashMap<T, K>> toLinkedHashMap() {
    return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o1, o2) -> o1, LinkedHashMap::new);
  }

  public static <T, K, U> Collector<T, ?, TreeMap<K, U>> toTreeMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collectors.toMap(keyMapper, valueMapper, (o1, o2) -> o1, TreeMap::new);
  }

  public static <K, V> Collector<Map.Entry<K, V>, ?, TreeMap<K, V>> toTreeMap() {
    return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, TreeMap::new);
  }

  public static <K, V> Collector<Map.Entry<K, V>, ?, HashMap<K, V>> toMap() {
    return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, HashMap::new);
  }

  public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(
      Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
    return Collectors.toMap(keyMapper, valueMapper, (o1, o2) -> o1, HashMap::new);
  }

}
