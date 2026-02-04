package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.RTBills;
import com.fintex.ce.domain.model.TreasuryBills;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
public class TreasuryBillsMapper implements CacheEntityMapper<TreasuryBills, RTBills> {

  @Override
  public Optional<TreasuryBills> toDomain(RTBills entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          TreasuryBills domain = new TreasuryBills();
          domain.setCurrency(e.getCurrency());
          if (e.getMonthlyReturns() != null) {
            domain.setMonthlyReturns(new HashMap<>(e.getMonthlyReturns()));
          }
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RTBills> toEntity(TreasuryBills domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RTBills entity = new RTBills();
          entity.setCurrency(d.getCurrency());
          if (d.getMonthlyReturns() != null) {
            entity.setMonthlyReturns(new HashMap<>(d.getMonthlyReturns()));
          }
          return entity;
        });
  }

}
