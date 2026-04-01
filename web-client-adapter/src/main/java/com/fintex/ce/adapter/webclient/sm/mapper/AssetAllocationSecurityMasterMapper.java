package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.allocation.AssetAllocation;
import com.fintex.sm.model.domain.value.NameValue;
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
    implements SecurityMasterResponseMapper<HoldingAssetAllocation, AssetAllocation> {

  @Override
  public HoldingAssetAllocation map(com.fintex.sm.model.domain.allocation.AssetAllocation smsResponse, Holding holding) {
    Map<String, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(AssetAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .collect(Collectors.toMap(NameValue::getName, NameValue::getValue));

    HoldingAssetAllocation result = new HoldingAssetAllocation()
        .setHoldingType(holding.getHoldingType())
        .setAllocations(allocationMap)
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(AssetAllocation::getDataProvider)
        .ifPresent(dp -> result.setProvider(DataProvider.of(dp.name()).name()));

    return result;
  }
}
