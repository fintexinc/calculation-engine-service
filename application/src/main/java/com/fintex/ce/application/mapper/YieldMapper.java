package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.RYield;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class YieldMapper implements CacheEntityMapper<Yield, RYield> {

  @Override
  public Optional<Yield> toDomain(RYield entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          Yield domain = new Yield();
          domain.setDividendYield(e.getDividendYield());
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          // Errors are transient and not stored in Redis, so no conversion needed
          return domain;
        });
  }

  @Override
  public Optional<RYield> toEntity(Yield domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RYield entity = new RYield();
          entity.setDividendYield(d.getDividendYield());
          entity.setHoldingId(d.getHoldingId());
          entity.setProvider(d.getProvider());
          entity.setProviders(d.getProviders());
          // Errors are transient and not stored in Redis, so no conversion needed
          return entity;
        });
  }

}
