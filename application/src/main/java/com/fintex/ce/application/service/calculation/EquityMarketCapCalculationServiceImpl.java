package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.domain.model.calculation.EquityMarketCapType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquityMarketCapResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.util.AllocationMappingUtils;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.GIANT;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.LARGE;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.MEDIUM;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.MICRO;
import static com.fintex.ce.domain.model.calculation.EquityMarketCapType.SMALL;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_EMC_EMC_001;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static java.math.BigDecimal.ZERO;

@Service
public class EquityMarketCapCalculationServiceImpl
    extends
      BreakdownAbstractService<EquityMarketCapResult, EquityMarketCapType> {

  static final Map<EquityMarketCapType, Set<EquityMarketCapType>> GROUPS;

  static final Map<EquityMarketCapType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static final Map<EquityMarketCapType, BigDecimal> ALLOCATION_DEFAULT_MAP;

  static {
    GROUPS = Collections.unmodifiableMap(
        Map.of(
            LARGE, Set.of(LARGE, GIANT),
            MEDIUM, Set.of(MEDIUM),
            SMALL, Set.of(SMALL, MICRO)));
    GROUPS.keySet().forEach(f -> DEFAULT_MAP.put(f, null));

    ALLOCATION_DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(EquityMarketCapType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final SecurityDataFetcher<EquityMarketCapitalization> equityMarketCapSecurityDataFetcher;

  public EquityMarketCapCalculationServiceImpl(
      final SecurityDataFetcher<EquityMarketCapitalization> equityMarketCapSecurityDataFetcher) {
    super();
    this.equityMarketCapSecurityDataFetcher = equityMarketCapSecurityDataFetcher;
  }

  @Override
  public Map<Holding, Map<EquityMarketCapType, BigDecimal>> fetchExposures(
      final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    Map<Holding, EquityMarketCapitalization> rawData = equityMarketCapSecurityDataFetcher.fetch(reqDTO.getHoldings(),
        List.of());
    return AllocationMappingUtils.mapToAllocations(rawData,
        EquityMarketCapitalization::getRatings, EquityMarketCapType::of,
        ALLOCATION_DEFAULT_MAP, WRN_EMC_EMC_001, "FDS Equity Market Capitalization", warnings);
  }

  @Override
  public EquityMarketCapResult calculate(final Map<Holding, Map<EquityMarketCapType, BigDecimal>> exposures,
      final List<Holding> holdings,
      final List<Warning> warnings) {
    if (PortfolioUtils.areAllValuesZerosInMap(exposures)) {
      EquityMarketCapResult defaultResult = new EquityMarketCapResult();
      defaultResult.setEquityMarketCapitalization(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    final Map<EquityMarketCapType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        EquityMarketCapType.values());
    final Map<EquityMarketCapType, BigDecimal> reScaled = toUserScale(groupedResults(reScaleAbs(netProducts)));
    EquityMarketCapResult result = new EquityMarketCapResult();
    result.setEquityMarketCapitalization(reScaled);
    result.setWarnings(warnings);
    return result;
  }

  Map<EquityMarketCapType, BigDecimal> groupedResults(final Map<EquityMarketCapType, BigDecimal> netProducts) {
    return GROUPS.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> calculateSumWithinTheSameGroup(netProducts,
        e)));
  }

  BigDecimal calculateSumWithinTheSameGroup(final Map<EquityMarketCapType, BigDecimal> netProducts,
      final Map.Entry<EquityMarketCapType, Set<EquityMarketCapType>> e) {
    return e.getValue().stream().map(type -> netProducts.getOrDefault(type, ZERO)).reduce(ZERO, BigDecimal::add);
  }

}
