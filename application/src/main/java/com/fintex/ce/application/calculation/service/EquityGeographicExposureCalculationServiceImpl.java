package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.GeographicAllocationMappingService;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.calculation.allocation.GeographicRegionType;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityGeographicExposureResult;
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
import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_GEOGRAPHIC_EXPOSURE;
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
      List<PortfolioHolding> holdings) {
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
    Map<PortfolioHolding, EquityCountryAllocation> rawData = equityCountryAllocationSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    Map<PortfolioHolding, Map<String, BigDecimal>> mappedHoldings = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    Map<PortfolioHolding, Map<GeographicRegionType, BigDecimal>> allocations = geographicAllocationMappingService
        .mapToGeographicRegions(mappedHoldings, warnings, MISSING_EQUITY_GEOGRAPHIC_EXPOSURE);
    return new ExposureDataHolder<>(allocations, warnings);
  }

}
