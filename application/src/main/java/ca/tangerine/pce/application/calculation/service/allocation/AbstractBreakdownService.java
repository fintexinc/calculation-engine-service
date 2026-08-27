package ca.tangerine.pce.application.calculation.service.allocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static ca.tangerine.pce.application.util.CalculationUtils.reScale;
import static ca.tangerine.pce.application.util.DecimalUtils.toUserScale;
import static ca.tangerine.pce.util.FilterUtils.CASH_PREDICATE;
import static ca.tangerine.pce.util.FilterUtils.GIC_PREDICATE;

import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.application.util.AllocationHelper;
import ca.tangerine.pce.application.util.PortfolioUtils;
import ca.tangerine.pce.calculation.CalculationService;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.error.Notification;

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

  private static final BigDecimal NEAR_ZERO_THRESHOLD = new BigDecimal("0.00001");

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
   * Clamps aggregated values whose magnitude is below {@code 1e-5} to zero, for the metrics that report their buckets
   * without rescaling. Morningstar carries tiny residues in buckets like {@code OTHER} or {@code CASH} for derivatives
   * accounting and percentage-rounding offsets; surfacing them as ~1e-6 slices of a client-facing chart is noise, while
   * real positions are always orders of magnitude larger. Metrics that rescale to 100% do not need this — the residue
   * disappears into the denominator — so this is a {@link #postProcess} helper rather than part of the pipeline.
   */
  protected final Map<T, BigDecimal> denoiseNearZero(Map<T, BigDecimal> netProducts) {
    Map<T, BigDecimal> denoised = new EnumMap<>(bucketType);
    for (Map.Entry<T, BigDecimal> entry : netProducts.entrySet()) {
      BigDecimal value = entry.getValue();
      denoised.put(entry.getKey(),
          value == null || value.abs().compareTo(NEAR_ZERO_THRESHOLD) < 0 ? BigDecimal.ZERO : value);
    }
    return denoised;
  }

  /**
   * Picks which of the two data a holding may carry applies to it, for the breakdowns whose buckets arrive under one
   * attribute for a composite security and another for an individual company — a distribution over sectors for a fund,
   * the single sector it belongs to for a company.
   *
   * <p>
   * The distribution wins only when it actually carries data, which is what {@code populated} answers. Market
   * Investment Catalogue serves an allocation attribute for any security declaring even one of its columns, and every
   * security declares {@code currency} — so a stock does come back under the distribution attribute, with its currency
   * and no allocations. Preferring the distribution on presence alone would discard the bucket the stock does have and
   * report it as missing data instead.
   *
   * <p>
   * Absence stays absence: when neither attribute answered, the result is {@code null} and the caller reports the
   * holding as unresolved rather than as resolved-but-undistributed. The unpopulated distribution is returned ahead of
   * a missing fallback for the same reason — the two facts carry different warnings.
   */
  protected static <A> A preferPopulated(A distribution, A fallback, Predicate<A> populated) {
    if (distribution != null && populated.test(distribution)) {
      return distribution;
    }
    return fallback != null ? fallback : distribution;
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
