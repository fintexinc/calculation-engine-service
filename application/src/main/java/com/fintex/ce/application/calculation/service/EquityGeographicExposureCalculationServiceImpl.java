package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.EquityGeographicExposureResult;
import com.fintex.ce.mapping.GeographicAllocationMappingService;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ExposureDataHolder;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.domain.exception.code.ErrorCode.WRN_RRC_EGE_001;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityGeographicExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<EquityGeographicExposureResult, GeographicRegionType> {

  private final SecurityDataFetcher<EquityCountryAllocation> equityCountryAllocationSecurityDataFetcher;
  private final GeographicAllocationMappingService geographicAllocationMappingService;

  static final Map<GeographicRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(GeographicRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  public EquityGeographicExposureCalculationServiceImpl(
      final SecurityDataFetcher<EquityCountryAllocation> equityCountryAllocationSecurityDataFetcher,
      final GeographicAllocationMappingService geographicAllocationMappingService) {
    super();
    this.equityCountryAllocationSecurityDataFetcher = equityCountryAllocationSecurityDataFetcher;
    this.geographicAllocationMappingService = geographicAllocationMappingService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_GEOGRAPHIC_EXPOSURE;
  }

  @Override
  public EquityGeographicExposureResult calculate(ExposureDataHolder<GeographicRegionType> exposureData,
      List<Holding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (areAllValuesInMapEmpty(exposures)) {
      EquityGeographicExposureResult defaultResult = new EquityGeographicExposureResult();
      defaultResult.setEquityGeographicExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    final Map<GeographicRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        GeographicRegionType.values());
    final Map<GeographicRegionType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
    EquityGeographicExposureResult result = new EquityGeographicExposureResult();
    result.setEquityGeographicExposure(scaledValues);
    result.setWarnings(warnings);
    return result;
  }

  @Override
  public ExposureDataHolder<GeographicRegionType> fetchExposures(final PortfolioHoldingsCommand reqDTO) {
    List<Warning> warnings = new ArrayList<>();
    Map<Holding, EquityCountryAllocation> rawData = equityCountryAllocationSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    Map<Holding, Map<String, BigDecimal>> mappedHoldings = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    Map<Holding, Map<GeographicRegionType, BigDecimal>> allocations = geographicAllocationMappingService
        .mapToGeographicRegions(mappedHoldings, warnings, WRN_RRC_EGE_001);
    return new ExposureDataHolder<>(allocations, warnings);
  }

}
