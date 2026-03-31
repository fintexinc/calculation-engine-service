package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.allocation.EquitySectorAllocation;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
import com.fintex.sm.model.domain.value.EquitySectorAllocationTypeNameValue;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EquitySectorAllocationMapper
    implements SecurityMasterResponseMapper<EquitySector, EquitySectorAllocation> {

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
        .ifPresent(dp -> {
          DataProvider provider = DataProvider.fromValue(dp.name());
          result.setProvider(provider != null ? provider.name() : dp.name());
        });

    return result;
  }
}
