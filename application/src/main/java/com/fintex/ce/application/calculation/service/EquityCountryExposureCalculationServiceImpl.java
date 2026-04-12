package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquityCountryExposureResult;
import com.fintex.ce.mapping.CountryAllocationMappingService;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ExposureDataHolder;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.domain.exception.code.ErrorCode.WRN_RRC_ECE_001;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
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
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_COUNTRY_EXPOSURE;
  }

  @Override
  public EquityCountryExposureResult calculate(ExposureDataHolder<CountryRegionType> exposureData,
      List<Holding> holdings) {
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
  public ExposureDataHolder<CountryRegionType> fetchExposures(final PortfolioHoldingsCommand reqDTO) {
    List<Warning> warnings = new ArrayList<>();
    Map<Holding, EquityCountryAllocation> rawData = equityCountryAllocationSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    Map<Holding, Map<String, BigDecimal>> holdingAllocations = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    Map<Holding, Map<CountryRegionType, BigDecimal>> allocations = countryAllocationMappingService
        .mapToCountryRegions(holdingAllocations, warnings, WRN_RRC_ECE_001);
    return new ExposureDataHolder<>(allocations, warnings);
  }

}
