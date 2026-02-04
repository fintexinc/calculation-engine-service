package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.REquityCountryAllocation;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class EquityCountryAllocationMapper
    implements
      CacheEntityMapper<EquityCountryAllocation, REquityCountryAllocation> {

  @Override
  public Optional<EquityCountryAllocation> toDomain(REquityCountryAllocation entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          EquityCountryAllocation domain = new EquityCountryAllocation();
          domain.setHoldingType(e.getHoldingType());
          domain.setAllocations(e.getAllocations() != null ? new HashMap<>(e.getAllocations()) : null);
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<REquityCountryAllocation> toEntity(EquityCountryAllocation domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          REquityCountryAllocation entity = new REquityCountryAllocation();
          entity.setHoldingType(d.getHoldingType());
          if (d.getAllocations() != null) {
            entity.setAllocations(new HashMap<>(d.getAllocations()));
          }
          return entity;
        });
  }

}
