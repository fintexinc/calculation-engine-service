package com.fintex.ce.mapping;

import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.holding.Holding;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GeographicAllocationMappingService {
  Map<Holding, Map<GeographicRegionType, BigDecimal>> mapToGeographicRegions(
      Map<Holding, Map<String, BigDecimal>> holdingAllocations,
      List<Warning> warnings, ExceptionCode errorCode);
}
