package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.topcommonholdings.RCommonHoldingsStock;
import com.fintex.ce.domain.model.CommonHoldingsStock;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommonHoldingsStockMapper implements CacheEntityMapper<CommonHoldingsStock, RCommonHoldingsStock> {

  @Override
  public Optional<CommonHoldingsStock> toDomain(RCommonHoldingsStock entity) {
    return Optional.ofNullable(entity)
        .map(e -> new CommonHoldingsStock(
            e.getCompanyName(),
            e.getTicker(),
            e.getExchangeCode()));
  }

  @Override
  public Optional<RCommonHoldingsStock> toEntity(CommonHoldingsStock domain) {
    return Optional.ofNullable(domain)
        .map(d -> new RCommonHoldingsStock(
            d.getCompanyName(),
            d.getTicker(),
            d.getExchangeCode()));
  }

}
