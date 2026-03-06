package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.RHistoricalDistributions;
import com.fintex.ce.domain.model.HistoricalDistributions;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class HistoricalDistributionsMapper
    implements
      CacheEntityMapper<HistoricalDistributions, RHistoricalDistributions> {

  @Override
  public Optional<HistoricalDistributions> toDomain(RHistoricalDistributions entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          HistoricalDistributions domain = new HistoricalDistributions();
          domain.setCurrency(e.getCurrency());
          domain.setHoldingType(e.getHoldingType());
          domain.setCapitalGains(mapCapitalGainsToDomain(e.getCapitalGains()));
          domain.setDistributions(mapDistributionsToDomain(e.getDistributions()));
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RHistoricalDistributions> toEntity(HistoricalDistributions domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RHistoricalDistributions entity = new RHistoricalDistributions();
          entity.setCurrency(d.getCurrency());
          entity.setHoldingType(d.getHoldingType());
          entity.setCapitalGains(mapCapitalGainsToEntity(d.getCapitalGains()));
          entity.setDistributions(mapDistributionsToEntity(d.getDistributions()));
          return entity;
        });
  }

  private TreeMap<java.time.LocalDate, HistoricalDistributions.CapitalGainsDto> mapCapitalGainsToDomain(
      TreeMap<java.time.LocalDate, RHistoricalDistributions.CapitalGainsDto> entityMap) {
    if (entityMap == null) {
      return null;
    }
    return entityMap.entrySet().stream()
        .collect(Collectors.toMap(
            java.util.Map.Entry::getKey,
            e -> new HistoricalDistributions.CapitalGainsDto(
                e.getValue().getCapitalGains(),
                e.getValue().getReturnOfCapital()),
            (a, b) -> a,
            TreeMap::new));
  }

  private TreeMap<java.time.LocalDate, HistoricalDistributions.DistributionsDto> mapDistributionsToDomain(
      TreeMap<java.time.LocalDate, RHistoricalDistributions.DistributionsDto> entityMap) {
    if (entityMap == null) {
      return null;
    }
    return entityMap.entrySet().stream()
        .collect(Collectors.toMap(
            java.util.Map.Entry::getKey,
            e -> new HistoricalDistributions.DistributionsDto(
                e.getValue().getDomesticDividend(),
                e.getValue().getForeignDividend(),
                e.getValue().getInterestIncome()),
            (a, b) -> a,
            TreeMap::new));
  }

  private TreeMap<java.time.LocalDate, RHistoricalDistributions.CapitalGainsDto> mapCapitalGainsToEntity(
      TreeMap<java.time.LocalDate, HistoricalDistributions.CapitalGainsDto> domainMap) {
    if (domainMap == null) {
      return null;
    }
    return domainMap.entrySet().stream()
        .collect(Collectors.toMap(
            java.util.Map.Entry::getKey,
            e -> new RHistoricalDistributions.CapitalGainsDto(
                e.getValue().getCapitalGains(),
                e.getValue().getReturnOfCapital()),
            (a, b) -> a,
            TreeMap::new));
  }

  private TreeMap<java.time.LocalDate, RHistoricalDistributions.DistributionsDto> mapDistributionsToEntity(
      TreeMap<java.time.LocalDate, HistoricalDistributions.DistributionsDto> domainMap) {
    if (domainMap == null) {
      return null;
    }
    return domainMap.entrySet().stream()
        .collect(Collectors.toMap(
            java.util.Map.Entry::getKey,
            e -> new RHistoricalDistributions.DistributionsDto(
                e.getValue().getDomesticDividend(),
                e.getValue().getForeignDividend(),
                e.getValue().getInterestIncome()),
            (a, b) -> a,
            TreeMap::new));
  }

}
