package com.fintex.ce.application.mapping.response;

import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.CreditQualityResult;
import com.fintex.ce.mapping.ResponseMapper;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import static com.fintex.ce.util.DecimalUtils.toUserScale;

@Component
public class CreditQualityResponseMapper implements ResponseMapper<CreditQuality, CreditQualityResult> {

  static final Map<FixedIncomeCreditQuality, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(FixedIncomeCreditQuality.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  @Override
  public CreditQualityResult toResponse(CreditQuality domain) {
    if (domain == null || domain.getRatings() == null) {
      CreditQualityResult defaultResult = new CreditQualityResult();
      defaultResult.setCreditQuality(DEFAULT_MAP);
      defaultResult.setWarnings(List.of());
      return defaultResult;
    }
    // Direct mapping not supported - need portfolio aggregation
    throw new UnsupportedOperationException("Use portfolio-level aggregation for CreditQuality");
  }

  @Override
  public CreditQualityResult toResponse(Map<Holding, CreditQuality> domainMap, List<Warning> warnings) {
    // This method requires complex aggregation with asset allocation data
    // Delegate to service for now
    throw new UnsupportedOperationException("Use service-level aggregation for CreditQuality");
  }

  /**
   * Creates response from pre-calculated fixed income credit quality values.
   */
  public CreditQualityResult fromCalculatedValues(Map<FixedIncomeCreditQuality, BigDecimal> creditQuality,
      List<Warning> warnings) {
    CreditQualityResult result = new CreditQualityResult();
    result.setCreditQuality(toUserScale(creditQuality));
    result.setWarnings(warnings);
    return result;
  }

  /**
   * Creates empty/default response with warnings.
   */
  public CreditQualityResult toEmptyResponse(List<Warning> warnings) {
    CreditQualityResult result = new CreditQualityResult();
    result.setCreditQuality(DEFAULT_MAP);
    result.setWarnings(warnings);
    return result;
  }
}
