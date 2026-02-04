package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.equitysector.REquitySector;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class EquitySectorMapper implements CacheEntityMapper<EquitySector, REquitySector> {

  @Override
  public Optional<EquitySector> toDomain(REquitySector entity) {
    return Optional.ofNullable(entity)
        .map(e -> new EquitySector(
            e.getAllocations() != null ? new HashMap<>(e.getAllocations()) : null));
  }

  @Override
  public Optional<REquitySector> toEntity(EquitySector domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          REquitySector entity = new REquitySector();
          if (d.getAllocations() != null) {
            entity.setAllocations(new HashMap<>(d.getAllocations()));
          }
          return entity;
        });
  }

}
