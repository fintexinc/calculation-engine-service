package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.CountryAllocation;
import com.fintex.wm.commons.domain.value.CountryValue;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps Security Master CountryAllocation response to EquityCountryAllocation domain model.
 */
@Component
public class EquityCountryAllocationMapper
    implements
      SecurityMasterResponseMapper<EquityCountryAllocation, CountryAllocation> {

  @Override
  public EquityCountryAllocation map(CountryAllocation smsResponse, PortfolioHolding holding) {
    Map<String, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(CountryAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .collect(Collectors.toMap(CountryValue::getIsoCode, CountryValue::getValue));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(CountryAllocation::getDataProvider)
        .map(List::of)
        .orElseGet(List::of);

    return EquityCountryAllocation.builder()
        .allocations(allocationMap)
        .holdingId(holding.getSecurityIdentifier().getId())
        .providers(providers)
        .build();
  }
}