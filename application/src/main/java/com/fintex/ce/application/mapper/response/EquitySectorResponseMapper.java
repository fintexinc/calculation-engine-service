package com.fintex.ce.application.mapper.response;

import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquitySectorResult;
import com.fintex.ce.mapper.ResponseMapper;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import static com.fintex.ce.util.DecimalUtils.toUserScale;

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
    EquitySectorResult result = new EquitySectorResult();
    result.setEquitySector(toUserScale(domain.getAllocations()));
    result.setWarnings(List.of());
    return result;
  }

  @Override
  public EquitySectorResult toResponse(Map<Holding, EquitySector> domainMap, List<Warning> warnings) {
    throw new UnsupportedOperationException("Use service-level aggregation for EquitySector");
  }

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

  public EquitySectorResult toEmptyResponse(List<Warning> warnings) {
    EquitySectorResult result = new EquitySectorResult();
    result.setEquitySector(DEFAULT_MAP);
    result.setWarnings(warnings);
    return result;
  }

}
