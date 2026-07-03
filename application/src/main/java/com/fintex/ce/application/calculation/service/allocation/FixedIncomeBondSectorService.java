package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.FixedIncomeSectorResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.model.error.ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR;
import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;
import static java.math.BigDecimal.ZERO;

@Service
public class FixedIncomeBondSectorService
    extends
      BreakdownAbstractService<FixedIncomeSectorResult, FixedIncomeSecuritiesAllocationType> {

  static final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> ALLOCATION_DEFAULT_MAP = Collections
      .unmodifiableMap(
          Stream.of(FixedIncomeSecuritiesAllocationType.values())
              .collect(Collectors.toMap(type -> type, type -> ZERO)));

  private final SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeBondSectorSecurityDataFetcher;
  private final FixedIncomeSectorResponseMapper responseMapper;

  public FixedIncomeBondSectorService(
      final SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeBondSectorSecurityDataFetcher,
      final FixedIncomeSectorResponseMapper responseMapper) {
    super();
    this.fixedIncomeBondSectorSecurityDataFetcher = fixedIncomeBondSectorSecurityDataFetcher;
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_BOND_SECTOR;
  }

  @Override
  public ExposureDataHolder<FixedIncomeSecuritiesAllocationType> fetchExposures(
      final PortfolioHoldingsCommand command) {
    Map<PortfolioHolding, FixedIncomeBondSecurities> rawData = fixedIncomeBondSectorSecurityDataFetcher.fetch(
        command.getHoldings(), command.getDataProviders());
    List<Notification> warnings = new ArrayList<>();
    Map<PortfolioHolding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> allocations = command.getHoldings()
        .stream()
        .filter(CASH_PREDICATE.or(GIC_PREDICATE).negate())
        .collect(toMap(holding -> holding, holding -> toSectorExposure(holding, rawData.get(holding), warnings)));
    return new ExposureDataHolder<>(allocations, warnings);
  }

  private Map<FixedIncomeSecuritiesAllocationType, BigDecimal> toSectorExposure(PortfolioHolding holding,
      FixedIncomeBondSecurities data, List<Notification> warnings) {
    Map<FixedIncomeSecuritiesAllocationType, BigDecimal> rawSectors = Optional.ofNullable(data)
        .map(FixedIncomeBondSecurities::getFixedIncomeBondSectors)
        .orElseGet(Map::of);
    if (CollectionUtils.isEmpty(rawSectors)) {
      warnings.add(MISSING_FIXED_INCOME_BOND_SECTOR.toNotificationForHolding(holding));
      return new EnumMap<>(ALLOCATION_DEFAULT_MAP);
    }
    return rawSectors.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (defaultValue, override) -> override,
            () -> new EnumMap<>(ALLOCATION_DEFAULT_MAP)));
  }

  @Override
  public FixedIncomeSectorResult calculate(
      final ExposureDataHolder<FixedIncomeSecuritiesAllocationType> exposureData,
      final List<PortfolioHolding> holdings) {
    final var sectors = exposureData.allocations();
    final var warnings = new ArrayList<>(exposureData.warnings());
    if (PortfolioUtils.areAllValuesZerosInMap(sectors)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> netProducts = calculateNetProducts(
        sectors, holdings, FixedIncomeSecuritiesAllocationType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }
}
