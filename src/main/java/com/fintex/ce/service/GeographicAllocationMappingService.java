package com.fintex.ce.service;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.calculation.GeographicRegionType;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GeographicAllocationMappingService {
    Map<Holding, Map<GeographicRegionType, BigDecimal>> mapToGeographicRegions(Map<Holding, Map<String, BigDecimal>> holdingAllocations,
                                                                               List<Warning> warnings, ExceptionCode errorCode);
}
