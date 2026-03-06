package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.RFixedIncomeBondSecurities;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class FixedIncomeBondSecuritiesMapper
    implements
      CacheEntityMapper<FixedIncomeBondSecurities, RFixedIncomeBondSecurities> {

  @Override
  public Optional<FixedIncomeBondSecurities> toDomain(RFixedIncomeBondSecurities entity) {
    return Optional.ofNullable(entity)
        .map(e -> new FixedIncomeBondSecurities(
            e.getHoldingType(),
            e.getFixedIncomeBondSectors() != null ? new HashMap<>(e.getFixedIncomeBondSectors()) : null));
  }

  @Override
  public Optional<RFixedIncomeBondSecurities> toEntity(FixedIncomeBondSecurities domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RFixedIncomeBondSecurities entity = new RFixedIncomeBondSecurities();
          entity.setHoldingType(d.getHoldingType());
          if (d.getFixedIncomeBondSectors() != null) {
            entity.setFixedIncomeBondSectors(new HashMap<>(d.getFixedIncomeBondSectors()));
          }
          return entity;
        });
  }

}
