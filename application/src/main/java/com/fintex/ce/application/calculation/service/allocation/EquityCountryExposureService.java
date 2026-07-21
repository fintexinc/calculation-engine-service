package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.CountryAllocationMappingService;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityCountryExposureResult;
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
import java.util.stream.Stream;

import static com.fintex.ce.application.util.CalculationUtils.reScaleAbs;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.application.util.PortfolioUtils.areAllValuesInMapEmpty;
import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_COUNTRY_EXPOSURE;
import static java.util.stream.Collectors.toMap;

@Service
public class EquityCountryExposureService
    extends
      BreakdownAbstractService<Map<PortfolioHolding, EquityCountryAllocation>, EquityCountryExposureResult, CountryRegionType>
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, EquityCountryAllocation, EquityCountryExposureResult> {

  private final CountryAllocationMappingService countryAllocationMappingService;

  protected static final Map<CountryRegionType, BigDecimal> DEFAULT_MAP = new EnumMap<>(CountryRegionType.class);

  static {
    Stream.of(CountryRegionType.values()).forEach(f -> DEFAULT_MAP.put(f, null));
  }

  public EquityCountryExposureService(final CountryAllocationMappingService countryAllocationMappingService) {
    super();
    this.countryAllocationMappingService = countryAllocationMappingService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.EQUITY_COUNTRY_EXPOSURE;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.EQUITY_COUNTRY_ALLOCATION;
  }

  @Override
  public EquityCountryExposureResult perform(PortfolioHoldingsCommand command,
      Map<PortfolioHolding, EquityCountryAllocation> data) {
    return calculate(fetchExposures(command, data), command.getHoldings());
  }

  public EquityCountryExposureResult calculate(ExposureDataHolder<CountryRegionType> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    if (areAllValuesInMapEmpty(exposures)) {
      return EquityCountryExposureResult.builder()
          .equityCountryExposure(DEFAULT_MAP)
          .warnings(warnings)
          .build();
    }
    final Map<CountryRegionType, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, CountryRegionType
        .values());
    final Map<CountryRegionType, BigDecimal> scaledValues = toUserScale(reScaleAbs(netProducts));
    return EquityCountryExposureResult.builder()
        .equityCountryExposure(scaledValues)
        .warnings(warnings)
        .build();
  }

  public ExposureDataHolder<CountryRegionType> fetchExposures(final PortfolioHoldingsCommand command,
      final Map<PortfolioHolding, EquityCountryAllocation> data) {
    Map<PortfolioHolding, EquityCountryAllocation> rawData = FilterUtils.restrictToHoldings(data,
        command.getHoldings());
    Map<PortfolioHolding, Map<Country, BigDecimal>> holdingAllocations = rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getAllocations()));
    return countryAllocationMappingService.mapToCountryRegions(holdingAllocations, MISSING_EQUITY_COUNTRY_EXPOSURE);
  }

}
