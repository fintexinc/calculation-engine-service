package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.RMaturityAllocation;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class MaturityAllocationMapper implements CacheEntityMapper<MaturityAllocation, RMaturityAllocation> {

  @Override
  public Optional<MaturityAllocation> toDomain(RMaturityAllocation entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          MaturityAllocation domain = new MaturityAllocation();
          domain.setHoldingType(e.getHoldingType());
          if (e.getMaturityDurationValues() != null) {
            domain.setMaturityDurationValues(new HashMap<>(e.getMaturityDurationValues()));
          }
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RMaturityAllocation> toEntity(MaturityAllocation domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RMaturityAllocation entity = new RMaturityAllocation();
          entity.setHoldingType(d.getHoldingType());
          if (d.getMaturityDurationValues() != null) {
            entity.setMaturityDurationValues(new HashMap<>(d.getMaturityDurationValues()));
          }
          return entity;
        });
  }

}
