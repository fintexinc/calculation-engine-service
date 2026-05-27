package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.Maturities;
import com.fintex.wm.commons.domain.allocation.MaturityDurationValue;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps Security Master Maturities response to MaturityAllocation domain model.
 */
@Component
public class MaturityAllocationMapper
    implements
      SecurityMasterResponseMapper<MaturityAllocation, Maturities> {

  @Override
  public MaturityAllocation map(Maturities smsResponse, PortfolioHolding holding) {
    Map<String, BigDecimal> durationMap = Optional.ofNullable(smsResponse)
        .map(Maturities::getPeriods)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getMaturityDuration() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            entry -> entry.getMaturityDuration().name(),
            MaturityDurationValue::getValue,
            BigDecimal::add));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(Maturities::getDataProviders)
        .orElseGet(List::of);

    return MaturityAllocation.builder()
        .maturityDurationValues(durationMap)
        .holdingType(holding.getHoldingType())
        .providers(providers)
        .build();
  }
}
