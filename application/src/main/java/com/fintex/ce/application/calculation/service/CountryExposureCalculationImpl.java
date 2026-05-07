package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.CountryAllocationMappingService;
import com.fintex.ce.application.mapping.response.CountryExposureResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.CountryExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.PortfolioUtils.areAllValuesInMapEmpty;
import static com.fintex.ce.model.error.ErrorCode.MISSING_BOND_COUNTRY_EXPOSURE;
import static java.util.stream.Collectors.toMap;

@Service
public class CountryExposureCalculationImpl extends BreakdownAbstractService<CountryExposureResult, CountryRegionType> {

  private final SecurityDataFetcher<CountryExposure> countryExposureSecurityDataFetcher;
  private final CountryExposureResponseMapper responseMapper;
  private final CountryAllocationMappingService countryAllocationMappingService;

  public static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new EnumMap<>(CountryRegionType.class);

  public CountryExposureCalculationImpl(
      final SecurityDataFetcher<CountryExposure> countryExposureSecurityDataFetcher,
      final CountryExposureResponseMapper responseMapper,
      final CountryAllocationMappingService countryAllocationMappingService) {
    super();
    this.countryExposureSecurityDataFetcher = countryExposureSecurityDataFetcher;
    this.responseMapper = responseMapper;
    this.countryAllocationMappingService = countryAllocationMappingService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_COUNTRY_EXPOSURE;
  }

  @Override
  public CountryExposureResult calculate(ExposureDataHolder<CountryRegionType> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (areAllValuesInMapEmpty(exposures)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    Map<CountryRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, CountryRegionType
        .values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public ExposureDataHolder<CountryRegionType> fetchExposures(PortfolioHoldingsCommand command) {
    List<Notification> warnings = new ArrayList<>();
    Map<PortfolioHolding, CountryExposure> rawData = countryExposureSecurityDataFetcher.fetch(command.getHoldings(),
        List
            .of());
    Map<PortfolioHolding, Map<String, BigDecimal>> mappedHoldings = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    Map<PortfolioHolding, Map<CountryRegionType, BigDecimal>> allocations = countryAllocationMappingService
        .mapToCountryRegions(mappedHoldings, warnings, MISSING_BOND_COUNTRY_EXPOSURE);
    return new ExposureDataHolder<>(allocations, warnings);
  }
}
