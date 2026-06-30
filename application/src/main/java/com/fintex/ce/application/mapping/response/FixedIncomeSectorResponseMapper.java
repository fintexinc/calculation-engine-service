package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;

@Component
public class FixedIncomeSectorResponseMapper {

  static final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> DEFAULT_MAP = Collections.unmodifiableMap(
      Stream.of(FixedIncomeSecuritiesAllocationType.values())
          .collect(
              () -> new EnumMap<>(FixedIncomeSecuritiesAllocationType.class),
              (map, type) -> map.put(type, null),
              EnumMap::putAll));

  public FixedIncomeSectorResult fromNetProducts(final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> netProducts,
      final List<Notification> warnings) {
    return FixedIncomeSectorResult.builder()
        .fixedIncomeSector(toUserScale(reScaleAbs(netProducts)))
        .warnings(warnings)
        .build();
  }

  public FixedIncomeSectorResult toEmptyResponse(final List<Notification> warnings) {
    return FixedIncomeSectorResult.builder()
        .fixedIncomeSector(DEFAULT_MAP)
        .warnings(warnings)
        .build();
  }
}
