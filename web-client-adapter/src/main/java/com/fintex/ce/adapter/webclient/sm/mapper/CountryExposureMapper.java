package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
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
 * Maps Security Master CountryAllocation response to CountryExposure domain model.
 */
@Component
public class CountryExposureMapper
    implements
      SecurityMasterResponseMapper<CountryExposure, CountryAllocation> {

  @Override
  public CountryExposure map(CountryAllocation smsResponse, PortfolioHolding holding) {
    final Map<String, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(CountryAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getIsoCode() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            CountryValue::getIsoCode,
            CountryValue::getValue,
            BigDecimal::add));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(CountryAllocation::getDataProvider)
        .map(List::of)
        .orElseGet(List::of);

    return CountryExposure.builder()
        .allocations(allocationMap)
        .holdingType(holding.getHoldingType())
        .providers(providers)
        .build();
  }
}
