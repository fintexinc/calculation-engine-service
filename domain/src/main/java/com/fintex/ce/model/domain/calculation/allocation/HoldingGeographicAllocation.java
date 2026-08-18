package com.fintex.ce.model.domain.calculation.allocation;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.allocation.GeographicRegionType;
import com.fintex.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Per-security geographic allocation breakdown plus the currency in which the underlying values are quoted. The
 * currency is sourced from the MIC geographic-allocation response (GeographicAllocationWithCurrency) and is used by
 * callers that need to normalize holding market values across currencies before weighting.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HoldingGeographicAllocation extends BaseCalculationData {

  private Map<GeographicRegionType, BigDecimal> allocations;
  private Currency currency;
}
