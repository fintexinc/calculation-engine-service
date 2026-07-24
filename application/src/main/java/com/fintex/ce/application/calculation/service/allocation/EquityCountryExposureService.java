package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.mapping.CountryAllocationMappingService;
import com.fintex.ce.model.domain.calculation.allocation.CountryRegionType;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.exposure.EquityCountryExposureResult;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.model.error.ErrorCode.MISSING_EQUITY_COUNTRY_EXPOSURE;

/**
 * Equity country exposure breakdown. Country-keyed allocations are remapped to {@link CountryRegionType} per holding
 * via {@link CountryAllocationMappingService}; the shared {@link AbstractBreakdownService} pipeline does the weighting,
 * aggregation, normalization and response assembly.
 */
@Service
public class EquityCountryExposureService
    extends
      AbstractSingleAttributeBreakdownService<EquityCountryAllocation, EquityCountryExposureResult, CountryRegionType> {

  private final CountryAllocationMappingService countryAllocationMappingService;

  public EquityCountryExposureService(PortfolioWeightCalculator portfolioWeightCalculator,
      CountryAllocationMappingService countryAllocationMappingService) {
    super(portfolioWeightCalculator, CountryRegionType.class);
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

  // TODO: return the holding's currency so multi-currency portfolios weight comparable values. Returning null leaves
  // the holding out of the FX conversion map, so its raw value is weighted as-is and country percentages come out
  // wrong when the portfolio mixes currencies. EquityCountryAllocation carries no currency today, so this needs the
  // datapoint extended (or the currency sourced elsewhere) first. Same gap in FixedIncomeCountryExposureService.
  @Override
  protected Currency currencyOf(EquityCountryAllocation attribute) {
    return null;
  }

  @Override
  protected Map<CountryRegionType, BigDecimal> toBuckets(PortfolioHolding holding, EquityCountryAllocation attribute,
      List<Notification> warnings) {
    return countryAllocationMappingService.mapToRegions(holding,
        attribute == null ? null : attribute.getAllocations(), warnings, MISSING_EQUITY_COUNTRY_EXPOSURE);
  }

  @Override
  protected EquityCountryExposureResult buildResult(Map<CountryRegionType, BigDecimal> buckets,
      List<Notification> warnings) {
    return EquityCountryExposureResult.builder()
        .equityCountryExposure(buckets)
        .warnings(warnings)
        .build();
  }
}
