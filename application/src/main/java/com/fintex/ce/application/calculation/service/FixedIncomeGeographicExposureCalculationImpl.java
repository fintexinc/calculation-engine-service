package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.calculation.GeographicRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.FixedIncomeGeographicExposureResult;
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

import static com.fintex.ce.domain.exception.code.ErrorCode.WRN_FICQ_BCE_001;
import static com.fintex.ce.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static java.util.stream.Collectors.toMap;

@Service
public class FixedIncomeGeographicExposureCalculationImpl
    extends
      BreakdownAbstractService<FixedIncomeGeographicExposureResult, GeographicRegionType> {

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
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_GEOGRAPHIC_EXPOSURE;
  }

  @Override
  public FixedIncomeGeographicExposureResult calculate(ExposureDataHolder<GeographicRegionType> exposureData,
      List<Holding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (areAllValuesInMapEmpty(exposures)) {
      FixedIncomeGeographicExposureResult defaultResult = new FixedIncomeGeographicExposureResult();
      defaultResult.setEquityGeographicExposure(DEFAULT_MAP);
      defaultResult.setWarnings(warnings);
      return defaultResult;
    }
    Map<GeographicRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, GeographicRegionType
        .values());
    Map<GeographicRegionType, BigDecimal> rescaledValues = toUserScale(reScaleAbs(netProducts));
    FixedIncomeGeographicExposureResult geoResult = new FixedIncomeGeographicExposureResult();
    geoResult.setEquityGeographicExposure(rescaledValues);
    geoResult.setWarnings(warnings);
    return geoResult;
  }

  @Override
  public ExposureDataHolder<GeographicRegionType> fetchExposures(PortfolioHoldingsCommand reqDTO) {
    List<Warning> warnings = new ArrayList<>();
    Map<Holding, CountryExposure> rawData = fiCountryExposureSecurityDataFetcher.fetch(reqDTO.getHoldings(), List.of());
    Map<Holding, Map<String, BigDecimal>> mappedHoldings = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    Map<Holding, Map<GeographicRegionType, BigDecimal>> allocations = geographicAllocationMappingService
        .mapToGeographicRegions(mappedHoldings, warnings, WRN_FICQ_BCE_001);
    return new ExposureDataHolder<>(allocations, warnings);
  }

}
