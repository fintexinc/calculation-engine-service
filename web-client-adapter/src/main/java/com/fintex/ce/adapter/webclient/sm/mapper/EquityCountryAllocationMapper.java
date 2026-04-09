package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.allocation.CountryAllocation;
import com.fintex.sm.model.domain.value.CountryValue;

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
  public EquityCountryAllocation map(CountryAllocation smsResponse, Holding holding) {
    Map<String, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(CountryAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .collect(Collectors.toMap(CountryValue::getIsoCode, CountryValue::getValue));

    EquityCountryAllocation result = new EquityCountryAllocation()
        .setAllocations(allocationMap)
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(CountryAllocation::getDataProvider)
        .ifPresent(dp -> result.setProviders(List.of(dp)));

    return result;
  }
}