package com.fintex.ce.model.domain.result.fee;

import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.wm.commons.domain.enumeration.TimePeriod;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Portfolio fee <b>amounts</b> — annual, monthly, and projected over several horizons. Every value in this class is
 * money; none of them is a rate. The rates live in {@link AverageMerResult}, and {@link FeeComparison} keeps the two in
 * separate holders for the same reason.
 *
 * <p>
 * Amounts are reported in the request's {@code targetCurrency}. When the request omits it they fall back to
 * {@code calculation.fx.default-target-currency}, which ships as {@code CAD} — so the currency is the caller's choice
 * first and configuration second, never a fixed one.
 */
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Annual, monthly and multi-year projected portfolio fee amounts, in the request's "
    + "targetCurrency — or calculation.fx.default-target-currency (CAD unless overridden) when the request omits it. "
    + "Amounts only — the rates behind them "
    + "are reported by the 'mer' metric. Each map is keyed by aggregation mode (scaled / absolute / forceReportFee). "
    + "Annual = Σ (holding value × resolved fee rate); Monthly = Annual ÷ 12.")
public class FeesResult extends BaseCalculationResult {

  @Schema(description = "Annual fee amount by aggregation mode, in the request's targetCurrency or the configured "
      + "default when it is omitted")
  @Builder.Default
  private Map<FeeAggregationMode, BigDecimal> annualFee = new EnumMap<>(FeeAggregationMode.class);

  @Schema(description = "Monthly fee amount by aggregation mode (annual / 12), in the request's targetCurrency or the "
      + "configured default when it is omitted")
  @Builder.Default
  private Map<FeeAggregationMode, BigDecimal> monthlyFee = new EnumMap<>(FeeAggregationMode.class);

  /**
   * Projected total fee amount per aggregation mode, keyed by period. Derived from {@link #annualFee} under the
   * assumptions in {@code calculation.fee.projection}, so {@code ONE_YR} equals {@code annualFee} and {@code ONE_MTH}
   * equals {@link #monthlyFee} by construction. A mode with no defined annual fee maps to {@code null} here too, never
   * to zeros.
   */
  @Schema(description = "Projected total fee amount by aggregation mode, keyed by period, in the request's "
      + "targetCurrency or the configured default when it is omitted. Assumes the balance grows at the configured annual growth rate and the fee is charged on it each "
      + "year; the fee is not compounded as a drag on the balance. ONE_YR equals annualFee, ONE_MTH equals monthlyFee.")
  @Builder.Default
  private Map<FeeAggregationMode, Map<TimePeriod, BigDecimal>> projectedSpend = new EnumMap<>(FeeAggregationMode.class);
}
