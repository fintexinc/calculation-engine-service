package com.fintex.ce.util;

import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.Warning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.fintex.ce.model.error.ErrorCode.WRN_UNKNOWN_001;
import static com.fintex.ce.util.CollectorUtils.toMap;

/**
 * Pure utility methods for mapping raw security data into typed allocation maps.
 *
 * <p>
 * Three overloads handle different mapping scenarios:
 * <ul>
 * <li>{@link #mapToAllocations(Map, Function, Function, Map, ErrorCode, String, BiConsumer)} — maps string-keyed raw
 * values to enum-keyed allocations with a custom accumulator (e.g., for maturity allocations that merge multiple raw
 * keys into one display type).</li>
 * <li>{@link #mapToAllocations(Map, Function, Function, Map, ErrorCode, String)} — simplified version using
 * {@code Map.put} as the default accumulator (e.g., for equity market cap where each raw key maps 1:1 to an enum
 * value).</li>
 * <li>{@link #mapTypedAllocations(Map, Function, Map, ErrorCode)} — for domain models where values are already
 * enum-keyed and need no string resolution (e.g., for fixed income sector allocations mapped by the SM REST
 * fetcher).</li>
 * </ul>
 *
 * <p>
 * All methods return an {@link ExposureDataHolder} containing both the mapped allocations and any warnings produced
 * during mapping, keeping the functions pure and side-effect free.
 */
public final class AllocationMappingUtils {

  private AllocationMappingUtils() {
  }

  /**
   * Maps string-keyed raw values to enum-keyed allocations with a custom accumulator. Use when multiple raw keys can
   * map to the same enum value and need custom merging logic.
   */
  public static <E extends Enum<E>, D> ExposureDataHolder<E> mapToAllocations(
      Map<Holding, D> rawData,
      Function<D, Map<String, BigDecimal>> valueExtractor,
      Function<String, E> typeResolver,
      Map<E, BigDecimal> defaultMap,
      ErrorCode emptyWarningCode,
      String fdsServiceName,
      BiConsumer<Map<E, BigDecimal>, Map.Entry<E, BigDecimal>> accumulator) {
    List<Warning> warnings = new ArrayList<>();
    Map<Holding, Map<E, BigDecimal>> allocations = rawData.entrySet().stream()
        .collect(toMap(
            Map.Entry::getKey,
            entry -> mapStringKeyedAllocations(
                entry.getKey(), valueExtractor.apply(entry.getValue()),
                typeResolver, defaultMap, emptyWarningCode, fdsServiceName, accumulator, warnings)));
    return new ExposureDataHolder<>(allocations, warnings);
  }

  /**
   * Maps string-keyed raw values to enum-keyed allocations using simple put semantics. Use when each raw key maps 1:1
   * to an enum value with no merging needed.
   */
  public static <E extends Enum<E>, D> ExposureDataHolder<E> mapToAllocations(
      Map<Holding, D> rawData,
      Function<D, Map<String, BigDecimal>> valueExtractor,
      Function<String, E> typeResolver,
      Map<E, BigDecimal> defaultMap,
      ErrorCode emptyWarningCode,
      String fdsServiceName) {
    return mapToAllocations(rawData, valueExtractor, typeResolver, defaultMap,
        emptyWarningCode, fdsServiceName,
        (map, entry) -> map.put(entry.getKey(), entry.getValue()));
  }

  /**
   * Maps already enum-keyed values to allocations with no string resolution needed. Use when the domain model already
   * contains typed enum keys (e.g., from SM REST fetcher mappers).
   */
  public static <E extends Enum<E>, D> ExposureDataHolder<E> mapTypedAllocations(
      Map<Holding, D> rawData,
      Function<D, Map<E, BigDecimal>> valueExtractor,
      Map<E, BigDecimal> defaultMap,
      ErrorCode emptyWarningCode) {
    List<Warning> warnings = new ArrayList<>();
    Map<Holding, Map<E, BigDecimal>> allocations = rawData.entrySet().stream()
        .collect(toMap(
            Map.Entry::getKey,
            entry -> {
              Map<E, BigDecimal> rawValues = valueExtractor.apply(entry.getValue());
              if (rawValues == null || rawValues.isEmpty()) {
                warnings.add(emptyWarningCode.warning(entry.getKey()));
                return new HashMap<>(defaultMap);
              }
              Map<E, BigDecimal> mapped = new HashMap<>(defaultMap);
              mapped.putAll(rawValues);
              return mapped;
            }));
    return new ExposureDataHolder<>(allocations, warnings);
  }

  private static <E extends Enum<E>> Map<E, BigDecimal> mapStringKeyedAllocations(
      Holding holding,
      Map<String, BigDecimal> rawValues,
      Function<String, E> typeResolver,
      Map<E, BigDecimal> defaultMap,
      ErrorCode emptyWarningCode,
      String fdsServiceName,
      BiConsumer<Map<E, BigDecimal>, Map.Entry<E, BigDecimal>> accumulator,
      List<Warning> warnings) {
    if (rawValues == null || rawValues.isEmpty()) {
      warnings.add(emptyWarningCode.warning(holding));
      return new HashMap<>(defaultMap);
    }
    Map<E, BigDecimal> resolved = rawValues.entrySet().stream()
        .map(entry -> {
          E type = typeResolver.apply(entry.getKey());
          if (type == null) {
            warnings.add(WRN_UNKNOWN_001.warning(holding, entry.getKey(), fdsServiceName));
          }
          return type != null ? Map.entry(type, entry.getValue()) : null;
        })
        .filter(Objects::nonNull)
        .collect(HashMap::new,
            accumulator::accept,
            HashMap::putAll);
    Map<E, BigDecimal> result = new HashMap<>(defaultMap);
    result.putAll(resolved);
    return result;
  }
}