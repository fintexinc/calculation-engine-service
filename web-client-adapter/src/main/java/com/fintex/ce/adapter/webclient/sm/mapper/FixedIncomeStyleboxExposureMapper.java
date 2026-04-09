package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.FixedIncomeStyleBoxType;
import com.fintex.sm.model.domain.rating.FixedIncomeStyleBoxes;
import com.fintex.sm.model.domain.value.FixedIncomeStyleBoxValue;

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
  public FixedIncomeStyleboxExposure map(FixedIncomeStyleBoxes smsResponse, Holding holding) {
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

    FixedIncomeStyleboxExposure result = new FixedIncomeStyleboxExposure()
        .setBoxValues(boxValuesMap)
        .setHoldingType(holding.getHoldingType())
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(FixedIncomeStyleBoxes::getDataProvider)
        .ifPresent(dp -> result.setProvider(dp.name()));

    return result;
  }
}