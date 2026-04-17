package com.fintex.ce.mapping;

import com.fintex.ce.model.domain.calculation.allocation.GeographicRegionType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.Warning;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GeographicAllocationMappingService {
  Map<PortfolioHolding, Map<GeographicRegionType, BigDecimal>> mapToGeographicRegions(
      Map<PortfolioHolding, Map<String, BigDecimal>> holdingAllocations,
      List<Warning> warnings, ErrorCode errorCode);
}
