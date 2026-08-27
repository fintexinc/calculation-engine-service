package ca.tangerine.pce.application.calculation.service.allocation;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ca.tangerine.pce.model.error.ErrorCode.MISSING_EQUITY_SECTOR_ALLOCATION;
import static ca.tangerine.pce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;

import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.application.util.PortfolioUtils;
import ca.tangerine.pce.model.domain.calculation.allocation.EquitySector;
import ca.tangerine.pce.model.domain.calculation.allocation.EquitySectorData;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.allocation.EquitySectorResult;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationType;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.error.Notification;

/**
 * Equity-sector breakdown. Only the equity-sector specifics live here; the weighting, aggregation, normalization and
 * response assembly are the shared {@link AbstractBreakdownService} pipeline.
 *
 * <p>
 * Two attributes, the way {@link AbstractGeographicExposureService} asks for its allocation plus {@code GEOGRAPHY}: a
 * fund carries a distribution over sectors, whereas an individual company carries the one sector it belongs to, and
 * Market Investment Catalogue serves those as {@code EQUITY_SECTOR_ALLOCATION} and {@code EQUITY_SECTOR} respectively.
 * Asking for both is what lets a mixed portfolio bucket its stocks instead of reporting them as missing data.
 */
@Service
public class EquitySectorExposureService
    extends
      AbstractBreakdownService<EquitySectorData, EquitySectorResult, EquitySectorAllocationType> {

  public EquitySectorExposureService(PortfolioWeightCalculator portfolioWeightCalculator) {
    super(portfolioWeightCalculator, EquitySectorAllocationType.class);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_SECTOR;
  }

  @Override
  public List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION, CompositeSecurityAttribute.EQUITY_SECTOR);
  }

  @Override
  public EquitySectorData prepareData(SecurityData securityData) {
    return new EquitySectorData(
        securityData.get(CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION),
        securityData.get(CompositeSecurityAttribute.EQUITY_SECTOR));
  }

  @Override
  protected Currency currencyFor(PortfolioHolding holding, EquitySectorData data) {
    return PortfolioUtils.cashOrGicCurrency(holding)
        .or(() -> Optional.ofNullable(sectorFor(holding, data)).map(EquitySector::getCurrency))
        .orElse(null);
  }

  @Override
  protected Map<EquitySectorAllocationType, BigDecimal> exposureFor(PortfolioHolding holding, EquitySectorData data,
      List<Notification> warnings) {
    EquitySector sector = sectorFor(holding, data);
    if (sector == null) {
      warnings.add(SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return singleBucket(EquitySectorAllocationType.UNKNOWN);
    }
    Map<EquitySectorAllocationType, BigDecimal> allocations = sector.getAllocations();
    if (CollectionUtils.isEmpty(allocations)) {
      warnings.add(MISSING_EQUITY_SECTOR_ALLOCATION.toNotificationForHolding(holding));
      return singleBucket(EquitySectorAllocationType.UNKNOWN);
    }
    return new EnumMap<>(allocations);
  }

  @Override
  protected EquitySectorResult buildResult(Map<EquitySectorAllocationType, BigDecimal> buckets,
      List<Notification> warnings) {
    return EquitySectorResult.builder()
        .equitySector(buckets)
        .warnings(warnings)
        .build();
  }

  /**
   * Which of the two attributes describes this holding, and why an answer under the distribution one is not enough on
   * its own, is {@link #preferPopulated}.
   */
  private static EquitySector sectorFor(PortfolioHolding holding, EquitySectorData data) {
    return preferPopulated(data.distributions().get(holding), data.scalarSectors().get(holding),
        sector -> !CollectionUtils.isEmpty(sector.getAllocations()));
  }
}
