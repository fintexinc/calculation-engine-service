package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.RCountryExposure;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class CountryExposureMapper implements CacheEntityMapper<CountryExposure, RCountryExposure> {

  @Override
  public Optional<CountryExposure> toDomain(RCountryExposure entity) {
    return Optional.ofNullable(entity)
        .map(e -> new CountryExposure(
            e.getHoldingType(),
            e.getAllocations() != null ? new HashMap<>(e.getAllocations()) : null));
  }

  @Override
  public Optional<RCountryExposure> toEntity(CountryExposure domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RCountryExposure entity = new RCountryExposure();
          entity.setHoldingType(d.getHoldingType());
          if (d.getAllocations() != null) {
            entity.setAllocations(new HashMap<>(d.getAllocations()));
          }
          return entity;
        });
  }

}
