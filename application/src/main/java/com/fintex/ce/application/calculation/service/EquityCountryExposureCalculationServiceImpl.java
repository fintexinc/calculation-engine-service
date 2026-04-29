package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.CountryAllocationMappingService;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityCountryExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.application.util.PortfolioUtils.areAllValuesInMapEmpty;
import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_COUNTRY_EXPOSURE;
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
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_COUNTRY_EXPOSURE;
  }

  @Override
  public EquityCountryExposureResult calculate(ExposureDataHolder<CountryRegionType> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
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
  public ExposureDataHolder<CountryRegionType> fetchExposures(final PortfolioHoldingsCommand command) {
    List<Warning> warnings = new ArrayList<>();
    Map<PortfolioHolding, EquityCountryAllocation> rawData = equityCountryAllocationSecurityDataFetcher.fetch(
        command.getHoldings(), List.of());
    Map<PortfolioHolding, Map<String, BigDecimal>> holdingAllocations = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    Map<PortfolioHolding, Map<CountryRegionType, BigDecimal>> allocations = countryAllocationMappingService
        .mapToCountryRegions(holdingAllocations, warnings, MISSING_EQUITY_COUNTRY_EXPOSURE);
    return new ExposureDataHolder<>(allocations, warnings);
  }

}
