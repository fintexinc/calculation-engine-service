package com.fintex.ce.application.mapper.response;

import com.fintex.ce.domain.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.result.EquitySectorResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.mapper.ResponseMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.DecimalUtils.toUserScale;

/**
 * Response mapper for EquitySector domain model to EquitySectorResult. Handles conversion of equity sector allocation
 * calculations to response format.
 */
@Component
public class EquitySectorResponseMapper implements ResponseMapper<EquitySector, EquitySectorResult> {

  public static final Map<EquitySectorAllocationType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(EquitySectorAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, BigDecimal.ZERO));
  }

  @Override
  public EquitySectorResult toResponse(EquitySector domain) {
    if (domain == null || domain.getAllocations() == null) {
      EquitySectorResult defaultResult = new EquitySectorResult();
      defaultResult.setEquitySector(DEFAULT_MAP);
      defaultResult.setWarnings(List.of());
      return defaultResult;
    }
    // Domain model uses String keys - convert to enum
    Map<EquitySectorAllocationType, BigDecimal> enumMap = convertToEnumMap(domain.getAllocations());
    EquitySectorResult result = new EquitySectorResult();
    result.setEquitySector(toUserScale(enumMap));
    result.setWarnings(List.of());
    return result;
  }

  @Override
  public EquitySectorResult toResponse(Map<Holding, EquitySector> domainMap, List<Warning> warnings) {
    // This method requires complex aggregation with holding weights
    // Delegate to service for now
    throw new UnsupportedOperationException("Use service-level aggregation for EquitySector");
  }

  /**
   * Creates response from pre-calculated net products (after weighting and rescaling).
   *
   * @param netProducts
   *          the calculated net product values per sector type
   * @param warnings
   *          list of warnings to include in response
   * @return the response DTO with scaled values
   */
  public EquitySectorResult fromNetProducts(Map<EquitySectorAllocationType, BigDecimal> netProducts,
      List<Warning> warnings) {
    if (netProducts == null || netProducts.isEmpty()) {
      EquitySectorResult defaultResult = new EquitySectorResult();
      defaultResult.setEquitySector(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    EquitySectorResult result = new EquitySectorResult();
    result.setEquitySector(toUserScale(netProducts));
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Creates empty/default response with warnings.
   *
   * @param warnings
   *          list of warnings to include in response
   * @return response with default (null) values for all sector types
   */
  public EquitySectorResult toEmptyResponse(List<Warning> warnings) {
    EquitySectorResult result = new EquitySectorResult();
    result.setEquitySector(DEFAULT_MAP);
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Converts String-keyed map from domain model to enum-keyed map.
   */
  private Map<EquitySectorAllocationType, BigDecimal> convertToEnumMap(Map<String, BigDecimal> stringMap) {
    Map<EquitySectorAllocationType, BigDecimal> result = new HashMap<>();
    for (Map.Entry<String, BigDecimal> entry : stringMap.entrySet()) {
      try {
        EquitySectorAllocationType type = EquitySectorAllocationType.valueOf(entry.getKey());
        result.put(type, entry.getValue());
      } catch (IllegalArgumentException ignored) {
        // Skip unknown type keys
      }
    }
    return result;
  }
}
