package com.fintex.ce.application.mapping.response;

import com.fintex.ce.mapping.ResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.DecimalUtils.toUserScale;

@Component
public class EquitySectorResponseMapper implements ResponseMapper<EquitySector, EquitySectorResult> {

  public static final Map<EquitySectorAllocationType, BigDecimal> DEFAULT_MAP;

  static {
    Map<EquitySectorAllocationType, BigDecimal> tmp = new HashMap<>();
    Stream.of(EquitySectorAllocationType.values()).forEach(f -> tmp.put(f, null));
    DEFAULT_MAP = Collections.unmodifiableMap(tmp);
  }

  @Override
  public EquitySectorResult toResponse(EquitySector domain) {
    if (domain == null || domain.getAllocations() == null) {
      return EquitySectorResult.builder()
          .equitySector(DEFAULT_MAP)
          .warnings(List.of())
          .build();
    }
    return EquitySectorResult.builder()
        .equitySector(toUserScale(domain.getAllocations()))
        .warnings(List.of())
        .build();
  }

  @Override
  public EquitySectorResult toResponse(Map<PortfolioHolding, EquitySector> domainMap, List<Notification> warnings) {
    throw new UnsupportedOperationException("Use service-level aggregation for EquitySector");
  }

  public EquitySectorResult fromNetProducts(Map<EquitySectorAllocationType, BigDecimal> netProducts,
      List<Notification> warnings) {
    if (netProducts == null || netProducts.isEmpty()) {
      return EquitySectorResult.builder()
          .equitySector(DEFAULT_MAP)
          .warnings(warnings)
          .build();
    }
    return EquitySectorResult.builder()
        .equitySector(toUserScale(netProducts))
        .warnings(warnings)
        .build();
  }

  public EquitySectorResult toEmptyResponse(List<Notification> warnings) {
    return EquitySectorResult.builder()
        .equitySector(DEFAULT_MAP)
        .warnings(warnings)
        .build();
  }

}
