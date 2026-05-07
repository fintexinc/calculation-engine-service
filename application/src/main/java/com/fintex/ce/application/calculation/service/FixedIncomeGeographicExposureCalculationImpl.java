package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.GeographicAllocationMappingService;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.GeographicRegionType;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.FixedIncomeGeographicExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.error.Notification;

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
import static com.fintex.ce.model.error.ErrorCode.MISSING_BOND_COUNTRY_EXPOSURE;
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
      List<PortfolioHolding> holdings) {
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
  public ExposureDataHolder<GeographicRegionType> fetchExposures(PortfolioHoldingsCommand command) {
    List<Notification> warnings = new ArrayList<>();
    Map<PortfolioHolding, CountryExposure> rawData = fiCountryExposureSecurityDataFetcher.fetch(command.getHoldings(),
        List.of());
    Map<PortfolioHolding, Map<String, BigDecimal>> mappedHoldings = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    Map<PortfolioHolding, Map<GeographicRegionType, BigDecimal>> allocations = geographicAllocationMappingService
        .mapToGeographicRegions(mappedHoldings, warnings, MISSING_BOND_COUNTRY_EXPOSURE);
    return new ExposureDataHolder<>(allocations, warnings);
  }

}
