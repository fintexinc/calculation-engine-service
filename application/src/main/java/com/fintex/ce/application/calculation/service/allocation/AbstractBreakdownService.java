package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.util.AllocationHelper;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.CalculationUtils.reScale;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;

/**
 * Single template for every breakdown allocation / exposure metric. All of these metrics are computed the same way and
 * differ only in their input attribute and how a single holding's data maps onto the bucket enum, so this base owns the
 * whole pipeline and each metric supplies only what is genuinely metric-specific:
 *
 * <ol>
 * <li>resolve each holding's currency ({@link #currencyFor});</li>
 * <li>for holdings that participate in the breakdown ({@link #participatesInBreakdown}), extract a per-bucket exposure
 * map, emitting a warning + a fallback bucket when the datum is missing ({@link #exposureFor});</li>
 * <li>weight holdings by value converted to the default target currency via {@link PortfolioWeightCalculator} (so
 * multi-currency portfolios are correct);</li>
 * <li>aggregate into per-bucket net products, apply an optional post-aggregation remap ({@link #postProcess}), rescale
 * to 100% and round to the user display scale;</li>
 * <li>assemble the concrete result ({@link #buildResult}).</li>
 * </ol>
 *
 * <p>
 * A portfolio with no participating exposure yields an all-buckets-present, all-null result carrying only the warnings.
 * Single-attribute metrics should extend {@link AbstractSingleAttributeBreakdownService}.
 *
 * @param <D>
 *          the strongly typed data the metric consumes (a per-holding attribute map, or a multi-attribute record)
 * @param <R>
 *          the concrete result type
 * @param <T>
 *          the bucket enum
 */
public abstract class AbstractBreakdownService<D, R extends BaseCalculationResult, T extends Enum<T>>
    implements
      CalculationService<PortfolioHoldingsCommand, D, R> {

  protected final PortfolioWeightCalculator portfolioWeightCalculator;
  private final Class<T> bucketType;

  protected AbstractBreakdownService(PortfolioWeightCalculator portfolioWeightCalculator, Class<T> bucketType) {
    this.portfolioWeightCalculator = portfolioWeightCalculator;
    this.bucketType = bucketType;
  }

  @Override
  public final R perform(PortfolioHoldingsCommand command, D data) {
    List<PortfolioHolding> holdings = command.getHoldings();
    List<Notification> warnings = new ArrayList<>();

    Map<PortfolioHolding, Map<T, BigDecimal>> exposures = new HashMap<>();
    Map<PortfolioHolding, Currency> currencies = new HashMap<>();
    for (PortfolioHolding holding : holdings) {
      Currency currency = currencyFor(holding, data);
      if (currency != null) {
        currencies.put(holding, currency);
      }
      if (participatesInBreakdown(holding)) {
        exposures.put(holding, exposureFor(holding, data, warnings));
      }
    }
    if (PortfolioUtils.areAllValuesInMapEmpty(exposures)) {
      return buildResult(emptyBuckets(), warnings);
    }

    PortfolioWeightCalculator.Result weightResult = portfolioWeightCalculator.compute(weightingHoldings(holdings),
        currencies);
    warnings.addAll(weightResult.warnings());

    Map<T, BigDecimal> netProducts = postProcess(aggregate(exposures, weightResult.weights()));
    return buildResult(toUserScale(normalize(netProducts)), warnings);
  }

  /**
   * Extracts a holding's per-bucket exposure. Implementations emit a warning and return a fallback bucket (see
   * {@link #singleBucket}) when the underlying datum is missing, so a portfolio always returns.
   */
  protected abstract Map<T, BigDecimal> exposureFor(PortfolioHolding holding, D data, List<Notification> warnings);

  /**
   * The holding's currency for value weighting, or {@code null} when it cannot be resolved (its raw value then
   * participates unchanged, matching {@link PortfolioWeightCalculator}).
   */
  protected abstract Currency currencyFor(PortfolioHolding holding, D data);

  /**
   * Assembles the concrete result from the finished (rescaled, user-scaled) per-bucket map and warnings.
   */
  protected abstract R buildResult(Map<T, BigDecimal> buckets, List<Notification> warnings);

  /**
   * Whether a holding contributes an exposure to this breakdown. Default excludes cash/GIC (they carry no security
   * breakdown); metrics where cash/GIC ARE buckets (asset allocation) override to include them.
   */
  protected boolean participatesInBreakdown(PortfolioHolding holding) {
    return !CASH_PREDICATE.or(GIC_PREDICATE).test(holding);
  }

  /**
   * The holdings whose values form the weighting denominator. Default is the whole portfolio; metrics that scope the
   * breakdown to a sleeve (e.g. equity-only geographic exposure) override to the participating subset.
   */
  protected List<PortfolioHolding> weightingHoldings(List<PortfolioHolding> holdings) {
    return holdings;
  }

  /**
   * Optional post-aggregation remap of the net products before normalization — e.g. collapsing buckets into coarser
   * groups. Default is identity.
   */
  protected Map<T, BigDecimal> postProcess(Map<T, BigDecimal> netProducts) {
    return netProducts;
  }

  /**
   * Normalizes the aggregated net products so the reported distribution sums to 100%. Default rescales by the signed
   * net total ({@code reScale}), which is the policy TMI-547 unified every allocation and exposure metric on: a short
   * bucket subtracts from the total it is expressed against rather than inflating it, so the reported percentages add
   * up to the sleeve as actually held. Metrics whose buckets are already absolute portfolio proportions that must not
   * be re-based (asset allocation) override to identity.
   */
  protected Map<T, BigDecimal> normalize(Map<T, BigDecimal> netProducts) {
    return reScale(netProducts);
  }

  /**
   * A single 100% bucket — the canonical fallback for a holding whose data is missing or unresolved.
   */
  protected Map<T, BigDecimal> singleBucket(T bucket) {
    Map<T, BigDecimal> map = new EnumMap<>(bucketType);
    map.put(bucket, BigDecimal.ONE);
    return map;
  }

  private Map<T, BigDecimal> aggregate(Map<PortfolioHolding, Map<T, BigDecimal>> exposures,
      Map<PortfolioHolding, BigDecimal> weights) {
    Map<T, BigDecimal> products = new EnumMap<>(bucketType);
    for (T type : bucketType.getEnumConstants()) {
      products.put(type, AllocationHelper.calculateNetProduct(type, exposures, weights));
    }
    return products;
  }

  private Map<T, BigDecimal> emptyBuckets() {
    Map<T, BigDecimal> map = new EnumMap<>(bucketType);
    for (T value : bucketType.getEnumConstants()) {
      map.put(value, null);
    }
    return map;
  }
}
