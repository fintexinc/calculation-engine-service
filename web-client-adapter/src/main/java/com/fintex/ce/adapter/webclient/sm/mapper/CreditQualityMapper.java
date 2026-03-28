package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.calculation.CreditQualityRating;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.rating.CreditQualityRatings;
import com.fintex.sm.model.domain.value.CreditQualityRatingTypeValue;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CreditQualityMapper
    implements SecurityMasterResponseMapper<CreditQuality, CreditQualityRatings> {

  @Override
  public CreditQuality map(CreditQualityRatings smsResponse, Holding holding) {
    Map<CreditQualityRating, BigDecimal> ratings = Optional.ofNullable(smsResponse)
        .map(CreditQualityRatings::getRatings)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getRating() != null && entry.getValue() != null)
        .filter(entry -> CreditQualityRating.fromValue(entry.getRating()) != null)
        .collect(Collectors.toMap(
            entry -> CreditQualityRating.fromValue(entry.getRating()),
            CreditQualityRatingTypeValue::getValue,
            (existing, replacement) -> existing,
            () -> new EnumMap<>(CreditQualityRating.class)));

    CreditQuality result = new CreditQuality()
        .setRatings(ratings)
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(CreditQualityRatings::getDataProvider)
        .ifPresent(dp -> result.setProvider(dp.name()));

    return result;
  }
}