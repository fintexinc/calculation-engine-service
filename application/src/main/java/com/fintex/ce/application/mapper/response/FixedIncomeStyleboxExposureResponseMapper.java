package com.fintex.ce.application.mapper.response;

import com.fintex.ce.domain.enumeration.calculation.FixedIncomeStyleboxType;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.result.FixedIncomeStyleboxExposureResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.mapper.ResponseMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.util.DecimalUtils.toUserScale;

/**
 * Response mapper for FixedIncomeStyleboxExposure domain model to FixedIncomeStyleboxExposureResult. Handles conversion
 * of fixed income stylebox exposure calculations to response format.
 */
@Component
public class FixedIncomeStyleboxExposureResponseMapper
    implements
      ResponseMapper<FixedIncomeStyleboxExposure, FixedIncomeStyleboxExposureResult> {

  static final Map<FixedIncomeStyleboxType, BigDecimal> DEFAULT_MAP = new EnumMap<>(FixedIncomeStyleboxType.class);

  static {
    Stream.of(FixedIncomeStyleboxType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  @Override
  public FixedIncomeStyleboxExposureResult toResponse(FixedIncomeStyleboxExposure domain) {
    if (domain == null || domain.getBoxValues() == null) {
      FixedIncomeStyleboxExposureResult defaultResult = new FixedIncomeStyleboxExposureResult();
      defaultResult.setFixedIncomeStyleboxExposure(DEFAULT_MAP);
      defaultResult.setWarnings(List.of());
      return defaultResult;
    }
    // Domain model uses String keys - convert to enum
    Map<FixedIncomeStyleboxType, BigDecimal> enumMap = convertToEnumMap(domain.getBoxValues());
    FixedIncomeStyleboxExposureResult result = new FixedIncomeStyleboxExposureResult();
    result.setFixedIncomeStyleboxExposure(toUserScale(enumMap));
    result.setWarnings(List.of());
    return result;
  }

  @Override
  public FixedIncomeStyleboxExposureResult toResponse(Map<Holding, FixedIncomeStyleboxExposure> domainMap,
      List<Warning> warnings) {
    // This method requires complex aggregation with holding weights
    // Delegate to service for now
    throw new UnsupportedOperationException("Use service-level aggregation for FixedIncomeStyleboxExposure");
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
  public FixedIncomeStyleboxExposureResult fromNetProducts(Map<FixedIncomeStyleboxType, BigDecimal> netProducts,
      List<Warning> warnings) {
    if (netProducts == null || netProducts.isEmpty()) {
      FixedIncomeStyleboxExposureResult defaultResult = new FixedIncomeStyleboxExposureResult();
      defaultResult.setFixedIncomeStyleboxExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    FixedIncomeStyleboxExposureResult result = new FixedIncomeStyleboxExposureResult();
    result.setFixedIncomeStyleboxExposure(toUserScale(netProducts));
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
  public FixedIncomeStyleboxExposureResult toEmptyResponse(List<Warning> warnings) {
    FixedIncomeStyleboxExposureResult result = new FixedIncomeStyleboxExposureResult();
    result.setFixedIncomeStyleboxExposure(DEFAULT_MAP);
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Converts String-keyed map from domain model to enum-keyed map.
   */
  private Map<FixedIncomeStyleboxType, BigDecimal> convertToEnumMap(Map<String, BigDecimal> stringMap) {
    Map<FixedIncomeStyleboxType, BigDecimal> result = new EnumMap<>(FixedIncomeStyleboxType.class);
    for (Map.Entry<String, BigDecimal> entry : stringMap.entrySet()) {
      try {
        FixedIncomeStyleboxType type = FixedIncomeStyleboxType.valueOf(entry.getKey());
        result.put(type, entry.getValue());
      } catch (IllegalArgumentException ignored) {
        // Skip unknown type keys
      }
    }
    return result;
  }
}
