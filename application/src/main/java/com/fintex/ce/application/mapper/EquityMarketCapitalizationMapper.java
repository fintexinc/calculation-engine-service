package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class EquityMarketCapitalizationMapper
    implements
      CacheEntityMapper<EquityMarketCapitalization, REquityMarketCapitalization> {

  @Override
  public Optional<EquityMarketCapitalization> toDomain(REquityMarketCapitalization entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          EquityMarketCapitalization domain = new EquityMarketCapitalization();
          domain.setHoldingType(e.getHoldingType());
          domain.setRatings(e.getRatings() != null ? new HashMap<>(e.getRatings()) : null);
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<REquityMarketCapitalization> toEntity(EquityMarketCapitalization domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          REquityMarketCapitalization entity = new REquityMarketCapitalization();
          entity.setHoldingType(d.getHoldingType());
          if (d.getRatings() != null) {
            entity.setRatings(new HashMap<>(d.getRatings()));
          }
          return entity;
        });
  }

}
