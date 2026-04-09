package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.datapoint.Maturities;
import com.fintex.sm.model.domain.value.MaturityDurationValue;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps Security Master Maturities response to MaturityAllocation domain model.
 */
@Component
public class MaturityAllocationMapper
    implements
      SecurityMasterResponseMapper<MaturityAllocation, Maturities> {

  @Override
  public MaturityAllocation map(Maturities smsResponse, Holding holding) {
    Map<String, BigDecimal> durationMap = Optional.ofNullable(smsResponse)
        .map(Maturities::getPeriods)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getMaturityDuration() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            entry -> entry.getMaturityDuration().name(),
            MaturityDurationValue::getValue,
            BigDecimal::add));

    MaturityAllocation result = new MaturityAllocation()
        .setMaturityDurationValues(durationMap)
        .setHoldingType(holding.getHoldingType())
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(Maturities::getDataProvider)
        .ifPresent(dp -> result.setProvider(dp.name()));

    return result;
  }
}
