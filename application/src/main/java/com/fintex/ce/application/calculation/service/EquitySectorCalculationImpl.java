package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.response.EquitySectorResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquitySectorResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toMap;

@Service
public class EquitySectorCalculationImpl
    extends
      BreakdownAbstractService<EquitySectorResult, EquitySectorAllocationType> {

  static final Map<EquitySectorAllocationType, BigDecimal> DEFAULT_MAP;

  static {
    DEFAULT_MAP = Collections.unmodifiableMap(
        Stream.of(EquitySectorAllocationType.values()).collect(toMap(type -> type, type -> ZERO)));
  }

  private final SecurityDataFetcher<EquitySector> equitySectorSecurityDataFetcher;
  private final EquitySectorResponseMapper responseMapper;

  public EquitySectorCalculationImpl(final SecurityDataFetcher<EquitySector> equitySectorSecurityDataFetcher,
      final EquitySectorResponseMapper responseMapper) {
    super();
    this.equitySectorSecurityDataFetcher = equitySectorSecurityDataFetcher;
    this.responseMapper = responseMapper;
  }

  @Override
  public EquitySectorResult calculate(final Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> sectors,
      final List<Holding> holdings,
      final List<Warning> warnings) {
    if (PortfolioUtils.areAllValuesZerosInMap(sectors)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<EquitySectorAllocationType, BigDecimal> netProducts = calculateNetProducts(sectors, holdings,
        EquitySectorAllocationType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> fetchExposures(
      final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    Map<Holding, EquitySector> rawData = equitySectorSecurityDataFetcher.fetch(reqDTO.getHoldings(), List.of());
    return toSectorExposures(rawData);
  }

  private Map<Holding, Map<EquitySectorAllocationType, BigDecimal>> toSectorExposures(
      Map<Holding, EquitySector> allocations) {
    if (CollectionUtils.isEmpty(allocations)) {
      return Collections.emptyMap();
    }
    return allocations.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> mapToSectorTypes(e.getValue())));
  }

  private Map<EquitySectorAllocationType, BigDecimal> mapToSectorTypes(EquitySector sector) {
    if (sector == null || CollectionUtils.isEmpty(sector.getAllocations())) {
      return new EnumMap<>(DEFAULT_MAP);
    }
    Map<EquitySectorAllocationType, BigDecimal> result = new EnumMap<>(DEFAULT_MAP);
    result.putAll(sector.getAllocations());
    return result;
  }
}
