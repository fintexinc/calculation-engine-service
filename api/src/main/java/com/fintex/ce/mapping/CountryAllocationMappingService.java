package com.fintex.ce.mapping;

import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.Warning;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CountryAllocationMappingService {

  Map<Holding, Map<CountryRegionType, BigDecimal>> mapToCountryRegions(
      final Map<Holding, Map<String, BigDecimal>> holdingAllocations,
      final List<Warning> warnings, final ErrorCode errorCode);

}
