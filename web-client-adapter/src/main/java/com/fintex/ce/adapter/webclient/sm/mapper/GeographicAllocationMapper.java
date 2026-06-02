package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.GeographicAllocation;
import com.fintex.wm.commons.domain.allocation.GeographicAllocationValue;
import com.fintex.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps the SMS geographic-allocation response ({@link GeographicAllocationWithCurrency}) into the domain
 * {@link HoldingGeographicAllocation}. Pulls the typed allocations from the nested {@link GeographicAllocation}, the
 * data provider from the wrapper, and the trading currency from the {@link CurrencyDatapoint}.
 */
@Component
public class GeographicAllocationMapper
    implements
      SecurityMasterResponseMapper<HoldingGeographicAllocation, GeographicAllocationWithCurrency> {

  @Override
  public HoldingGeographicAllocation map(GeographicAllocationWithCurrency smsResponse, PortfolioHolding holding) {
    return HoldingGeographicAllocation.builder()
        .allocations(toTypedAllocations(smsResponse))
        .currency(toCurrency(smsResponse))
        .providers(providers(smsResponse))
        .build();
  }

  private Map<GeographicRegionType, BigDecimal> toTypedAllocations(GeographicAllocationWithCurrency smsResponse) {
    if (smsResponse == null || smsResponse.getGeographicAllocation() == null
        || CollectionUtils.isEmpty(smsResponse.getGeographicAllocation().getAllocations())) {
      return new EnumMap<>(GeographicRegionType.class);
    }
    return smsResponse.getGeographicAllocation().getAllocations().stream()
        .filter(value -> value != null && value.getType() != null && value.getValue() != null)
        .collect(Collectors.toMap(
            GeographicAllocationValue::getType,
            GeographicAllocationValue::getValue,
            BigDecimal::add,
            () -> new EnumMap<>(GeographicRegionType.class)));
  }

  private Currency toCurrency(GeographicAllocationWithCurrency smsResponse) {
    return Optional.ofNullable(smsResponse)
        .map(GeographicAllocationWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }

  private List<DataProvider> providers(GeographicAllocationWithCurrency smsResponse) {
    if (smsResponse == null) {
      return List.of();
    }
    List<DataProvider> providers = Optional.ofNullable(smsResponse.getGeographicAllocation())
        .map(GeographicAllocation::getDataProviders)
        .orElseGet(smsResponse::getDataProviders);
    return providers == null ? List.of() : providers;
  }
}
