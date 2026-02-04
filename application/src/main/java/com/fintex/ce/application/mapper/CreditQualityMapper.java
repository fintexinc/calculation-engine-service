package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.RCreditQuality;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class CreditQualityMapper implements CacheEntityMapper<CreditQuality, RCreditQuality> {

  @Override
  public Optional<CreditQuality> toDomain(RCreditQuality entity) {
    return Optional.ofNullable(entity)
        .map(e -> new CreditQuality(
            e.getHoldingType(),
            e.getRatings() != null ? new HashMap<>(e.getRatings()) : null));
  }

  @Override
  public Optional<RCreditQuality> toEntity(CreditQuality domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RCreditQuality entity = new RCreditQuality();
          entity.setHoldingType(d.getHoldingType());
          if (d.getRatings() != null) {
            entity.setRatings(new HashMap<>(d.getRatings()));
          }
          return entity;
        });
  }

}
