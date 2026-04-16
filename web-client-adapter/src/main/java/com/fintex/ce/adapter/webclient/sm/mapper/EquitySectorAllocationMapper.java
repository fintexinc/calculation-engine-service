package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocation;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationTypeNameValue;

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
      SecurityMasterResponseMapper<EquitySector, EquitySectorAllocation> {

  @Override
  public EquitySector map(EquitySectorAllocation smsResponse, Holding holding) {
    Map<EquitySectorAllocationType, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(EquitySectorAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getType() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            EquitySectorAllocationTypeNameValue::getType,
            EquitySectorAllocationTypeNameValue::getValue,
            (existing, replacement) -> existing,
            () -> new EnumMap<>(EquitySectorAllocationType.class)));

    EquitySector result = new EquitySector()
        .setAllocations(allocationMap)
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(EquitySectorAllocation::getDataProvider)
        .ifPresent(dp -> result.setProviders(List.of(dp)));

    return result;
  }
}
