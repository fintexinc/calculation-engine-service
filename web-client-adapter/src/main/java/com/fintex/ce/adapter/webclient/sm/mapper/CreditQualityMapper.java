package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.CreditQualityRatingType;
import com.fintex.sm.model.domain.rating.CreditQualityRatings;

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
  public CreditQuality map(CreditQualityRatings smsResponse, Holding holding) {
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

    CreditQuality result = new CreditQuality()
        .setRatings(ratings)
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(CreditQualityRatings::getDataProvider)
        .ifPresent(dp -> result.setProvider(dp.name()));

    return result;
  }
}