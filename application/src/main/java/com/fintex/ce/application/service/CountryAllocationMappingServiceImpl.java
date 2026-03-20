package com.fintex.ce.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.CountryAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.service.CountryAllocationMappingService;
import com.fintex.ce.util.JacksonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.util.CollectorUtils.toMap;

@Service
public class CountryAllocationMappingServiceImpl implements CountryAllocationMappingService {
  private static final String COUNTRY_ALLOCATION_MAPPING_PATH = "/jsons/country-allocation-mapping.json";

  // pre-loaded country allocations mapping: country id - rest fields
  public Map<String, CountryAllocation> countryAllocationMap;

  public CountryAllocationMappingServiceImpl() {
    this.countryAllocationMap = initCountryAllocationMapping();
  }

  /**
   * Maps responses from FDS to defined country regions
   *
   * @param holdingAllocations
   *          map of holdings and their responses from FDS
   * @param warnings
   *          warning messages
   * @param errorCode
   *          error code when response is empty
   * @return holdings that are grouped by regions
   */
  @Override
  public Map<Holding, Map<CountryRegionType, BigDecimal>> mapToCountryRegions(
      final Map<Holding, Map<String, BigDecimal>> holdingAllocations,
      final List<Warning> warnings, final ExceptionCode errorCode) {
    return holdingAllocations.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> mapToRegions(e.getKey(), e
        .getValue(), warnings, errorCode)));
  }

  /**
   * @param holding
   *          holding
   * @param allocations
   *          allocations: county id - value
   * @param warnings
   *          warning
   * @param errorCode
   *          error code when response is empty
   * @return grouped by regions
   */
  public Map<CountryRegionType, BigDecimal> mapToRegions(final Holding holding,
      final Map<String, BigDecimal> allocations,
      final List<Warning> warnings, final ExceptionCode errorCode) {
    final Map<CountryRegionType, BigDecimal> map = new HashMap<>();
    if (CollectionUtils.isEmpty(allocations)) {
      warnings.add(errorCode.warning(holding));
      return map;
    }
    allocations.forEach((countryId, value) -> {
      final CountryAllocation allocationDTO = countryAllocationMap.get(countryId);
      if (allocationDTO == null || allocationDTO.getRegion() == null) {
        warnings.add(WRN_UNKNOWN_001.warning(holding, countryId, "Country Allocation Mapping Table"));
      } else {
        sumAllocations(map, value, allocationDTO.getRegion());
      }
    });
    return map;
  }

  public void sumAllocations(final Map<CountryRegionType, BigDecimal> map, final BigDecimal value,
      final CountryRegionType region) {
    map.putIfAbsent(region, BigDecimal.ZERO);
    map.computeIfPresent(region, (type, sum) -> sum.add(value));
  }

  public Map<String, CountryAllocation> initCountryAllocationMapping() {
    final InputStream in = getCountryAllocationInputStream();
    if (in == null) {
      final String message = String.format("Country Allocation Mapping is missing from path %s",
          COUNTRY_ALLOCATION_MAPPING_PATH);
      throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }
    final List<CountryAllocation> list = JacksonUtil.deserialize(in, new TypeReference<>() {
    });
    return list.stream().filter(e -> e.getRegion() != null).collect(toMap(CountryAllocation::getCountryId, e -> e));
  }

  public InputStream getCountryAllocationInputStream() {
    return this.getClass().getResourceAsStream(COUNTRY_ALLOCATION_MAPPING_PATH);
  }
}
