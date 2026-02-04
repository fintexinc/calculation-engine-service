package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.RHistoricalNavPrices;
import com.fintex.ce.domain.model.HistoricalNavPrices;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeMap;

@Component
public class HistoricalNavPricesMapper implements CacheEntityMapper<HistoricalNavPrices, RHistoricalNavPrices> {

  @Override
  public Optional<HistoricalNavPrices> toDomain(RHistoricalNavPrices entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          HistoricalNavPrices domain = new HistoricalNavPrices();
          domain.setCurrency(e.getCurrency());
          domain.setHoldingType(e.getHoldingType());
          domain.setReturns(e.getReturns() != null ? new TreeMap<>(e.getReturns()) : null);
          domain.setMissedMonthData(e.getMissedMonthData() != null
              ? new ArrayList<>(e.getMissedMonthData())
              : new ArrayList<>());
          domain.setMissedDates(e.getMissedDates() != null ? new ArrayList<>(e.getMissedDates()) : new ArrayList<>());
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RHistoricalNavPrices> toEntity(HistoricalNavPrices domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RHistoricalNavPrices entity = new RHistoricalNavPrices();
          entity.setCurrency(d.getCurrency());
          entity.setHoldingType(d.getHoldingType());
          if (d.getReturns() != null) {
            entity.setReturns(new TreeMap<>(d.getReturns()));
          }
          if (d.getMissedMonthData() != null) {
            entity.setMissedMonthData(new ArrayList<>(d.getMissedMonthData()));
          }
          if (d.getMissedDates() != null) {
            entity.setMissedDates(new ArrayList<>(d.getMissedDates()));
          }
          return entity;
        });
  }

}
