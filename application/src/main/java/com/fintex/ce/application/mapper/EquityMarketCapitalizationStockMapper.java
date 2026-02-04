package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.equitymarketcapitalization.REquityMarketCapitalizationStock;
import com.fintex.ce.domain.model.EquityMarketCapitalizationStock;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EquityMarketCapitalizationStockMapper
    implements
      CacheEntityMapper<EquityMarketCapitalizationStock, REquityMarketCapitalizationStock> {

  @Override
  public Optional<EquityMarketCapitalizationStock> toDomain(REquityMarketCapitalizationStock entity) {
    return Optional.ofNullable(entity)
        .map(e -> new EquityMarketCapitalizationStock(e.getStyleBox()));
  }

  @Override
  public Optional<REquityMarketCapitalizationStock> toEntity(EquityMarketCapitalizationStock domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          REquityMarketCapitalizationStock entity = new REquityMarketCapitalizationStock();
          entity.setStyleBox(d.getStyleBox());
          return entity;
        });
  }

}
