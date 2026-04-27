package com.fintex.ce.application.mapping.response;

import com.fintex.ce.mapping.ResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocation;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocationType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.MaturityAllocationResult;
import com.fintex.ce.model.error.Warning;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;

/**
 * Response mapper for MaturityAllocation domain model to MaturityAllocationResult. Handles conversion of maturity
 * allocation calculations to response format.
 */
@Component
public class MaturityAllocationResponseMapper implements ResponseMapper<MaturityAllocation, MaturityAllocationResult> {

  static final Map<MaturityAllocationType, BigDecimal> DEFAULT_MAP = new EnumMap<>(MaturityAllocationType.class);

  static {
    Stream.of(MaturityAllocationType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  @Override
  public MaturityAllocationResult toResponse(MaturityAllocation domain) {
    if (domain == null || domain.getMaturityDurationValues() == null) {
      MaturityAllocationResult defaultResult = new MaturityAllocationResult();
      defaultResult.setMaturityAllocation(DEFAULT_MAP);
      defaultResult.setWarnings(List.of());
      return defaultResult;
    }
    // Domain model uses String keys - convert to enum
    Map<MaturityAllocationType, BigDecimal> enumMap = convertToEnumMap(domain.getMaturityDurationValues());
    MaturityAllocationResult result = new MaturityAllocationResult();
    result.setMaturityAllocation(toUserScale(enumMap));
    result.setWarnings(List.of());
    return result;
  }

  @Override
  public MaturityAllocationResult toResponse(Map<PortfolioHolding, MaturityAllocation> domainMap,
      List<Warning> warnings) {
    // This method requires complex aggregation with holding weights
    // Delegate to service for now
    throw new UnsupportedOperationException("Use service-level aggregation for MaturityAllocation");
  }

  /**
   * Creates response from pre-calculated net products (after weighting and rescaling).
   *
   * @param netProducts
   *          the calculated net product values per maturity type
   * @param warnings
   *          list of warnings to include in response
   * @return the response DTO with scaled values
   */
  public MaturityAllocationResult fromNetProducts(Map<MaturityAllocationType, BigDecimal> netProducts,
      List<Warning> warnings) {
    if (netProducts == null || netProducts.isEmpty()) {
      MaturityAllocationResult defaultResult = new MaturityAllocationResult();
      defaultResult.setMaturityAllocation(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    MaturityAllocationResult result = new MaturityAllocationResult();
    result.setMaturityAllocation(toUserScale(netProducts));
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Creates empty/default response with warnings.
   *
   * @param warnings
   *          list of warnings to include in response
   * @return response with default (null) values for all maturity types
   */
  public MaturityAllocationResult toEmptyResponse(List<Warning> warnings) {
    MaturityAllocationResult result = new MaturityAllocationResult();
    result.setMaturityAllocation(DEFAULT_MAP);
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Converts String-keyed map from domain model to enum-keyed map.
   */
  private Map<MaturityAllocationType, BigDecimal> convertToEnumMap(Map<String, BigDecimal> stringMap) {
    Map<MaturityAllocationType, BigDecimal> result = new EnumMap<>(MaturityAllocationType.class);
    for (Map.Entry<String, BigDecimal> entry : stringMap.entrySet()) {
      try {
        MaturityAllocationType type = MaturityAllocationType.valueOf(entry.getKey());
        result.put(type, entry.getValue());
      } catch (IllegalArgumentException ignored) {
        // Skip unknown type keys
      }
    }
    return result;
  }
}
