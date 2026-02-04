package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.REquityStyleboxExposure;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class EquityStyleboxExposureMapper
    implements
      CacheEntityMapper<EquityStyleboxExposure, REquityStyleboxExposure> {

  @Override
  public Optional<EquityStyleboxExposure> toDomain(REquityStyleboxExposure entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          EquityStyleboxExposure domain = new EquityStyleboxExposure();
          domain.setHoldingType(e.getHoldingType());
          domain.setBoxValues(e.getBoxValues() != null ? new HashMap<>(e.getBoxValues()) : null);
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<REquityStyleboxExposure> toEntity(EquityStyleboxExposure domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          REquityStyleboxExposure entity = new REquityStyleboxExposure();
          entity.setHoldingType(d.getHoldingType());
          if (d.getBoxValues() != null) {
            entity.setBoxValues(new HashMap<>(d.getBoxValues()));
          }
          return entity;
        });
  }

}
