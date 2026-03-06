package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.RClassificationAllocation;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class ClassificationAllocationMapper
    implements
      CacheEntityMapper<ClassificationAllocation, RClassificationAllocation> {

  @Override
  public Optional<ClassificationAllocation> toDomain(RClassificationAllocation entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          ClassificationAllocation domain = new ClassificationAllocation();
          domain.setHoldingType(e.getHoldingType());
          domain.setSecurityClassificationValues(e.getSecurityClassificationValues() != null
              ? new HashMap<>(e.getSecurityClassificationValues())
              : null);
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RClassificationAllocation> toEntity(ClassificationAllocation domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RClassificationAllocation entity = new RClassificationAllocation();
          entity.setHoldingType(d.getHoldingType());
          if (d.getSecurityClassificationValues() != null) {
            entity.setSecurityClassificationValues(new HashMap<>(d.getSecurityClassificationValues()));
          }
          return entity;
        });
  }

}
