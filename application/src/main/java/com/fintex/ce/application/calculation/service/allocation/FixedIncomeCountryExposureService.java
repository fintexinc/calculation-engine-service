package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.CountryAllocationMappingService;
import com.fintex.ce.application.mapping.response.CountryExposureResponseMapper;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.CountryExposureResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;

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
public class FixedIncomeCountryExposureService
    extends
      BreakdownAbstractService<Map<PortfolioHolding, CountryExposure>, CountryExposureResult, CountryRegionType>
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, CountryExposure, CountryExposureResult> {

  private final CountryExposureResponseMapper responseMapper;
  private final CountryAllocationMappingService countryAllocationMappingService;

  protected static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new EnumMap<>(CountryRegionType.class);

  public FixedIncomeCountryExposureService(
      final CountryExposureResponseMapper responseMapper,
      final CountryAllocationMappingService countryAllocationMappingService) {
    super();
    this.responseMapper = responseMapper;
    this.countryAllocationMappingService = countryAllocationMappingService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_COUNTRY_EXPOSURE;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.COUNTRY_ALLOCATION;
  }

  @Override
  public CountryExposureResult perform(PortfolioHoldingsCommand command, Map<PortfolioHolding, CountryExposure> data) {
    return calculate(fetchExposures(command, data), command.getHoldings());
  }

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

  public ExposureDataHolder<CountryRegionType> fetchExposures(PortfolioHoldingsCommand command,
      Map<PortfolioHolding, CountryExposure> data) {
    Map<PortfolioHolding, CountryExposure> rawData = FilterUtils.restrictToHoldings(data, command.getHoldings());
    Map<PortfolioHolding, Map<Country, BigDecimal>> mappedHoldings = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    return countryAllocationMappingService.mapToCountryRegions(mappedHoldings, MISSING_BOND_COUNTRY_EXPOSURE);
  }
}
