package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.rating.CreditQualityRatingType;
import com.fintex.wm.commons.domain.rating.CreditQualityRatings;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CreditQualityMapper
    implements
      SecurityMasterResponseMapper<CreditQuality, CreditQualityRatings> {

  private static final Map<String, CreditQualityRatingType> RATING_TYPE_LOOKUP = Arrays
      .stream(CreditQualityRatingType.values())
      .collect(Collectors.toMap(e -> e.name().toUpperCase(), e -> e));

  @Override
  public CreditQuality map(CreditQualityRatings smsResponse, PortfolioHolding holding) {
    Map<CreditQualityRatingType, BigDecimal> ratings = Optional.ofNullable(smsResponse)
        .map(CreditQualityRatings::getRatings)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getRating() != null && entry.getValue() != null)
        .map(entry -> {
          CreditQualityRatingType type = RATING_TYPE_LOOKUP.get(entry.getRating().toUpperCase());
          return type != null ? new AbstractMap.SimpleEntry<>(type, entry.getValue()) : null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (existing, replacement) -> existing,
            () -> new EnumMap<>(CreditQualityRatingType.class)));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(CreditQualityRatings::getDataProvider)
        .map(List::of)
        .orElseGet(List::of);

    return CreditQuality.builder()
        .ratings(ratings)
        .providers(providers)
        .build();
  }
}