package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.GeographicExposureResult;
import com.fintex.ce.mapping.GeographicAllocationMappingService;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_RRC_EGE_001;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityGeographicExposureCalculationServiceImpl
    extends
      BreakdownAbstractService<GeographicExposureResult, GeographicRegionType> {

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
  public GeographicExposureResult calculate(final Map<Holding, Map<GeographicRegionType, BigDecimal>> exposures,
      final List<Holding> holdings,
      final List<Warning> warnings) {
    if (areAllValuesInMapEmpty(exposures)) {
      GeographicExposureResult defaultResult = new GeographicExposureResult();
      defaultResult.setEquityGeographicExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    final Map<GeographicRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        GeographicRegionType.values());
    final Map<GeographicRegionType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
    GeographicExposureResult result = new GeographicExposureResult();
    result.setEquityGeographicExposure(scaledValues);
    result.setWarnings(warnings);
    return result;
  }

  @Override
  public Map<Holding, Map<GeographicRegionType, BigDecimal>> fetchExposures(
      final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    Map<Holding, EquityCountryAllocation> rawData = equityCountryAllocationSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    Map<Holding, Map<String, BigDecimal>> mappedHoldings = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    return geographicAllocationMappingService.mapToGeographicRegions(mappedHoldings, warnings, WRN_RRC_EGE_001);
  }

}
