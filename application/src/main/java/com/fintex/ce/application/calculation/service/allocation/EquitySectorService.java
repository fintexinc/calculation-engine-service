package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.EquitySectorResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_SECTOR_ALLOCATION;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static java.math.BigDecimal.ZERO;

@Service
public class EquitySectorService
    extends
      BreakdownAbstractService<EquitySectorResult, EquitySectorAllocationType> {

  static final Map<EquitySectorAllocationType, BigDecimal> ALLOCATION_DEFAULT_MAP;

  static {
    ALLOCATION_DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(EquitySectorAllocationType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final SecurityDataFetcher<EquitySector> equitySectorSecurityDataFetcher;
  private final EquitySectorResponseMapper responseMapper;

  public EquitySectorService(final SecurityDataFetcher<EquitySector> equitySectorSecurityDataFetcher,
      final EquitySectorResponseMapper responseMapper) {
    super();
    this.equitySectorSecurityDataFetcher = equitySectorSecurityDataFetcher;
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_SECTOR;
  }

  @Override
  public EquitySectorResult calculate(ExposureDataHolder<EquitySectorAllocationType> exposureData,
      List<PortfolioHolding> holdings) {
    var sectors = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (PortfolioUtils.areAllValuesZerosInMap(sectors)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<EquitySectorAllocationType, BigDecimal> netProducts = calculateNetProducts(sectors, holdings,
        EquitySectorAllocationType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public ExposureDataHolder<EquitySectorAllocationType> fetchExposures(final PortfolioHoldingsCommand command) {
    Map<PortfolioHolding, EquitySector> rawData = equitySectorSecurityDataFetcher.fetch(command.getHoldings(),
        command.getDataProviders());
    List<Notification> warnings = new ArrayList<>();
    Map<PortfolioHolding, Map<EquitySectorAllocationType, BigDecimal>> exposures = command.getHoldings().stream()
        .filter(CASH_PREDICATE.or(GIC_PREDICATE).negate())
        .collect(toMap(holding -> holding, holding -> toSectorExposure(holding, rawData.get(holding), warnings)));
    return new ExposureDataHolder<>(exposures, warnings);
  }

  private Map<EquitySectorAllocationType, BigDecimal> toSectorExposure(PortfolioHolding holding, EquitySector sector,
      List<Notification> warnings) {
    Map<EquitySectorAllocationType, BigDecimal> allocations = Optional.ofNullable(sector)
        .map(EquitySector::getAllocations)
        .orElseGet(Map::of);
    if (CollectionUtils.isEmpty(allocations)) {
      warnings.add(MISSING_EQUITY_SECTOR_ALLOCATION.toNotificationForHolding(holding));
      return new EnumMap<>(ALLOCATION_DEFAULT_MAP);
    }
    Map<EquitySectorAllocationType, BigDecimal> result = new EnumMap<>(ALLOCATION_DEFAULT_MAP);
    result.putAll(allocations);
    return result;
  }
}
