package com.fintex.ce.port.mapper;

import com.fintex.ce.domain.model.holding.Holding;

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

/**
 * Generic mapper interface for converting between domain models and cache entities (Redis DTOs).
 *
 * @param <D>
 *          Domain model type
 * @param <E>
 *          Cache entity type (R* classes)
 */
public interface CacheEntityMapper<D, E> {

  /**
   * Converts a cache entity to a domain model.
   *
   * @param entity
   *          the cache entity
   * @return Optional containing the domain model, or empty if entity is null
   */
  Optional<D> toDomain(E entity);

  /**
   * Converts a domain model to a cache entity.
   *
   * @param domain
   *          the domain model
   * @return Optional containing the cache entity, or empty if domain is null
   */
  Optional<E> toEntity(D domain);

  /**
   * Converts a map of holdings to cache entities into a map of holdings to domain models.
   *
   * @param entityMap
   *          map of holdings to cache entities
   * @return map of holdings to domain models
   */
  default Map<Holding, D> toDomainMap(Map<Holding, E> entityMap) {
    return entityMap.entrySet().stream()
        .filter(e -> e.getValue() != null)
        .collect(toMap(
            Map.Entry::getKey,
            e -> toDomain(e.getValue()).orElse(null),
            (a, b) -> a));
  }

  /**
   * Converts a map of holdings to domain models into a map of holdings to cache entities.
   *
   * @param domainMap
   *          map of holdings to domain models
   * @return map of holdings to cache entities
   */
  default Map<Holding, E> toEntityMap(Map<Holding, D> domainMap) {
    return domainMap.entrySet().stream()
        .filter(e -> e.getValue() != null)
        .collect(toMap(
            Map.Entry::getKey,
            e -> toEntity(e.getValue()).orElse(null),
            (a, b) -> a));
  }

}
