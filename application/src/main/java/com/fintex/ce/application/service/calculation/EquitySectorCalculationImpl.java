package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.EquitySectorResponseMapper;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.EquitySectorResult;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.util.PortfolioUtils;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.fintex.ce.domain.enumeration.DataProvider.DEFAULT_PROVIDERS;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static java.util.stream.Collectors.toMap;

@Service
public class EquitySectorCalculationImpl
    extends
      BreakdownAbstractService<EquitySectorResult, EquitySectorAllocationType> {

  private static final Map<EquitySectorAllocationType, BigDecimal> DEFAULT_MAP =
      Collections.unmodifiableMap(
          Stream.of(EquitySectorAllocationType.values()).collect(toMap(t -> t, t -> BigDecimal.ZERO)));

  private final SecurityDataPort<EquitySector> securityDataPort;
  private final EquitySectorResponseMapper responseMapper;

  public EquitySectorCalculationImpl(final SecurityDataPort<EquitySector> securityDataPort,
      final EquitySectorResponseMapper responseMapper) {
    super();
    this.securityDataPort = securityDataPort;
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
    List<DataProvider> providers = getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS);
    Map<Holding, EquitySector> allocations = securityDataPort.fetch(reqDTO.getHoldings(), providers);
    return toSectorExposures(allocations);
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
