package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.HoldingEquityMarketCap;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalization;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationTypeValue;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EquityMarketCapitalizationMapper
    implements
      SecurityMasterResponseMapper<HoldingEquityMarketCap, EquityMarketCapitalization> {

  @Override
  public HoldingEquityMarketCap map(EquityMarketCapitalization smsResponse, PortfolioHolding holding) {
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

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(EquityMarketCapitalization::getDataProviders)
        .orElseGet(List::of);

    return HoldingEquityMarketCap.builder()
        .ratings(ratingsMap)
        .providers(providers)
        .build();
  }
}
