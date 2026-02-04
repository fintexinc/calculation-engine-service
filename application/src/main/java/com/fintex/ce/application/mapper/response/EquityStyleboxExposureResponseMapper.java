package com.fintex.ce.application.mapper.response;

import com.fintex.ce.domain.enumeration.calculation.EquityStyleboxType;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.result.EquityStyleboxExposureResult;
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
 * Response mapper for EquityStyleboxExposure domain model to EquityStyleboxExposureResult. Handles conversion of
 * stylebox exposure calculations to response format.
 */
@Component
public class EquityStyleboxExposureResponseMapper
    implements
      ResponseMapper<EquityStyleboxExposure, EquityStyleboxExposureResult> {

  static final Map<EquityStyleboxType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(EquityStyleboxType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  @Override
  public EquityStyleboxExposureResult toResponse(EquityStyleboxExposure domain) {
    if (domain == null || domain.getBoxValues() == null) {
      EquityStyleboxExposureResult defaultResult = new EquityStyleboxExposureResult();
      defaultResult.setEquityStyleboxExposure(DEFAULT_MAP);
      defaultResult.setWarnings(List.of());
      return defaultResult;
    }
    // Domain model uses String keys - convert to enum
    Map<EquityStyleboxType, BigDecimal> enumMap = convertToEnumMap(domain.getBoxValues());
    EquityStyleboxExposureResult result = new EquityStyleboxExposureResult();
    result.setEquityStyleboxExposure(toUserScale(enumMap));
    result.setWarnings(List.of());
    return result;
  }

  @Override
  public EquityStyleboxExposureResult toResponse(Map<Holding, EquityStyleboxExposure> domainMap,
      List<Warning> warnings) {
    // This method requires complex aggregation with holding weights
    // Delegate to service for now
    throw new UnsupportedOperationException("Use service-level aggregation for EquityStyleboxExposure");
  }

  /**
   * Creates response from pre-calculated net products (after weighting and rescaling).
   *
   * @param netProducts
   *          the calculated net product values per stylebox type
   * @param warnings
   *          list of warnings to include in response
   * @return the response DTO with scaled values
   */
  public EquityStyleboxExposureResult fromNetProducts(Map<EquityStyleboxType, BigDecimal> netProducts,
      List<Warning> warnings) {
    if (netProducts == null || netProducts.isEmpty()) {
      EquityStyleboxExposureResult defaultResult = new EquityStyleboxExposureResult();
      defaultResult.setEquityStyleboxExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    EquityStyleboxExposureResult result = new EquityStyleboxExposureResult();
    result.setEquityStyleboxExposure(toUserScale(netProducts));
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Creates empty/default response with warnings.
   *
   * @param warnings
   *          list of warnings to include in response
   * @return response with default (null) values for all stylebox types
   */
  public EquityStyleboxExposureResult toEmptyResponse(List<Warning> warnings) {
    EquityStyleboxExposureResult result = new EquityStyleboxExposureResult();
    result.setEquityStyleboxExposure(DEFAULT_MAP);
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Converts String-keyed map from domain model to enum-keyed map.
   */
  private Map<EquityStyleboxType, BigDecimal> convertToEnumMap(Map<String, BigDecimal> stringMap) {
    Map<EquityStyleboxType, BigDecimal> result = new HashMap<>();
    for (Map.Entry<String, BigDecimal> entry : stringMap.entrySet()) {
      try {
        EquityStyleboxType type = EquityStyleboxType.valueOf(entry.getKey());
        result.put(type, entry.getValue());
      } catch (IllegalArgumentException ignored) {
        // Skip unknown type keys
      }
    }
    return result;
  }
}
