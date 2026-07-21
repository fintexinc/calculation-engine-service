package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocation;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationTypeValue;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationWithCurrency;
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
public class EquitySectorAllocationMapper
    implements
      SecurityMasterResponseMapper<EquitySector, EquitySectorAllocationWithCurrency> {

  @Override
  public EquitySector map(EquitySectorAllocationWithCurrency smsResponse, PortfolioHolding holding) {
    Map<EquitySectorAllocationType, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(EquitySectorAllocationWithCurrency::getEquitySectorAllocation)
        .map(EquitySectorAllocation::getAllocations)
        .orElseGet(List::of)
        .stream()
        .filter(entry -> entry.getType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            EquitySectorAllocationTypeValue::getType,
            EquitySectorAllocationTypeValue::getValue,
            (existing, replacement) -> existing,
            () -> new EnumMap<>(EquitySectorAllocationType.class)));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(EquitySectorAllocationWithCurrency::getEquitySectorAllocation)
        .map(EquitySectorAllocation::getDataProviders)
        .orElseGet(List::of);

    return EquitySector.builder()
        .allocations(allocationMap)
        .currency(toCurrency(smsResponse))
        .providers(providers)
        .build();
  }

  private Currency toCurrency(EquitySectorAllocationWithCurrency smsResponse) {
    return Optional.ofNullable(smsResponse)
        .map(EquitySectorAllocationWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }
}
