package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.mapping.response.FixedIncomeSectorResponseMapper;
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
 * Converts each holding's value to the default target currency before weighting fixed-income bond-sector exposures, so
 * multi-currency portfolios produce correct sector percentages. See {@link AbstractSectorAllocationService} for the
 * shared template.
 */
@Service
public class FixedIncomeBondSectorService
    extends
      AbstractSectorAllocationService<FixedIncomeBondSector, FixedIncomeSectorResult, FixedIncomeSectorAllocationType> {

  private final FixedIncomeSectorResponseMapper responseMapper;

  public FixedIncomeBondSectorService(final FixedIncomeSectorResponseMapper responseMapper,
      final PortfolioWeightCalculator portfolioWeightCalculator) {
    super(portfolioWeightCalculator);
    this.responseMapper = responseMapper;
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
  protected FixedIncomeSectorAllocationType[] allocationTypes() {
    return FixedIncomeSectorAllocationType.values();
  }

  @Override
  protected Currency currencyOf(FixedIncomeBondSector data) {
    return data.getCurrency();
  }

  @Override
  protected FixedIncomeSectorResult emptyResponse(List<Notification> warnings) {
    return responseMapper.toEmptyResponse(warnings);
  }

  @Override
  protected FixedIncomeSectorResult fromNetProducts(Map<FixedIncomeSectorAllocationType, BigDecimal> netProducts,
      List<Notification> warnings) {
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  protected Map<FixedIncomeSectorAllocationType, BigDecimal> toSectorExposure(PortfolioHolding holding,
      FixedIncomeBondSector sector, List<Notification> warnings) {
    if (sector == null) {
      warnings.add(SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return unknownAllocation();
    }
    Map<FixedIncomeSectorAllocationType, BigDecimal> rawSectors = Optional.ofNullable(
        sector.getFixedIncomeBondSectors()).orElseGet(Map::of);
    if (CollectionUtils.isEmpty(rawSectors)) {
      warnings.add(MISSING_FIXED_INCOME_BOND_SECTOR.toNotificationForHolding(holding));
      return unknownAllocation();
    }
    return rawSectors.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left,
            () -> new EnumMap<>(FixedIncomeSectorAllocationType.class)));
  }

  private static Map<FixedIncomeSectorAllocationType, BigDecimal> unknownAllocation() {
    Map<FixedIncomeSectorAllocationType, BigDecimal> result = new EnumMap<>(FixedIncomeSectorAllocationType.class);
    result.put(FixedIncomeSectorAllocationType.UNKNOWN, BigDecimal.ONE);
    return result;
  }
}
