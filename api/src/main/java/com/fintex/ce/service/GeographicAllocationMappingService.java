package com.fintex.ce.service;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.domain.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GeographicAllocationMappingService {
  Map<Holding, Map<GeographicRegionType, BigDecimal>> mapToGeographicRegions(
      Map<Holding, Map<String, BigDecimal>> holdingAllocations,
      List<Warning> warnings, ExceptionCode errorCode);
}
