package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.calculation.CountryRegionType;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.EquityCountryExposureResult;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.service.CountryAllocationMappingService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.fintex.ce.domain.enumeration.DataProvider.DEFAULT_PROVIDERS;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_RRC_ECE_001;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityCountryExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<EquityCountryExposureResult, CountryRegionType> {

  private final SecurityDataPort<EquityCountryAllocation> securityDataPort;
  private final CountryAllocationMappingService countryAllocationMappingService;

  public static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(CountryRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  public EquityCountryExposureCalculationServiceImpl(
      final SecurityDataPort<EquityCountryAllocation> securityDataPort,
      final CountryAllocationMappingService countryAllocationMappingService) {
    super();
    this.securityDataPort = securityDataPort;
    this.countryAllocationMappingService = countryAllocationMappingService;
  }

  @Override
  public EquityCountryExposureResult calculate(final Map<Holding, Map<CountryRegionType, BigDecimal>> exposures,
      final List<Holding> holdings,
      final List<Warning> warnings) {
    if (areAllValuesInMapEmpty(exposures)) {
      EquityCountryExposureResult defaultResult = new EquityCountryExposureResult();
      defaultResult.setEquityCountryExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    final Map<CountryRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, CountryRegionType
        .values());
    final Map<CountryRegionType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
    EquityCountryExposureResult result = new EquityCountryExposureResult();
    result.setEquityCountryExposure(scaledValues);
    result.setWarnings(warnings);
    return result;
  }

  @Override
  public Map<Holding, Map<CountryRegionType, BigDecimal>> fetchExposures(final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    List<DataProvider> providers = getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS);
    Map<Holding, EquityCountryAllocation> allocations = securityDataPort.fetch(reqDTO.getHoldings(), providers);
    return toRegionExposures(allocations, warnings);
  }

  private Map<Holding, Map<CountryRegionType, BigDecimal>> toRegionExposures(
      Map<Holding, EquityCountryAllocation> allocations, List<Warning> warnings) {
    if (CollectionUtils.isEmpty(allocations)) {
      return Collections.emptyMap();
    }
    Map<Holding, Map<String, BigDecimal>> holdingAllocations = allocations.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    return countryAllocationMappingService.mapToCountryRegions(holdingAllocations, warnings, WRN_RRC_ECE_001);
  }

}
