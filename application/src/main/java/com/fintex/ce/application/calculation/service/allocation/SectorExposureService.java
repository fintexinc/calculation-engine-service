package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.calculation.allocation.HoldingSectorAllocation;
import com.fintex.ce.model.domain.calculation.allocation.SectorExposureData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.ConsolidatedSectorExposureResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.wm.commons.domain.allocation.AssetAllocationRegionType;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.allocation.SectorAllocationType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fintex.ce.application.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.model.error.ErrorCode.MISSING_SECTOR_ALLOCATION;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;

/**
 * Consolidated sector exposure: one sector distribution over the <em>whole</em> portfolio, with the equity sectors and
 * the fixed-income sectors as buckets of the same pie, on the {@link AbstractBreakdownService} template.
 *
 * <p>
 * <b>Why this metric exists rather than summing the two per-sleeve ones.</b> {@code equity-sector} rescales each
 * security's equity sectors to 100% and then weights it by its <em>full</em> portfolio weight, so a 60/40 balanced fund
 * lends its equity sector profile to its bond half as well, and a pure bond fund lands wholly in {@code UNKNOWN}.
 * {@code fixed-income-bond-sector} has the mirror-image defect. Neither answers "what sectors is my money in", and the
 * two cannot simply be added because each is expressed against a different denominator.
 *
 * <p>
 * <b>Why the distribution is read rather than derived.</b> Reconciling the two sector vectors needs the security's
 * sleeve split, and the split can only be derived where the provider's raw figures are: the equity vector arrives as a
 * percentage of the whole security while the fixed-income one is normalised inside its own sleeve, and Security Master
 * used to rescale both away before the calculation engine saw them. TMI-558 moved that reconciliation upstream into
 * {@code SECTOR_ALLOCATION}, which is published as a single vector over the whole security, summing to 1, with whatever
 * the two vectors do not account for reported as {@link SectorAllocationType#UNKNOWN}. Deriving it here as well would
 * be a second, less informed copy of the same arithmetic — the calculation engine sees only the rescaled outputs, not
 * the provider's raw columns.
 *
 * <p>
 * <b>Why two attributes all the same.</b> A security that has no distribution to reconcile — an individual company,
 * which belongs to one sector — publishes that sector as a scalar instead, so the metric reads {@code EQUITY_SECTOR}
 * alongside the consolidated vector, the way {@link AbstractGeographicExposureService} reads {@code GEOGRAPHY}
 * alongside its region allocation. Without it every individual stock in the portfolio would report as missing data and
 * the pie would total less than the money in it, which is the very defect this metric exists to fix.
 *
 * <p>
 * <b>Why normalization is left off.</b> Every holding contributes a distribution that already sums to 1, so the
 * weighted aggregate totals 100% on its own. Rescaling it, as the per-sleeve metrics do, would be a second
 * normalization and would hide exactly the imbalance this metric exists to show. This is the same policy as
 * {@link AbstractAssetAllocationService}, and for the same reason: these buckets are absolute portfolio proportions,
 * not proportions of a sleeve.
 */
@Service
public class SectorExposureService
    extends
      AbstractBreakdownService<SectorExposureData, ConsolidatedSectorExposureResult, SectorAllocationType> {

  public SectorExposureService(PortfolioWeightCalculator portfolioWeightCalculator) {
    super(portfolioWeightCalculator, SectorAllocationType.class);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.SECTOR_EXPOSURE;
  }

  @Override
  public List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(CompositeSecurityAttribute.SECTOR_ALLOCATION, CompositeSecurityAttribute.EQUITY_SECTOR);
  }

  /**
   * The scalar arrives on the equity taxonomy, because that is what a company's sector is, and is translated onto the
   * consolidated one here so that the calculation below sees one distribution type whichever attribute answered. The
   * translation lives in this service rather than in the adapter because an attribute has exactly one binding, and
   * {@code EQUITY_SECTOR} is already bound to the per-sleeve domain type the equity-sector metric consumes; two metrics
   * reading one attribute therefore share the mapper and convert to their own taxonomy themselves.
   */
  @Override
  public SectorExposureData prepareData(SecurityData securityData) {
    return new SectorExposureData(
        securityData.get(CompositeSecurityAttribute.SECTOR_ALLOCATION),
        consolidate(securityData.get(CompositeSecurityAttribute.EQUITY_SECTOR)));
  }

  private static Map<PortfolioHolding, HoldingSectorAllocation> consolidate(
      Map<PortfolioHolding, EquitySector> equitySectors) {
    Map<PortfolioHolding, HoldingSectorAllocation> consolidated = new HashMap<>();
    equitySectors.forEach((holding, equitySector) -> consolidated.put(holding, HoldingSectorAllocation.builder()
        .allocations(onConsolidatedTaxonomy(equitySector.getAllocations()))
        .currency(equitySector.getCurrency())
        .providers(equitySector.getProviders())
        .build()));
    return consolidated;
  }

  /**
   * A bucket the consolidated taxonomy cannot name is dropped rather than folded into {@code UNKNOWN}, so the holding
   * arrives with nothing distributed and is reported as such with a warning. Defensive: every equity sector has a
   * consolidated counterpart, and the only unmapped value is {@code UNKNOWN} itself, which no publisher of this
   * attribute emits.
   */
  private static Map<SectorAllocationType, BigDecimal> onConsolidatedTaxonomy(
      Map<EquitySectorAllocationType, BigDecimal> equityBuckets) {
    Map<SectorAllocationType, BigDecimal> consolidated = new EnumMap<>(SectorAllocationType.class);
    if (CollectionUtils.isEmpty(equityBuckets)) {
      return consolidated;
    }
    equityBuckets.forEach((equitySector, weight) -> {
      SectorAllocationType bucket = SectorAllocationType.fromEquitySector(equitySector);
      if (bucket != SectorAllocationType.UNKNOWN) {
        consolidated.merge(bucket, weight, BigDecimal::add);
      }
    });
    return consolidated;
  }

  @Override
  protected Currency currencyFor(PortfolioHolding holding, SectorExposureData data) {
    return PortfolioUtils.cashOrGicCurrency(holding)
        .or(() -> Optional.ofNullable(sectorsFor(holding, data)).map(HoldingSectorAllocation::getCurrency))
        .orElse(null);
  }

  /**
   * Cash and GIC are buckets here, not omissions: a portfolio's sector pie can only total 100% if every holding is
   * accounted for, and unlike a geographic breakdown, cash does have a sector — the provider reports it inside the
   * fixed-income breakdown as short-term investments.
   */
  @Override
  protected boolean participatesInBreakdown(PortfolioHolding holding) {
    return true;
  }

  @Override
  protected Map<SectorAllocationType, BigDecimal> exposureFor(PortfolioHolding holding, SectorExposureData data,
      List<Notification> warnings) {
    if (CASH_PREDICATE.test(holding)) {
      return singleBucket(SectorAllocationType.ST_INVESTMENTS);
    }
    if (GIC_PREDICATE.test(holding)) {
      return singleBucket(gicSector((GicHolding) holding));
    }
    HoldingSectorAllocation sectors = sectorsFor(holding, data);
    if (sectors == null) {
      warnings.add(SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return singleBucket(SectorAllocationType.UNKNOWN);
    }
    if (CollectionUtils.isEmpty(sectors.getAllocations())) {
      warnings.add(MISSING_SECTOR_ALLOCATION.toNotificationForHolding(holding));
      return singleBucket(SectorAllocationType.UNKNOWN);
    }
    // A no-op while Security Master honours its contract — it balances the vector to exactly 1 before publishing it.
    // Kept because this metric reports its buckets without a final rescale: a vector that arrived summing to 1.05
    // would push the reported portfolio past 100% with nothing downstream to catch it, so the invariant the class
    // javadoc rests on is enforced where it is relied upon rather than assumed of an upstream release.
    return reScaleAbs(sectors.getAllocations());
  }

  /**
   * Reports the provider's rounding residues as zero rather than as ~1e-6 slices, which this metric needs for the same
   * reason {@link AbstractAssetAllocationService} does: both surface their buckets unrescaled, so nothing washes the
   * residue into a denominator.
   */
  @Override
  protected Map<SectorAllocationType, BigDecimal> postProcess(Map<SectorAllocationType, BigDecimal> netProducts) {
    return denoiseNearZero(netProducts);
  }

  /**
   * Identity: the aggregated buckets are already absolute portfolio proportions. See the class javadoc.
   */
  @Override
  protected Map<SectorAllocationType, BigDecimal> normalize(Map<SectorAllocationType, BigDecimal> netProducts) {
    return netProducts;
  }

  @Override
  protected ConsolidatedSectorExposureResult buildResult(Map<SectorAllocationType, BigDecimal> buckets,
      List<Notification> warnings) {
    return ConsolidatedSectorExposureResult.builder()
        .sectorExposure(buckets)
        .warnings(warnings)
        .build();
  }

  /**
   * Which of the two attributes describes this holding, and why an answer under the consolidated one is not enough on
   * its own, is {@link #preferPopulated}.
   */
  private static HoldingSectorAllocation sectorsFor(PortfolioHolding holding, SectorExposureData data) {
    return preferPopulated(data.distributions().get(holding), data.scalarSectors().get(holding),
        sectors -> !CollectionUtils.isEmpty(sectors.getAllocations()));
  }

  /**
   * A GIC's bucket follows the rule the asset-allocation metric already applies to it — under a year it is cash, beyond
   * that it is fixed income — rather than a second rule invented here, so the same deposit is never short-term in one
   * client chart and a bond in another. It has no issuer sector of its own, so the longer-term case lands in
   * {@code OTHER_BONDS}: it is neither government nor corporate debt.
   */
  private SectorAllocationType gicSector(GicHolding holding) {
    return holding.getAssetAllocationRegionType() == AssetAllocationRegionType.CASH
        ? SectorAllocationType.ST_INVESTMENTS
        : SectorAllocationType.OTHER_BONDS;
  }
}
