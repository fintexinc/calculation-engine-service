package com.fintex.ce.application.mapper;

import com.fintex.ce.adapter.cache.entity.RIncomeForecast;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.smclient.graphql.PaymentFrequencyType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;

@Component
public class IncomeForecastMapper implements CacheEntityMapper<IncomeForecast, RIncomeForecast> {

  @Override
  public Optional<IncomeForecast> toDomain(RIncomeForecast entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          IncomeForecast domain = new IncomeForecast();
          domain.setDividendYield(e.getDividendYield());
          domain.setPaymentFrequencyType(e.getPaymentFrequencyType() != null
              ? e.getPaymentFrequencyType().name()
              : null);
          domain.setSchedule(e.getSchedule() != null ? new ArrayList<>(e.getSchedule()) : null);
          domain.setMaturityDate(e.getMaturityDate());
          domain.setIssueDate(e.getIssueDate());
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RIncomeForecast> toEntity(IncomeForecast domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RIncomeForecast entity = new RIncomeForecast();
          entity.setDividendYield(d.getDividendYield());
          if (d.getPaymentFrequencyType() != null) {
            entity.setPaymentFrequencyType(PaymentFrequencyType.valueOf(d.getPaymentFrequencyType()));
          }
          if (d.getSchedule() != null) {
            entity.setSchedule(new ArrayList<>(d.getSchedule()));
          }
          entity.setMaturityDate(d.getMaturityDate());
          entity.setIssueDate(d.getIssueDate());
          return entity;
        });
  }

}
