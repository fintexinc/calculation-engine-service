package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSector;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fintex.ce.model.error.ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;

/**
 * Fixed-income bond-sector breakdown. Only the bond-sector specifics live here; the weighting, aggregation,
 * normalization and response assembly are the shared {@link AbstractBreakdownService} pipeline.
 */
@Service
public class FixedIncomeBondSectorService
    extends
      AbstractSingleAttributeBreakdownService<FixedIncomeBondSector, FixedIncomeSectorResult, FixedIncomeSectorAllocationType> {

  public FixedIncomeBondSectorService(PortfolioWeightCalculator portfolioWeightCalculator) {
    super(portfolioWeightCalculator, FixedIncomeSectorAllocationType.class);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_BOND_SECTOR;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.FIXED_INCOME_SECTOR_ALLOCATION;
  }

  @Override
  protected Currency currencyOf(FixedIncomeBondSector data) {
    return data.getCurrency();
  }

  @Override
  protected Map<FixedIncomeSectorAllocationType, BigDecimal> toBuckets(PortfolioHolding holding,
      FixedIncomeBondSector sector, List<Notification> warnings) {
    if (sector == null) {
      warnings.add(SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return singleBucket(FixedIncomeSectorAllocationType.UNKNOWN);
    }
    Map<FixedIncomeSectorAllocationType, BigDecimal> rawSectors = Optional.ofNullable(
        sector.getFixedIncomeBondSectors()).orElseGet(Map::of);
    if (CollectionUtils.isEmpty(rawSectors)) {
      warnings.add(MISSING_FIXED_INCOME_BOND_SECTOR.toNotificationForHolding(holding));
      return singleBucket(FixedIncomeSectorAllocationType.UNKNOWN);
    }
    return rawSectors.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left,
            () -> new EnumMap<>(FixedIncomeSectorAllocationType.class)));
  }

  @Override
  protected FixedIncomeSectorResult buildResult(Map<FixedIncomeSectorAllocationType, BigDecimal> buckets,
      List<Notification> warnings) {
    return FixedIncomeSectorResult.builder()
        .fixedIncomeSector(buckets)
        .warnings(warnings)
        .build();
  }
}
