package com.fintex.ce.util;

import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.holding.Holding;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_UNKNOWN_001;

public final class AllocationMappingUtils {

  private AllocationMappingUtils() {
  }

  public static <E extends Enum<E>, D> Map<Holding, Map<E, BigDecimal>> mapToAllocations(
      Map<Holding, D> rawData,
      Function<D, Map<String, BigDecimal>> valueExtractor,
      Function<String, E> typeResolver,
      Map<E, BigDecimal> defaultMap,
      ExceptionCode emptyWarningCode,
      String fdsServiceName,
      List<Warning> warnings,
      BiConsumer<Map<E, BigDecimal>, Map.Entry<E, BigDecimal>> accumulator) {
    Map<Holding, Map<E, BigDecimal>> result = new HashMap<>();
    rawData.forEach((holding, domainModel) -> {
      Map<String, BigDecimal> rawValues = valueExtractor.apply(domainModel);
      if (rawValues == null || rawValues.isEmpty()) {
        warnings.add(emptyWarningCode.warning(holding));
        result.put(holding, new HashMap<>(defaultMap));
        return;
      }
      Map<E, BigDecimal> mapped = new HashMap<>(defaultMap);
      rawValues.forEach((typeStr, value) -> {
        E type = typeResolver.apply(typeStr);
        if (type == null) {
          warnings.add(WRN_UNKNOWN_001.warning(holding, typeStr, fdsServiceName));
        } else {
          accumulator.accept(mapped, Map.entry(type, value));
        }
      });
      result.put(holding, mapped);
    });
    return result;
  }

  public static <E extends Enum<E>, D> Map<Holding, Map<E, BigDecimal>> mapToAllocations(
      Map<Holding, D> rawData,
      Function<D, Map<String, BigDecimal>> valueExtractor,
      Function<String, E> typeResolver,
      Map<E, BigDecimal> defaultMap,
      ExceptionCode emptyWarningCode,
      String fdsServiceName,
      List<Warning> warnings) {
    return mapToAllocations(rawData, valueExtractor, typeResolver, defaultMap,
        emptyWarningCode, fdsServiceName, warnings,
        (map, entry) -> map.put(entry.getKey(), entry.getValue()));
  }

}
