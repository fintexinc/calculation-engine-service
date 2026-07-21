package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.FixedIncomeSectorResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSector;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.model.error.ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR;
import static com.fintex.ce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;

@Service
public class FixedIncomeBondSectorService
    extends
      BreakdownAbstractService<Map<PortfolioHolding, FixedIncomeBondSector>, FixedIncomeSectorResult, FixedIncomeSectorAllocationType>
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, FixedIncomeBondSector, FixedIncomeSectorResult> {

  private final FixedIncomeSectorResponseMapper responseMapper;

  public FixedIncomeBondSectorService(final FixedIncomeSectorResponseMapper responseMapper) {
    super();
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

  public ExposureDataHolder<FixedIncomeSectorAllocationType> fetchExposures(
      final PortfolioHoldingsCommand command, final Map<PortfolioHolding, FixedIncomeBondSector> data) {
    Map<PortfolioHolding, FixedIncomeBondSector> rawData = FilterUtils.restrictToHoldings(data,
        command.getHoldings());
    List<Notification> warnings = new ArrayList<>();
    Map<PortfolioHolding, Map<FixedIncomeSectorAllocationType, BigDecimal>> exposures = command.getHoldings()
        .stream()
        .filter(CASH_PREDICATE.or(GIC_PREDICATE).negate())
        .collect(toMap(holding -> holding, holding -> toSectorExposure(holding, rawData.get(holding), warnings)));
    return new ExposureDataHolder<>(exposures, warnings);
  }

  private Map<FixedIncomeSectorAllocationType, BigDecimal> toSectorExposure(PortfolioHolding holding,
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
    Map<FixedIncomeSectorAllocationType, BigDecimal> result = new EnumMap<>(
        FixedIncomeSectorAllocationType.class);
    result.put(FixedIncomeSectorAllocationType.UNKNOWN, BigDecimal.ONE);
    return result;
  }

  @Override
  public FixedIncomeSectorResult perform(PortfolioHoldingsCommand command,
      Map<PortfolioHolding, FixedIncomeBondSector> data) {
    return calculate(fetchExposures(command, data), command.getHoldings());
  }

  public FixedIncomeSectorResult calculate(
      final ExposureDataHolder<FixedIncomeSectorAllocationType> exposureData,
      final List<PortfolioHolding> holdings) {
    final var sectors = exposureData.allocations();
    final var warnings = new ArrayList<>(exposureData.warnings());
    if (sectors.isEmpty()) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<FixedIncomeSectorAllocationType, BigDecimal> netProducts = calculateNetProducts(
        sectors, holdings, FixedIncomeSectorAllocationType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }
}
