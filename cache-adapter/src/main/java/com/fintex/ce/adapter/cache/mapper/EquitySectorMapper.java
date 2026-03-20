package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.equitysector.REquitySector;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@Component
public class EquitySectorMapper implements CacheEntityMapper<EquitySector, REquitySector> {

  @Override
  public Optional<EquitySector> toDomain(REquitySector entity) {
    return Optional.ofNullable(entity)
        .map(e -> new EquitySector(
            e.getAllocations() != null ? toEnumMap(e.getAllocations()) : null));
  }

  @Override
  public Optional<REquitySector> toEntity(EquitySector domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          REquitySector entity = new REquitySector();
          if (d.getAllocations() != null) {
            entity.setAllocations(toStringMap(d.getAllocations()));
          }
          return entity;
        });
  }

  private Map<EquitySectorAllocationType, BigDecimal> toEnumMap(Map<String, BigDecimal> stringMap) {
    Map<EquitySectorAllocationType, BigDecimal> result = new EnumMap<>(EquitySectorAllocationType.class);
    stringMap.forEach((key, value) -> {
      try {
        result.put(EquitySectorAllocationType.valueOf(key), value);
      } catch (IllegalArgumentException ignored) {
        // Skip unknown sector types
      }
    });
    return result;
  }

  private Map<String, BigDecimal> toStringMap(Map<EquitySectorAllocationType, BigDecimal> enumMap) {
    return enumMap.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
  }

}
