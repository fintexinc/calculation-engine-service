package com.fintex.ce.service.interfaces;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.calculation.CountryRegionType;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CountryAllocationMappingService {

    Map<Holding, Map<CountryRegionType, BigDecimal>> mapToCountryRegions(final Map<Holding, Map<String, BigDecimal>> holdingAllocations,
                                                                         final List<Warning> warnings, final ExceptionCode errorCode);

}
