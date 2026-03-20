package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.result.EquityCountryExposureResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.service.CountryAllocationMappingService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_RRC_ECE_001;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityCountryExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<EquityCountryExposureResult, CountryRegionType> {

  private final SecurityDataFetcher<EquityCountryAllocation> equityCountryAllocationSecurityDataFetcher;
  private final CountryAllocationMappingService countryAllocationMappingService;

  public static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(CountryRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  public EquityCountryExposureCalculationServiceImpl(
      final SecurityDataFetcher<EquityCountryAllocation> equityCountryAllocationSecurityDataFetcher,
      final CountryAllocationMappingService countryAllocationMappingService) {
    super();
    this.equityCountryAllocationSecurityDataFetcher = equityCountryAllocationSecurityDataFetcher;
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
    Map<Holding, EquityCountryAllocation> rawData = equityCountryAllocationSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    Map<Holding, Map<String, BigDecimal>> holdingAllocations = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    return countryAllocationMappingService.mapToCountryRegions(holdingAllocations, warnings, WRN_RRC_ECE_001);
  }

}
