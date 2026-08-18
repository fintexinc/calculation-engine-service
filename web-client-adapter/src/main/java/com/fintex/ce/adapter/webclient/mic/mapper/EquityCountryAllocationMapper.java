package com.fintex.ce.adapter.webclient.mic.mapper;

import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.CountryAllocation;
import com.fintex.wm.commons.domain.allocation.CountryAllocationValue;
import com.fintex.wm.commons.domain.enumeration.Country;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps Market Investment Catalogue CountryAllocation response to EquityCountryAllocation domain model.
 */
@Component
public class EquityCountryAllocationMapper
    implements
      MarketInvestmentCatalogueResponseMapper<EquityCountryAllocation, CountryAllocation> {

  @Override
  public EquityCountryAllocation map(CountryAllocation micResponse, PortfolioHolding holding) {
    Map<Country, BigDecimal> allocationMap = Optional.ofNullable(micResponse)
        .map(CountryAllocation::getAllocations)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(CountryAllocationValue::getType, CountryAllocationValue::getValue));

    final List<DataProvider> providers = Optional.ofNullable(micResponse)
        .map(CountryAllocation::getDataProviders)
        .orElseGet(List::of);

    return EquityCountryAllocation.builder()
        .allocations(allocationMap)
        .providers(providers)
        .build();
  }
}
