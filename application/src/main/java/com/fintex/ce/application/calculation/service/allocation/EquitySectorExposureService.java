package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.mapping.response.EquitySectorResponseMapper;
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
 * Converts each holding's value to the default target currency before weighting equity-sector exposures, so
 * multi-currency portfolios produce correct sector percentages. See {@link AbstractSectorAllocationService} for the
 * shared template.
 */
@Service
public class EquitySectorExposureService
    extends
      AbstractSectorAllocationService<EquitySector, EquitySectorResult, EquitySectorAllocationType> {

  private final EquitySectorResponseMapper responseMapper;

  public EquitySectorExposureService(final EquitySectorResponseMapper responseMapper,
      final PortfolioWeightCalculator portfolioWeightCalculator) {
    super(portfolioWeightCalculator);
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_SECTOR;
  }

  // TODO(TMI-475): EQUITY_SECTOR_ALLOCATION only exists for funds. Individual stocks are not published with a
  // sector-allocation breakdown and need a distinct sector/industry attribute; add separate stock-data handling once
  // TMI-475 decides which attribute stores the stock sector.
  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION;
  }

  @Override
  protected EquitySectorAllocationType[] allocationTypes() {
    return EquitySectorAllocationType.values();
  }

  @Override
  protected Currency currencyOf(EquitySector data) {
    return data.getCurrency();
  }

  @Override
  protected EquitySectorResult emptyResponse(List<Notification> warnings) {
    return responseMapper.toEmptyResponse(warnings);
  }

  @Override
  protected EquitySectorResult fromNetProducts(Map<EquitySectorAllocationType, BigDecimal> netProducts,
      List<Notification> warnings) {
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  protected Map<EquitySectorAllocationType, BigDecimal> toSectorExposure(PortfolioHolding holding, EquitySector sector,
      List<Notification> warnings) {
    if (sector == null) {
      warnings.add(SECURITY_NOT_FOUND_FOR_METRIC.toNotificationForHolding(holding,
          getMetric().getUserFriendlyName()));
      return unknownAllocation();
    }
    Map<EquitySectorAllocationType, BigDecimal> allocations = sector.getAllocations();
    if (CollectionUtils.isEmpty(allocations)) {
      warnings.add(MISSING_EQUITY_SECTOR_ALLOCATION.toNotificationForHolding(holding));
      return unknownAllocation();
    }
    return new EnumMap<>(allocations);
  }

  private static Map<EquitySectorAllocationType, BigDecimal> unknownAllocation() {
    Map<EquitySectorAllocationType, BigDecimal> result = new EnumMap<>(EquitySectorAllocationType.class);
    result.put(EquitySectorAllocationType.UNKNOWN, BigDecimal.ONE);
    return result;
  }
}
