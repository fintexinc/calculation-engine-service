package com.fintex.ce.application.mapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fintex.ce.domain.dto.GeographicAllocationDTO;
import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.ErrorCode;
import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.mapping.GeographicAllocationMappingService;
import com.fintex.ce.util.JacksonUtil;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_UNKNOWN_001;
import static com.fintex.ce.util.CollectorUtils.toMap;

@Service
public class GeographicAllocationMappingServiceImpl implements GeographicAllocationMappingService {
  private static final String GEOGRAPHIC_ALLOCATION_MAPPING_PATH = "/jsons/geography-allocation-mapping.json";

  private final Map<String, GeographicAllocationDTO> geographicAllocationMap;

  public GeographicAllocationMappingServiceImpl() {
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
  @Override
  public Map<Holding, Map<GeographicRegionType, BigDecimal>> mapToGeographicRegions(
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
  private Map<GeographicRegionType, BigDecimal> mapToRegions(final Holding holding,
      final Map<String, BigDecimal> allocations,
      final List<Warning> warnings, final ExceptionCode errorCode) {
    final Map<GeographicRegionType, BigDecimal> map = new HashMap<>();
    if (CollectionUtils.isEmpty(allocations)) {
      warnings.add(errorCode.warning(holding));
      return map;
    }
    allocations.forEach((countryId, value) -> {
      final GeographicAllocationDTO allocationDTO = geographicAllocationMap.get(countryId);
      if (allocationDTO == null || allocationDTO.getRegion() == null) {
        warnings.add(WRN_UNKNOWN_001.warning(holding, countryId, "Geographic Allocation Mapping Table"));
      } else {
        sumAllocations(map, value, allocationDTO.getRegion());
      }
    });
    return map;
  }

  private void sumAllocations(final Map<GeographicRegionType, BigDecimal> map, final BigDecimal value,
      final GeographicRegionType region) {
    map.putIfAbsent(region, BigDecimal.ZERO);
    map.computeIfPresent(region, (type, sum) -> sum.add(value));
  }

  private Map<String, GeographicAllocationDTO> initGeographicAllocationMapping() {
    final InputStream in = getGeographicAllocationInputStream();
    if (in == null) {
      final String message = String.format("Geographic Allocation Mapping is missing from path %s",
          GEOGRAPHIC_ALLOCATION_MAPPING_PATH);
      throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }
    final List<GeographicAllocationDTO> list = JacksonUtil.deserialize(in, new TypeReference<>() {
    });
    return list.stream().filter(e -> e.getRegion() != null).collect(toMap(GeographicAllocationDTO::getCountryId,
        e -> e));
  }

  private InputStream getGeographicAllocationInputStream() {
    return this.getClass().getResourceAsStream(GEOGRAPHIC_ALLOCATION_MAPPING_PATH);
  }
}
