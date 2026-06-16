package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.util.AllocationMappingUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.HoldingEquityMarketCap;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.EquityMarketCapResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_MARKET_CAPITALIZATION;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.GIANT;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.LARGE;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.MEDIUM;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.MICRO;
import static com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType.SMALL;
import static java.math.BigDecimal.ZERO;

@Service
public class EquityMarketCapitalizationService
    extends
      BreakdownAbstractService<EquityMarketCapResult, EquityMarketCapitalizationType> {

  static final Map<EquityMarketCapitalizationType, Set<EquityMarketCapitalizationType>> GROUPS;

  static final Map<EquityMarketCapitalizationType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static final Map<EquityMarketCapitalizationType, BigDecimal> ALLOCATION_DEFAULT_MAP;

  static {
    GROUPS = Collections.unmodifiableMap(
        Map.of(
            LARGE, Set.of(LARGE, GIANT),
            MEDIUM, Set.of(MEDIUM),
            SMALL, Set.of(SMALL, MICRO)));
    GROUPS.keySet().forEach(f -> DEFAULT_MAP.put(f, null));

    ALLOCATION_DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(EquityMarketCapitalizationType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final SecurityDataFetcher<HoldingEquityMarketCap> equityMarketCapSecurityDataFetcher;

  public EquityMarketCapitalizationService(
      final SecurityDataFetcher<HoldingEquityMarketCap> equityMarketCapSecurityDataFetcher) {
    super();
    this.equityMarketCapSecurityDataFetcher = equityMarketCapSecurityDataFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_MARKET_CAPITALIZATION;
  }

  @Override
  public ExposureDataHolder<EquityMarketCapitalizationType> fetchExposures(final PortfolioHoldingsCommand command) {
    Map<PortfolioHolding, HoldingEquityMarketCap> rawData = equityMarketCapSecurityDataFetcher.fetch(
        command.getHoldings(), command.getDataProviders());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        HoldingEquityMarketCap::getRatings,
        ALLOCATION_DEFAULT_MAP, MISSING_EQUITY_MARKET_CAPITALIZATION);
  }

  @Override
  public EquityMarketCapResult calculate(ExposureDataHolder<EquityMarketCapitalizationType> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      return EquityMarketCapResult.builder()
          .equityMarketCapitalization(DEFAULT_MAP)
          .warnings(warnings)
          .build();
    }
    final Map<EquityMarketCapitalizationType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        EquityMarketCapitalizationType.values());
    final Map<EquityMarketCapitalizationType, BigDecimal> reScaled = toUserScale(groupedResults(reScaleAbs(
        netProducts)));
    return EquityMarketCapResult.builder()
        .equityMarketCapitalization(reScaled)
        .warnings(warnings)
        .build();
  }

  Map<EquityMarketCapitalizationType, BigDecimal> groupedResults(
      final Map<EquityMarketCapitalizationType, BigDecimal> netProducts) {
    return GROUPS.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> calculateSumWithinTheSameGroup(netProducts,
        e)));
  }

  BigDecimal calculateSumWithinTheSameGroup(final Map<EquityMarketCapitalizationType, BigDecimal> netProducts,
      final Map.Entry<EquityMarketCapitalizationType, Set<EquityMarketCapitalizationType>> e) {
    return e.getValue().stream().map(type -> netProducts.getOrDefault(type, ZERO)).reduce(ZERO, BigDecimal::add);
  }

}
