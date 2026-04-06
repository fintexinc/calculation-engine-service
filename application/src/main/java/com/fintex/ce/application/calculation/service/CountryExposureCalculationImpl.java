package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.mapping.response.CountryExposureResponseMapper;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.calculation.CountryRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.CountryExposureResult;
import com.fintex.ce.mapping.CountryAllocationMappingService;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ExposureDataHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_FICQ_BCE_001;
import static com.fintex.ce.util.PortfolioUtils.areAllValuesInMapEmpty;
import static java.util.stream.Collectors.toMap;

@Service
public class CountryExposureCalculationImpl extends BreakdownAbstractService<CountryExposureResult, CountryRegionType> {

  private final SecurityDataFetcher<CountryExposure> countryExposureSecurityDataFetcher;
  private final CountryExposureResponseMapper responseMapper;
  private final CountryAllocationMappingService countryAllocationMappingService;

  public static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new HashMap<>();

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
  public CountryExposureResult calculate(ExposureDataHolder<CountryRegionType> exposureData,
      List<Holding> holdings) {
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
  public ExposureDataHolder<CountryRegionType> fetchExposures(PortfolioHoldingsCommand reqDTO) {
    List<Warning> warnings = new ArrayList<>();
    Map<Holding, CountryExposure> rawData = countryExposureSecurityDataFetcher.fetch(reqDTO.getHoldings(), List.of());
    Map<Holding, Map<String, BigDecimal>> mappedHoldings = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    Map<Holding, Map<CountryRegionType, BigDecimal>> allocations = countryAllocationMappingService
        .mapToCountryRegions(mappedHoldings, warnings, WRN_FICQ_BCE_001);
    return new ExposureDataHolder<>(allocations, warnings);
  }
}
