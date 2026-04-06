package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.HoldingEquityMarketCap;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.datapoint.EquityMarketCapitalization;
import com.fintex.sm.model.domain.enumeration.EquityMarketCapitalizationType;
import com.fintex.sm.model.domain.value.EquityMarketCapitalizationTypeValue;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EquityMarketCapitalizationMapper
    implements SecurityMasterResponseMapper<HoldingEquityMarketCap, EquityMarketCapitalization> {

  @Override
  public HoldingEquityMarketCap map(EquityMarketCapitalization smsResponse, Holding holding) {
    Map<EquityMarketCapitalizationType, BigDecimal> ratingsMap = Optional.ofNullable(smsResponse)
        .map(EquityMarketCapitalization::getValues)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getEquityMarketCapitalization() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            EquityMarketCapitalizationTypeValue::getEquityMarketCapitalization,
            EquityMarketCapitalizationTypeValue::getValue,
            (existing, replacement) -> existing,
            () -> new EnumMap<>(EquityMarketCapitalizationType.class)));

    HoldingEquityMarketCap result = new HoldingEquityMarketCap()
        .setRatings(ratingsMap)
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(EquityMarketCapitalization::getDataProvider)
        .ifPresent(dp -> result.setProvider(dp.name()));

    return result;
  }
}