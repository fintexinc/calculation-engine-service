package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSector;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationTypeValue;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps SM {@link FixedIncomeSectorAllocationWithCurrency} (whose nested {@link FixedIncomeSectorAllocation} is keyed by
 * the canonical {@link FixedIncomeSectorAllocationType} 8-bucket taxonomy) to PCE {@link FixedIncomeBondSector}. SM
 * already classifies each entry into a typed bucket, so the mapper reads the type directly with no translation.
 */
@Component
public class FixedIncomeSectorAllocationMapper
    implements
      SecurityMasterResponseMapper<FixedIncomeBondSector, FixedIncomeSectorAllocationWithCurrency> {

  @Override
  public FixedIncomeBondSector map(FixedIncomeSectorAllocationWithCurrency smsResponse, PortfolioHolding holding) {
    Map<FixedIncomeSectorAllocationType, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(FixedIncomeSectorAllocationWithCurrency::getFixedIncomeSectorAllocation)
        .map(FixedIncomeSectorAllocation::getAllocations)
        .orElseGet(List::of)
        .stream()
        .filter(entry -> entry.getType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            FixedIncomeSectorAllocationTypeValue::getType,
            FixedIncomeSectorAllocationTypeValue::getValue,
            BigDecimal::add,
            () -> new EnumMap<>(FixedIncomeSectorAllocationType.class)));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(FixedIncomeSectorAllocationWithCurrency::getFixedIncomeSectorAllocation)
        .map(FixedIncomeSectorAllocation::getDataProviders)
        .orElseGet(List::of);

    return FixedIncomeBondSector.builder()
        .fixedIncomeBondSectors(allocationMap)
        .holdingType(holding.getHoldingType())
        .currency(toCurrency(smsResponse))
        .providers(providers)
        .build();
  }

  private Currency toCurrency(FixedIncomeSectorAllocationWithCurrency smsResponse) {
    return Optional.ofNullable(smsResponse)
        .map(FixedIncomeSectorAllocationWithCurrency::getCurrency)
        .map(CurrencyDatapoint::getValue)
        .orElse(null);
  }
}
