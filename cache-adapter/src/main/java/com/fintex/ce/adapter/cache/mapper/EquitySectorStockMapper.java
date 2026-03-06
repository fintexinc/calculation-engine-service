package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.equitysector.REquitySectorStock;
import com.fintex.ce.domain.model.EquitySectorStock;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EquitySectorStockMapper implements CacheEntityMapper<EquitySectorStock, REquitySectorStock> {

  @Override
  public Optional<EquitySectorStock> toDomain(REquitySectorStock entity) {
    return Optional.ofNullable(entity)
        .map(e -> new EquitySectorStock(e.getSectorName()));
  }

  @Override
  public Optional<REquitySectorStock> toEntity(EquitySectorStock domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          REquitySectorStock entity = new REquitySectorStock();
          entity.setSectorName(d.getSectorName());
          return entity;
        });
  }

}
