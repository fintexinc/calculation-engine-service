package com.fintex.ce.adapter.webclient.mic.mapper;

import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
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
 * Maps Market Investment Catalogue CountryAllocation response to CountryExposure domain model.
 */
@Component
public class CountryExposureMapper
    implements
      MarketInvestmentCatalogueResponseMapper<CountryExposure, CountryAllocation> {

  @Override
  public CountryExposure map(CountryAllocation micResponse, PortfolioHolding holding) {
    final Map<Country, BigDecimal> allocationMap = Optional.ofNullable(micResponse)
        .map(CountryAllocation::getAllocations)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            CountryAllocationValue::getType,
            CountryAllocationValue::getValue,
            BigDecimal::add));

    final List<DataProvider> providers = Optional.ofNullable(micResponse)
        .map(CountryAllocation::getDataProviders)
        .orElseGet(List::of);

    return CountryExposure.builder()
        .allocations(allocationMap)
        .holdingType(holding.getHoldingType())
        .providers(providers)
        .build();
  }
}
