package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.result.GeographicExposureResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.ce.service.GeographicAllocationMappingService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_FICQ_BCE_001;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static java.util.stream.Collectors.toMap;

@Service
public class FixedIncomeGeographicExposureCalculationImpl
    extends
      BreakdownAbstractService<GeographicExposureResult, GeographicRegionType> {

  private final SecurityDataFetcher<CountryExposure> fiCountryExposureSecurityDataFetcher;
  private final GeographicAllocationMappingService geographicAllocationMappingService;

  public static final Map<GeographicRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

  static {
    Stream.of(GeographicRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  public FixedIncomeGeographicExposureCalculationImpl(
      final SecurityDataFetcher<CountryExposure> fiCountryExposureSecurityDataFetcher,
      final GeographicAllocationMappingService geographicAllocationMappingService) {
    super();
    this.fiCountryExposureSecurityDataFetcher = fiCountryExposureSecurityDataFetcher;
    this.geographicAllocationMappingService = geographicAllocationMappingService;
  }

  @Override
  public GeographicExposureResult calculate(Map<Holding, Map<GeographicRegionType, BigDecimal>> exposures,
      List<Holding> holdings,
      List<Warning> warnings) {
    if (areAllValuesInMapEmpty(exposures)) {
      GeographicExposureResult defaultResult = new GeographicExposureResult();
      defaultResult.setEquityGeographicExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    Map<GeographicRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, GeographicRegionType
        .values());
    Map<GeographicRegionType, BigDecimal> rescaledValues = toUserScale(reScaleAbs(netProducts));
    GeographicExposureResult geoResult = new GeographicExposureResult();
    geoResult.setEquityGeographicExposure(rescaledValues);
    geoResult.setWarnings(warnings);
    return geoResult;
  }

  @Override
  public Map<Holding, Map<GeographicRegionType, BigDecimal>> fetchExposures(PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    Map<Holding, CountryExposure> rawData = fiCountryExposureSecurityDataFetcher.fetch(reqDTO.getHoldings(), List.of());
    Map<Holding, Map<String, BigDecimal>> mappedHoldings = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    return geographicAllocationMappingService.mapToGeographicRegions(mappedHoldings, warnings, WRN_FICQ_BCE_001);
  }

}
