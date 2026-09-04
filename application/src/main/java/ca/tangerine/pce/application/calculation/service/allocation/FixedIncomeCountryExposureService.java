package ca.tangerine.pce.application.calculation.service.allocation;

import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.application.mapping.CountryAllocationMappingService;
import ca.tangerine.pce.model.domain.calculation.allocation.CountryRegionType;
import ca.tangerine.pce.model.domain.calculation.exposure.CountryExposure;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.exposure.CountryExposureResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static ca.tangerine.pce.model.error.ErrorCode.MISSING_BOND_COUNTRY_EXPOSURE;

/**
 * Fixed-income (bond) country exposure breakdown. Country-keyed allocations are remapped to {@link CountryRegionType}
 * per holding via {@link CountryAllocationMappingService}; the shared {@link AbstractBreakdownService} pipeline does
 * the weighting, aggregation, normalization and response assembly.
 */
@Service
public class FixedIncomeCountryExposureService
    extends
      AbstractSingleAttributeBreakdownService<CountryExposure, CountryExposureResult, CountryRegionType> {

  private final CountryAllocationMappingService countryAllocationMappingService;

  public FixedIncomeCountryExposureService(PortfolioWeightCalculator portfolioWeightCalculator,
      CountryAllocationMappingService countryAllocationMappingService) {
    super(portfolioWeightCalculator, CountryRegionType.class);
    this.countryAllocationMappingService = countryAllocationMappingService;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_COUNTRY_EXPOSURE;
  }

  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.FIXED_INCOME_COUNTRY_ALLOCATION;
  }

  // TODO: return the holding's currency so multi-currency portfolios weight comparable values. Returning null leaves
  // the holding out of the FX conversion map, so its raw value is weighted as-is and country percentages come out
  // wrong when the portfolio mixes currencies. CountryExposure carries no currency today, so this needs the datapoint
  // extended (or the currency sourced elsewhere) first. Same gap in EquityCountryExposureService.
  @Override
  protected Currency currencyOf(CountryExposure attribute) {
    return null;
  }

  @Override
  protected Map<CountryRegionType, BigDecimal> toBuckets(PortfolioHolding holding, CountryExposure attribute,
      List<Notification> warnings) {
    return countryAllocationMappingService.mapToRegions(holding,
        attribute == null ? null : attribute.getAllocations(), warnings, MISSING_BOND_COUNTRY_EXPOSURE);
  }

  @Override
  protected CountryExposureResult buildResult(Map<CountryRegionType, BigDecimal> buckets,
      List<Notification> warnings) {
    return CountryExposureResult.builder()
        .countryExposure(buckets)
        .warnings(warnings)
        .build();
  }
}
