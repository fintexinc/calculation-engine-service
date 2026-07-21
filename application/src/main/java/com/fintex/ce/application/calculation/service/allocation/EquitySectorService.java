package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.EquitySectorResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_SECTOR_ALLOCATION;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;

@Service
public class EquitySectorService
    extends
      BreakdownAbstractService<Map<PortfolioHolding, EquitySector>, EquitySectorResult, EquitySectorAllocationType>
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, EquitySector, EquitySectorResult> {

  private final EquitySectorResponseMapper responseMapper;

  public EquitySectorService(final EquitySectorResponseMapper responseMapper) {
    super();
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_SECTOR;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION;
  }

  @Override
  public EquitySectorResult perform(PortfolioHoldingsCommand command, Map<PortfolioHolding, EquitySector> data) {
    return calculate(fetchExposures(command, data), command.getHoldings());
  }

  public EquitySectorResult calculate(ExposureDataHolder<EquitySectorAllocationType> exposureData,
      List<PortfolioHolding> holdings) {
    var sectors = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (sectors.isEmpty()) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<EquitySectorAllocationType, BigDecimal> netProducts = calculateNetProducts(sectors, holdings,
        EquitySectorAllocationType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  public ExposureDataHolder<EquitySectorAllocationType> fetchExposures(final PortfolioHoldingsCommand command,
      final Map<PortfolioHolding, EquitySector> data) {
    Map<PortfolioHolding, EquitySector> rawData = FilterUtils.restrictToHoldings(data, command.getHoldings());
    List<Notification> warnings = new ArrayList<>();
    Map<PortfolioHolding, Map<EquitySectorAllocationType, BigDecimal>> exposures = command.getHoldings().stream()
        .filter(CASH_PREDICATE.or(GIC_PREDICATE).negate())
        .collect(toMap(holding -> holding, holding -> toSectorExposure(holding, rawData.get(holding), warnings)));
    return new ExposureDataHolder<>(exposures, warnings);
  }

  private Map<EquitySectorAllocationType, BigDecimal> toSectorExposure(PortfolioHolding holding, EquitySector sector,
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
