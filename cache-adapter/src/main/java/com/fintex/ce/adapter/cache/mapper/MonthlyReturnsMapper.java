package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.RMonthlyReturns;
import com.fintex.ce.domain.model.MonthlyReturns;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.TreeMap;

@Component
public class MonthlyReturnsMapper implements CacheEntityMapper<MonthlyReturns, RMonthlyReturns> {

  @Override
  public Optional<MonthlyReturns> toDomain(RMonthlyReturns entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          MonthlyReturns domain = new MonthlyReturns();
          domain.setCurrency(e.getCurrency());
          domain.setHoldingType(e.getHoldingType());
          if (e.getReturns() != null) {
            domain.setReturns(new TreeMap<>(e.getReturns()));
          }
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RMonthlyReturns> toEntity(MonthlyReturns domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RMonthlyReturns entity = new RMonthlyReturns();
          entity.setCurrency(d.getCurrency());
          entity.setHoldingType(d.getHoldingType());
          if (d.getReturns() != null) {
            entity.setReturns(new TreeMap<>(d.getReturns()));
          }
          entity.setHoldingId(d.getHoldingId());
          entity.setProvider(d.getProvider());
          entity.setProviders(d.getProviders());
          return entity;
        });
  }

}
