package com.fintex.ce.application.mapping.response;

import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;

/**
 * Shared response-mapping logic for sector allocations keyed by an enum bucket type. Concrete mappers (equity / fixed
 * income sector) supply the enum type and how to assemble their specific result; this base owns the default (all-null)
 * bucket map and the net-products / empty-response assembly so the two mappers do not duplicate it.
 *
 * @param <T>
 *          the sector allocation bucket enum
 * @param <R>
 *          the calculation result type produced by the concrete mapper
 */
// TODO unify all 7 breakdown response mappers TMI-552
public abstract class AbstractSectorResponseMapper<T extends Enum<T>, R> {

  private final Map<T, BigDecimal> defaultMap;

  protected AbstractSectorResponseMapper(Class<T> bucketType) {
    Map<T, BigDecimal> tmp = new EnumMap<>(bucketType);
    for (T value : bucketType.getEnumConstants()) {
      tmp.put(value, null);
    }
    this.defaultMap = Collections.unmodifiableMap(tmp);
  }

  /**
   * Builds a result from aggregated, FX-adjusted net products: rescales by absolute weight and applies the user display
   * scale before delegating to {@link #buildResult}.
   */
  public R fromNetProducts(Map<T, BigDecimal> netProducts, List<Notification> warnings) {
    return buildResult(toUserScale(reScaleAbs(netProducts)), warnings);
  }

  /**
   * Builds a result whose buckets are all present but null-valued (no data), carrying the given warnings.
   */
  public R toEmptyResponse(List<Notification> warnings) {
    return buildResult(defaultMap, warnings);
  }

  /**
   * Assembles the concrete result type from the per-bucket values and warnings.
   */
  protected abstract R buildResult(Map<T, BigDecimal> sectors, List<Notification> warnings);
}
