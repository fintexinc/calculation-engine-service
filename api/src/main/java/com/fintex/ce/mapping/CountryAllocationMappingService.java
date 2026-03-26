package com.fintex.ce.mapping;

import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.holding.Holding;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CountryAllocationMappingService {

  Map<Holding, Map<CountryRegionType, BigDecimal>> mapToCountryRegions(
      final Map<Holding, Map<String, BigDecimal>> holdingAllocations,
      final List<Warning> warnings, final ExceptionCode errorCode);

}
