package com.fintex.ce.application.mapping.response;

import com.fintex.ce.application.mapping.CountryRegionResolver;
import com.fintex.ce.mapping.ResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.CountryExposureResult;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;

/**
 * Response mapper for CountryExposure domain model to CountryExposureResult. Handles conversion of country exposure
 * calculations to response format.
 */
@Component
public class CountryExposureResponseMapper implements ResponseMapper<CountryExposure, CountryExposureResult> {

  static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(CountryRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  private final CountryRegionResolver countryRegionResolver;

  public CountryExposureResponseMapper(CountryRegionResolver countryRegionResolver) {
    this.countryRegionResolver = countryRegionResolver;
  }

  @Override
  public CountryExposureResult toResponse(CountryExposure domain) {
    if (domain == null || domain.getAllocations() == null) {
      return CountryExposureResult.builder()
          .countryExposure(DEFAULT_MAP)
          .warnings(List.of())
          .build();
    }
    // Domain model uses Country keys - resolve each to its CountryRegionType bucket via the mapping table
    Map<CountryRegionType, BigDecimal> enumMap = convertToEnumMap(domain.getAllocations());
    return CountryExposureResult.builder()
        .countryExposure(toUserScale(enumMap))
        .warnings(List.of())
        .build();
  }

  @Override
  public CountryExposureResult toResponse(Map<PortfolioHolding, CountryExposure> domainMap,
      List<Notification> warnings) {
    // This method requires complex aggregation with holding weights
    // Delegate to service for now
    throw new UnsupportedOperationException("Use service-level aggregation for CountryExposure");
  }

  /**
   * Creates response from pre-calculated net products (after weighting and rescaling).
   *
   * @param netProducts
   *          the calculated net product values per country region type
   * @param warnings
   *          list of warnings to include in response
   * @return the result with scaled values
   */
  public CountryExposureResult fromNetProducts(Map<CountryRegionType, BigDecimal> netProducts,
      List<Notification> warnings) {
    if (netProducts == null || netProducts.isEmpty()) {
      return CountryExposureResult.builder()
          .countryExposure(DEFAULT_MAP)
          .warnings(warnings)
          .build();
    }
    return CountryExposureResult.builder()
        .countryExposure(toUserScale(netProducts))
        .warnings(warnings)
        .build();
  }

  /**
   * Creates empty/default response with warnings.
   *
   * @param warnings
   *          list of warnings to include in response
   * @return response with default (null) values for all country region types
   */
  public CountryExposureResult toEmptyResponse(List<Notification> warnings) {
    return CountryExposureResult.builder()
        .countryExposure(DEFAULT_MAP)
        .warnings(warnings)
        .build();
  }

  /**
   * Converts a Country-keyed allocation map from the domain model into a CountryRegionType-keyed map, summing values
   * that resolve to the same region and skipping countries with no mapped region.
   */
  private Map<CountryRegionType, BigDecimal> convertToEnumMap(Map<Country, BigDecimal> allocations) {
    Map<CountryRegionType, BigDecimal> result = new EnumMap<>(CountryRegionType.class);
    for (Map.Entry<Country, BigDecimal> entry : allocations.entrySet()) {
      CountryRegionType region = countryRegionResolver.regionOf(entry.getKey());
      if (region != null && entry.getValue() != null) {
        result.merge(region, entry.getValue(), BigDecimal::add);
      }
    }
    return result;
  }
}
