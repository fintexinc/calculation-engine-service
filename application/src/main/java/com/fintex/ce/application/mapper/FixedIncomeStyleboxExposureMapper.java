package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.RFixedIncomeStyleboxExposure;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class FixedIncomeStyleboxExposureMapper
    implements
      CacheEntityMapper<FixedIncomeStyleboxExposure, RFixedIncomeStyleboxExposure> {

  @Override
  public Optional<FixedIncomeStyleboxExposure> toDomain(RFixedIncomeStyleboxExposure entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          FixedIncomeStyleboxExposure domain = new FixedIncomeStyleboxExposure();
          domain.setHoldingType(e.getHoldingType());
          domain.setBoxValues(e.getBoxValues() != null ? new HashMap<>(e.getBoxValues()) : null);
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RFixedIncomeStyleboxExposure> toEntity(FixedIncomeStyleboxExposure domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RFixedIncomeStyleboxExposure entity = new RFixedIncomeStyleboxExposure();
          entity.setHoldingType(d.getHoldingType());
          if (d.getBoxValues() != null) {
            entity.setBoxValues(new HashMap<>(d.getBoxValues()));
          }
          return entity;
        });
  }

}
