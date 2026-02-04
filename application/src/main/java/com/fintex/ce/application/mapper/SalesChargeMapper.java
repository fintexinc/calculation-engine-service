package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.RSalesCharge;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SalesChargeMapper implements CacheEntityMapper<SalesCharge, RSalesCharge> {

  @Override
  public Optional<SalesCharge> toDomain(RSalesCharge entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          SalesCharge domain = new SalesCharge();
          domain.setValue(e.getValue());
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RSalesCharge> toEntity(SalesCharge domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RSalesCharge entity = new RSalesCharge();
          entity.setValue(d.getValue());
          return entity;
        });
  }

}
