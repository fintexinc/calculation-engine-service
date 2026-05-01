package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxValue;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxes;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FixedIncomeStyleboxExposureMapper
    implements
      SecurityMasterResponseMapper<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> {

  @Override
  public FixedIncomeStyleboxExposure map(FixedIncomeStyleBoxes smsResponse, PortfolioHolding holding) {
    Map<FixedIncomeStyleBoxType, BigDecimal> boxValuesMap = Optional.ofNullable(smsResponse)
        .map(FixedIncomeStyleBoxes::getBoxValues)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getStyleBoxType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            FixedIncomeStyleBoxValue::getStyleBoxType,
            FixedIncomeStyleBoxValue::getValue,
            (existing, replacement) -> existing,
            () -> new EnumMap<>(FixedIncomeStyleBoxType.class)));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(FixedIncomeStyleBoxes::getDataProvider)
        .map(List::of)
        .orElseGet(List::of);

    return FixedIncomeStyleboxExposure.builder()
        .boxValues(boxValuesMap)
        .holdingType(holding.getHoldingType())
        .providers(providers)
        .build();
  }
}