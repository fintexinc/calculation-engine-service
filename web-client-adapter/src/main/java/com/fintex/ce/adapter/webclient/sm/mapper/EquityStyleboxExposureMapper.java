package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.rating.StyleBoxType;
import com.fintex.wm.commons.domain.rating.StyleBoxValue;
import com.fintex.wm.commons.domain.rating.StyleBoxes;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps SM StyleBoxes (equity style box) response to PCE EquityStyleboxExposure domain model. SM StyleBoxType values are
 * mapped to PCE StyleBoxType enum keys.
 */
@Component
public class EquityStyleboxExposureMapper
    implements
      SecurityMasterResponseMapper<EquityStyleboxExposure, StyleBoxes> {

  @Override
  public EquityStyleboxExposure map(StyleBoxes smsResponse, PortfolioHolding holding) {
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
        .ifPresent(dp -> result.setProviders(List.of(dp)));

    return result;
  }
}
