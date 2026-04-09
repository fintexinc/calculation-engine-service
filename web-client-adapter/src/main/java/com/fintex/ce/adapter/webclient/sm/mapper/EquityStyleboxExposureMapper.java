package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.StyleBoxType;
import com.fintex.sm.model.domain.rating.StyleBoxes;
import com.fintex.sm.model.domain.value.StyleBoxValue;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps SM StyleBoxes (equity style box) response to CE EquityStyleboxExposure domain model. SM StyleBoxType values are
 * mapped to CE StyleBoxType enum keys.
 */
@Component
public class EquityStyleboxExposureMapper
    implements
      SecurityMasterResponseMapper<EquityStyleboxExposure, StyleBoxes> {

  @Override
  public EquityStyleboxExposure map(StyleBoxes smsResponse, Holding holding) {
    Map<StyleBoxType, BigDecimal> boxValues = Optional.ofNullable(smsResponse)
        .map(StyleBoxes::getBoxValues)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getStyleBoxType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            StyleBoxValue::getStyleBoxType,
            StyleBoxValue::getValue,
            BigDecimal::add));

    EquityStyleboxExposure result = new EquityStyleboxExposure()
        .setBoxValues(boxValues)
        .setHoldingType(holding.getHoldingType())
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(StyleBoxes::getDataProvider)
        .ifPresent(dp -> result.setProvider(dp.name()));

    return result;
  }
}
