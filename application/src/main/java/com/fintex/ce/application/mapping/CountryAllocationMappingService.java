package com.fintex.ce.application.mapping;

import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.JacksonUtil;
import com.fintex.ce.model.domain.calculation.allocation.CountryAllocation;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.model.error.ErrorCode.UNKNOWN_TYPE_FROM_DATA_POINT;

@Service
public class CountryAllocationMappingService {
  private static final String COUNTRY_ALLOCATION_MAPPING_PATH = "/jsons/country-allocation-mapping.json";

  // pre-loaded country allocations mapping: country id or country name - rest fields
  Map<String, CountryAllocation> countryAllocationMap;

  public CountryAllocationMappingService() {
    this.countryAllocationMap = initCountryAllocationMapping();
  }

  /**
   * Maps responses from FDS to defined country regions
   *
   * @param holdingAllocations
   *          map of holdings and their responses from FDS
   * @param errorCode
   *          error code when response is empty
   * @return holdings grouped by region, paired with any warnings produced during mapping
   */
  public ExposureDataHolder<CountryRegionType> mapToCountryRegions(
      final Map<PortfolioHolding, Map<String, BigDecimal>> holdingAllocations,
      final ErrorCode errorCode) {
    final List<Notification> warnings = new ArrayList<>();
    final Map<PortfolioHolding, Map<CountryRegionType, BigDecimal>> allocations = holdingAllocations.entrySet()
        .stream()
        .collect(toMap(Map.Entry::getKey, e -> mapToRegions(e.getKey(), e.getValue(), warnings, errorCode)));
    return new ExposureDataHolder<>(allocations, warnings);
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
  public Map<CountryRegionType, BigDecimal> mapToRegions(final PortfolioHolding holding,
      final Map<String, BigDecimal> allocations,
      final List<Notification> warnings, final ErrorCode errorCode) {
    final Map<CountryRegionType, BigDecimal> map = new EnumMap<>(CountryRegionType.class);
    if (CollectionUtils.isEmpty(allocations)) {
      warnings.add(errorCode.toNotificationForHolding(holding));
      return map;
    }
    allocations.forEach((countryId, value) -> {
      final CountryAllocation allocation = countryAllocationMap.get(countryId);
      if (allocation == null || allocation.getRegion() == null) {
        warnings.add(UNKNOWN_TYPE_FROM_DATA_POINT.toNotificationForHolding(holding, countryId,
            "Country Allocation Mapping Table"));
      } else {
        sumAllocations(map, value, allocation.getRegion());
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
      throw ErrorCode.INTERNAL_SERVER_ERROR.toException(
          String.format("Country Allocation Mapping is missing from path %s", COUNTRY_ALLOCATION_MAPPING_PATH));
    }
    final List<CountryAllocation> list = JacksonUtil.deserialize(in, new TypeReference<>() {});
    Map<String, CountryAllocation> map = new HashMap<>();
    list.stream().filter(e -> e.getRegion() != null).forEach(e -> {
      map.put(e.getCountryId(), e);
      if (e.getCountryName() != null) {
        map.put(e.getCountryName(), e);
      }
    });
    return map;
  }

  public InputStream getCountryAllocationInputStream() {
    return this.getClass().getResourceAsStream(COUNTRY_ALLOCATION_MAPPING_PATH);
  }
}
