package com.fintex.ce.application.mapping.response;

import com.fintex.ce.mapping.ResponseMapper;
import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityStyleboxExposureResult;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.rating.StyleBoxType;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;

/**
 * Response mapper for EquityStyleboxExposure domain model to EquityStyleboxExposureResult. Handles conversion of
 * stylebox exposure calculations to response format.
 */
@Component
public class EquityStyleboxExposureResponseMapper
    implements
      ResponseMapper<EquityStyleboxExposure, EquityStyleboxExposureResult> {

  static final Map<StyleBoxType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(StyleBoxType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  @Override
  public EquityStyleboxExposureResult toResponse(EquityStyleboxExposure domain) {
    if (domain == null || domain.getBoxValues() == null) {
      return EquityStyleboxExposureResult.builder()
          .equityStyleboxExposure(DEFAULT_MAP)
          .warnings(List.of())
          .build();
    }
    return EquityStyleboxExposureResult.builder()
        .equityStyleboxExposure(toUserScale(domain.getBoxValues()))
        .warnings(List.of())
        .build();
  }

  @Override
  public EquityStyleboxExposureResult toResponse(Map<PortfolioHolding, EquityStyleboxExposure> domainMap,
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
   * @return the result with scaled values
   */
  public EquityStyleboxExposureResult fromNetProducts(Map<StyleBoxType, BigDecimal> netProducts,
      List<Warning> warnings) {
    if (netProducts == null || netProducts.isEmpty()) {
      return EquityStyleboxExposureResult.builder()
          .equityStyleboxExposure(DEFAULT_MAP)
          .warnings(warnings)
          .build();
    }
    return EquityStyleboxExposureResult.builder()
        .equityStyleboxExposure(toUserScale(netProducts))
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
  public EquityStyleboxExposureResult toEmptyResponse(List<Warning> warnings) {
    return EquityStyleboxExposureResult.builder()
        .equityStyleboxExposure(DEFAULT_MAP)
        .warnings(warnings)
        .build();
  }

}
