package com.fintex.ce.application.mapping.response;

import com.fintex.ce.mapping.ResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeCreditQuality;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.CreditQualityResult;
import com.fintex.ce.model.error.Warning;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;

@Component
public class CreditQualityResponseMapper implements ResponseMapper<CreditQuality, CreditQualityResult> {

  static final Map<FixedIncomeCreditQuality, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(FixedIncomeCreditQuality.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  @Override
  public CreditQualityResult toResponse(CreditQuality domain) {
    if (domain == null || domain.getRatings() == null) {
      return CreditQualityResult.builder()
          .creditQuality(DEFAULT_MAP)
          .warnings(List.of())
          .build();
    }
    // Direct mapping not supported - need portfolio aggregation
    throw new UnsupportedOperationException("Use portfolio-level aggregation for CreditQuality");
  }

  @Override
  public CreditQualityResult toResponse(Map<PortfolioHolding, CreditQuality> domainMap, List<Warning> warnings) {
    // This method requires complex aggregation with asset allocation data
    // Delegate to service for now
    throw new UnsupportedOperationException("Use service-level aggregation for CreditQuality");
  }

  /**
   * Creates response from pre-calculated fixed income credit quality values.
   */
  public CreditQualityResult fromCalculatedValues(Map<FixedIncomeCreditQuality, BigDecimal> creditQuality,
      List<Warning> warnings) {
    return CreditQualityResult.builder()
        .creditQuality(toUserScale(creditQuality))
        .warnings(warnings)
        .build();
  }

  /**
   * Creates empty/default response with warnings.
   */
  public CreditQualityResult toEmptyResponse(List<Warning> warnings) {
    return CreditQualityResult.builder()
        .creditQuality(DEFAULT_MAP)
        .warnings(warnings)
        .build();
  }
}
