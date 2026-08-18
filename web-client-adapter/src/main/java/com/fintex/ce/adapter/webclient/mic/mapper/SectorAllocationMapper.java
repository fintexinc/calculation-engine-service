package com.fintex.ce.adapter.webclient.mic.mapper;

import com.fintex.ce.model.domain.calculation.allocation.HoldingSectorAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.SectorAllocation;
import com.fintex.wm.commons.domain.allocation.SectorAllocationType;
import com.fintex.wm.commons.domain.allocation.SectorAllocationValue;
import com.fintex.wm.commons.domain.allocation.SectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SectorAllocationMapper
    implements
      MarketInvestmentCatalogueResponseMapper<HoldingSectorAllocation, SectorAllocationWithCurrency> {

  @Override
  public HoldingSectorAllocation map(SectorAllocationWithCurrency micResponse, PortfolioHolding holding) {
    Map<SectorAllocationType, BigDecimal> allocationMap = Optional.ofNullable(micResponse)
        .map(SectorAllocationWithCurrency::getSectorAllocation)
        .map(SectorAllocation::getAllocations)
        .orElseGet(List::of)
        .stream()
        .filter(entry -> entry.getType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            SectorAllocationValue::getType,
            SectorAllocationValue::getValue,
            (existing, replacement) -> existing,
            () -> new EnumMap<>(SectorAllocationType.class)));

    final List<DataProvider> providers = Optional.ofNullable(micResponse)
        .map(SectorAllocationWithCurrency::getSectorAllocation)
        .map(SectorAllocation::getDataProviders)
        .orElseGet(List::of);

    return HoldingSectorAllocation.builder()
        .allocations(allocationMap)
        .currency(toCurrency(micResponse))
        .providers(providers)
        .build();
  }

  private Currency toCurrency(SectorAllocationWithCurrency micResponse) {
    return Optional.ofNullable(micResponse)
        .map(SectorAllocationWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }
}
