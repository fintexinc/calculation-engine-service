package com.fintex.ce.application.mapping.response;

import com.fintex.ce.mapping.ResponseMapper;
import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeStyleboxExposureResult;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;

/**
 * Response mapper for FixedIncomeStyleboxExposure domain model to FixedIncomeStyleboxExposureResult. Handles conversion
 * of fixed income stylebox exposure calculations to response format.
 */
@Component
public class FixedIncomeStyleboxExposureResponseMapper
    implements
      ResponseMapper<FixedIncomeStyleboxExposure, FixedIncomeStyleboxExposureResult> {

  static final Map<FixedIncomeStyleBoxType, BigDecimal> DEFAULT_MAP = new EnumMap<>(FixedIncomeStyleBoxType.class);

  static {
    Stream.of(FixedIncomeStyleBoxType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  @Override
  public FixedIncomeStyleboxExposureResult toResponse(FixedIncomeStyleboxExposure domain) {
    if (domain == null || domain.getBoxValues() == null) {
      return FixedIncomeStyleboxExposureResult.builder()
          .fixedIncomeStyleboxExposure(DEFAULT_MAP)
          .warnings(List.of())
          .build();
    }
    return FixedIncomeStyleboxExposureResult.builder()
        .fixedIncomeStyleboxExposure(toUserScale(domain.getBoxValues()))
        .warnings(List.of())
        .build();
  }

  @Override
  public FixedIncomeStyleboxExposureResult toResponse(Map<PortfolioHolding, FixedIncomeStyleboxExposure> domainMap,
      List<Notification> warnings) {
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
   * @return the result with scaled values
   */
  public FixedIncomeStyleboxExposureResult fromNetProducts(Map<FixedIncomeStyleBoxType, BigDecimal> netProducts,
      List<Notification> warnings) {
    if (netProducts == null || netProducts.isEmpty()) {
      return FixedIncomeStyleboxExposureResult.builder()
          .fixedIncomeStyleboxExposure(DEFAULT_MAP)
          .warnings(warnings)
          .build();
    }
    return FixedIncomeStyleboxExposureResult.builder()
        .fixedIncomeStyleboxExposure(toUserScale(netProducts))
        .warnings(warnings)
        .build();
  }

  /**
   * Creates empty/default response with warnings.
   *
   * @param warnings
   *          list of warnings to include in response
   * @return response with default (null) values for all stylebox types
   */
  public FixedIncomeStyleboxExposureResult toEmptyResponse(List<Notification> warnings) {
    return FixedIncomeStyleboxExposureResult.builder()
        .fixedIncomeStyleboxExposure(DEFAULT_MAP)
        .warnings(warnings)
        .build();
  }
}
