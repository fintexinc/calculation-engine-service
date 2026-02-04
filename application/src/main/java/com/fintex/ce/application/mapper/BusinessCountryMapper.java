package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.RBusinessCountry;
import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BusinessCountryMapper implements CacheEntityMapper<BusinessCountry, RBusinessCountry> {

  @Override
  public Optional<BusinessCountry> toDomain(RBusinessCountry entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          BusinessCountry domain = new BusinessCountry();
          domain.setValue(e.getValue());
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RBusinessCountry> toEntity(BusinessCountry domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RBusinessCountry entity = new RBusinessCountry();
          entity.setValue(d.getValue());
          return entity;
        });
  }

}
