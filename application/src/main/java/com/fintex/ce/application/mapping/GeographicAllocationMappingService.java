package com.fintex.ce.application.mapping;

import com.fintex.ce.application.util.JacksonUtil;
import com.fintex.ce.model.domain.calculation.allocation.GeographicAllocation;
import com.fintex.ce.model.domain.calculation.allocation.GeographicRegionType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.model.error.ErrorCode.UNKNOWN_TYPE_FROM_DATA_POINT;

@Service
public class GeographicAllocationMappingService {
  private static final String GEOGRAPHIC_ALLOCATION_MAPPING_PATH = "/jsons/geography-allocation-mapping.json";

  private final Map<String, GeographicAllocation> geographicAllocationMap;

  public GeographicAllocationMappingService() {
    this.geographicAllocationMap = initGeographicAllocationMapping();
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
  public Map<PortfolioHolding, Map<GeographicRegionType, BigDecimal>> mapToGeographicRegions(
      final Map<PortfolioHolding, Map<String, BigDecimal>> holdingAllocations,
      final List<Notification> warnings, final ErrorCode errorCode) {
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
  private Map<GeographicRegionType, BigDecimal> mapToRegions(final PortfolioHolding holding,
      final Map<String, BigDecimal> allocations,
      final List<Notification> warnings, final ErrorCode errorCode) {
    final Map<GeographicRegionType, BigDecimal> map = new HashMap<>();
    if (CollectionUtils.isEmpty(allocations)) {
      warnings.add(errorCode.toNotificationForHolding(holding));
      return map;
    }
    allocations.forEach((countryId, value) -> {
      final GeographicAllocation allocation = geographicAllocationMap.get(countryId);
      if (allocation == null || allocation.getRegion() == null) {
        warnings.add(UNKNOWN_TYPE_FROM_DATA_POINT.toNotificationForHolding(holding, countryId,
            "Geographic Allocation Mapping Table"));
      } else {
        sumAllocations(map, value, allocation.getRegion());
      }
    });
    return map;
  }

  private void sumAllocations(final Map<GeographicRegionType, BigDecimal> map, final BigDecimal value,
      final GeographicRegionType region) {
    map.putIfAbsent(region, BigDecimal.ZERO);
    map.computeIfPresent(region, (type, sum) -> sum.add(value));
  }

  private Map<String, GeographicAllocation> initGeographicAllocationMapping() {
    final InputStream in = getGeographicAllocationInputStream();
    if (in == null) {
      throw ErrorCode.INTERNAL_SERVER_ERROR.toException(
          String.format("Geographic Allocation Mapping is missing from path %s", GEOGRAPHIC_ALLOCATION_MAPPING_PATH));
    }
    final List<GeographicAllocation> list = JacksonUtil.deserialize(in, new TypeReference<>() {});
    return list.stream().filter(e -> e.getRegion() != null).collect(toMap(GeographicAllocation::getCountryId,
        e -> e));
  }

  private InputStream getGeographicAllocationInputStream() {
    return this.getClass().getResourceAsStream(GEOGRAPHIC_ALLOCATION_MAPPING_PATH);
  }
}
