package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.allocation.CountryAllocation;
import com.fintex.sm.model.domain.value.CountryValue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Maps Security Master CountryAllocation response to CountryExposure domain model.
 */
@Component
public class CountryExposureMapper
    implements SecurityMasterResponseMapper<CountryExposure, CountryAllocation> {

  @Override
  public CountryExposure map(CountryAllocation smsResponse, Holding holding) {
    Map<String, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(CountryAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getIsoCode() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            CountryValue::getIsoCode,
            CountryValue::getValue,
            BigDecimal::add));

    CountryExposure result = new CountryExposure()
        .setAllocations(allocationMap)
        .setHoldingType(holding.getHoldingType())
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(CountryAllocation::getDataProvider)
        .ifPresent(dp -> result.setProvider(dp.name()));

    return result;
  }
}
