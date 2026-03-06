package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.RFxRates;
import com.fintex.ce.domain.model.FxRates;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.smclient.dto.FxRatesDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FxRatesMapper implements CacheEntityMapper<FxRates, RFxRates> {

  @Override
  public Optional<FxRates> toDomain(RFxRates entity) {
    return Optional.ofNullable(entity)
        .map(e -> new FxRates(mapFxRatesToDomain(e.getFxRates())));
  }

  @Override
  public Optional<RFxRates> toEntity(FxRates domain) {
    return Optional.ofNullable(domain)
        .map(d -> new RFxRates(mapFxRatesToEntity(d.getFxRates())));
  }

  private Map<LocalDate, FxRates.FxRate> mapFxRatesToDomain(Map<LocalDate, FxRatesDTO> entityMap) {
    if (entityMap == null) {
      return null;
    }
    return entityMap.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> new FxRates.FxRate(
                e.getValue().getUsdCad(),
                e.getValue().getCadUsd()),
            (a, b) -> a,
            HashMap::new));
  }

  private Map<LocalDate, FxRatesDTO> mapFxRatesToEntity(Map<LocalDate, FxRates.FxRate> domainMap) {
    if (domainMap == null) {
      return null;
    }
    return domainMap.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> new FxRatesDTO(e.getValue().getUsdCad(), e.getValue().getCadUsd()),
            (a, b) -> a,
            HashMap::new));
  }

}
