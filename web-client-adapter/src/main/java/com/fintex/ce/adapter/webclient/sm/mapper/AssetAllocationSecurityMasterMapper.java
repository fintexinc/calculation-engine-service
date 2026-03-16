package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.sm.dto.AssetAllocationDto;
import com.fintex.sm.model.domain.allocation.AssetAllocation;
import com.fintex.sm.model.domain.value.NameValue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Maps Security Master Asset Allocation response to AssetAllocationDto.
 */
@Component
public class AssetAllocationSecurityMasterMapper
    implements SecurityMasterResponseMapper<AssetAllocationDto, AssetAllocation> {

  @Override
  public AssetAllocationDto map(AssetAllocation smsResponse, Holding holding) {
    Map<String, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(AssetAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .collect(Collectors.toMap(NameValue::getName, NameValue::getValue));

    AssetAllocationDto result = new AssetAllocationDto()
        .setHoldingType(holding.getHoldingType())
        .setAssetAllocation(allocationMap)
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(AssetAllocation::getDataProvider)
        .ifPresent(dp -> result.setProvider(DataProvider.of(dp.name()).name()));

    return result;
  }
}
