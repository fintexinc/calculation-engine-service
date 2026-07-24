package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_SECTOR_ALLOCATION;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;

/**
 * Equity-sector breakdown. Only the equity-sector specifics live here; the weighting, aggregation, normalization and
 * response assembly are the shared {@link AbstractBreakdownService} pipeline.
 */
@Service
public class EquitySectorExposureService
    extends
      AbstractSingleAttributeBreakdownService<EquitySector, EquitySectorResult, EquitySectorAllocationType> {

  public EquitySectorExposureService(PortfolioWeightCalculator portfolioWeightCalculator) {
    super(portfolioWeightCalculator, EquitySectorAllocationType.class);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_SECTOR;
  }

  // TMI-475 decision: individual stocks are classified through this SAME attribute, not a separate one. Security
  // Master publishes a stock's single sector as a one-bucket EQUITY_SECTOR_ALLOCATION ({ sector -> 1.0 } with
  // currency), so no stock-specific handling is needed here. Until SM publishes it, a stock resolves to UNKNOWN plus
  // a warning, exactly like a fund missing its allocation. See equity-stock-sector-source-decision-for-pm.txt.
  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION;
  }

  @Override
  protected Currency currencyOf(EquitySector data) {
    return data.getCurrency();
  }

  @Override
  protected Map<EquitySectorAllocationType, BigDecimal> toBuckets(PortfolioHolding holding, EquitySector sector,
      List<Notification> warnings) {
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
}
