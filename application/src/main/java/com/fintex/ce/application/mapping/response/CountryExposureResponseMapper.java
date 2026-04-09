package com.fintex.ce.application.mapping.response;

import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.CountryExposureResult;
import com.fintex.ce.mapping.ResponseMapper;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.DecimalUtils.toUserScale;

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

  @Override
  public CountryExposureResult toResponse(CountryExposure domain) {
    if (domain == null || domain.getAllocations() == null) {
      CountryExposureResult defaultResult = new CountryExposureResult();
      defaultResult.setCountryExposure(DEFAULT_MAP);
      defaultResult.setWarnings(List.of());
      return defaultResult;
    }
    // Domain model uses String keys - convert to enum
    Map<CountryRegionType, BigDecimal> enumMap = convertToEnumMap(domain.getAllocations());
    CountryExposureResult result = new CountryExposureResult();
    result.setCountryExposure(toUserScale(enumMap));
    result.setWarnings(List.of());
    return result;
  }

  @Override
  public CountryExposureResult toResponse(Map<Holding, CountryExposure> domainMap, List<Warning> warnings) {
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
   * @return the response DTO with scaled values
   */
  public CountryExposureResult fromNetProducts(Map<CountryRegionType, BigDecimal> netProducts, List<Warning> warnings) {
    if (netProducts == null || netProducts.isEmpty()) {
      CountryExposureResult defaultResult = new CountryExposureResult();
      defaultResult.setCountryExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    CountryExposureResult result = new CountryExposureResult();
    result.setCountryExposure(toUserScale(netProducts));
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Creates empty/default response with warnings.
   *
   * @param warnings
   *          list of warnings to include in response
   * @return response with default (null) values for all country region types
   */
  public CountryExposureResult toEmptyResponse(List<Warning> warnings) {
    CountryExposureResult result = new CountryExposureResult();
    result.setCountryExposure(DEFAULT_MAP);
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Converts String-keyed map from domain model to enum-keyed map.
   */
  private Map<CountryRegionType, BigDecimal> convertToEnumMap(Map<String, BigDecimal> stringMap) {
    Map<CountryRegionType, BigDecimal> result = new HashMap<>();
    for (Map.Entry<String, BigDecimal> entry : stringMap.entrySet()) {
      try {
        CountryRegionType type = CountryRegionType.valueOf(entry.getKey());
        result.put(type, entry.getValue());
      } catch (IllegalArgumentException ignored) {
        // Skip unknown type keys
      }
    }
    return result;
  }
}
