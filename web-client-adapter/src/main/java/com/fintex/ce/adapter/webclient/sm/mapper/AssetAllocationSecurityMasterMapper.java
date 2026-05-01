package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.value.NameValue;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps Security Master Asset Allocation response to domain AssetAllocation.
 */
@Component
public class AssetAllocationSecurityMasterMapper
    implements
      SecurityMasterResponseMapper<HoldingAssetAllocation, AssetAllocation> {

  @Override
  public HoldingAssetAllocation map(AssetAllocation smsResponse,
      PortfolioHolding holding) {
    Map<String, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(AssetAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .collect(Collectors.toMap(NameValue::getName, NameValue::getValue));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(AssetAllocation::getDataProvider)
        .map(List::of)
        .orElseGet(List::of);

    return HoldingAssetAllocation.builder()
        .holdingType(holding.getHoldingType())
        .allocations(allocationMap)
        .providers(providers)
        .build();
  }
}
