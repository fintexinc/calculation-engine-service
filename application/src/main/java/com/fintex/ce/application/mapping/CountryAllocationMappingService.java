package com.fintex.ce.application.mapping;

import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.model.error.ErrorCode.UNKNOWN_TYPE_FROM_DATA_POINT;

@Service
public class CountryAllocationMappingService {

  private final CountryRegionResolver countryRegionResolver;

  public CountryAllocationMappingService(CountryRegionResolver countryRegionResolver) {
    this.countryRegionResolver = countryRegionResolver;
  }

  /**
   * Maps responses from MIC to defined country regions
   *
   * @param holdingAllocations
   *          map of holdings and their responses from MIC
   * @param errorCode
   *          error code when response is empty
   * @return holdings grouped by region, paired with any warnings produced during mapping
   */
  public ExposureDataHolder<CountryRegionType> mapToCountryRegions(
      final Map<PortfolioHolding, Map<Country, BigDecimal>> holdingAllocations,
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
   *          allocations: country - value
   * @param warnings
   *          warning
   * @param errorCode
   *          error code when response is empty
   * @return grouped by regions
   */
  public Map<CountryRegionType, BigDecimal> mapToRegions(final PortfolioHolding holding,
      final Map<Country, BigDecimal> allocations,
      final List<Notification> warnings, final ErrorCode errorCode) {
    final Map<CountryRegionType, BigDecimal> map = new EnumMap<>(CountryRegionType.class);
    if (CollectionUtils.isEmpty(allocations)) {
      warnings.add(errorCode.toNotificationForHolding(holding));
      return map;
    }
    allocations.forEach((country, value) -> {
      final CountryRegionType region = countryRegionResolver.regionOf(country);
      if (region == null) {
        final String alpha3 = Optional.ofNullable(country).map(Country::getAlpha3Code).orElse(null);
        warnings.add(UNKNOWN_TYPE_FROM_DATA_POINT.toNotificationForHolding(holding, alpha3,
            "Country Allocation Mapping Table"));
      } else {
        sumAllocations(map, value, region);
      }
    });
    return map;
  }

  public void sumAllocations(final Map<CountryRegionType, BigDecimal> map, final BigDecimal value,
      final CountryRegionType region) {
    map.putIfAbsent(region, BigDecimal.ZERO);
    map.computeIfPresent(region, (type, sum) -> sum.add(value));
  }
}
