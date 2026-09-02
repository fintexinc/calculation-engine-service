package ca.tangerine.pce.webclient.mic.fetcher;

import ca.tangerine.pce.model.domain.calculation.allocation.EquityCountryAllocation;
import ca.tangerine.pce.model.domain.calculation.allocation.EquitySector;
import ca.tangerine.pce.model.domain.calculation.allocation.FixedIncomeBondSector;
import ca.tangerine.pce.model.domain.calculation.allocation.HoldingAssetAllocation;
import ca.tangerine.pce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import ca.tangerine.pce.model.domain.calculation.allocation.HoldingSectorAllocation;
import ca.tangerine.pce.model.domain.calculation.exposure.CountryExposure;
import ca.tangerine.pce.model.domain.calculation.fee.FeeData;
import ca.tangerine.pce.model.domain.calculation.holding.CommonTopHoldings;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.port.webclient.mic.SecurityAttributesFetcher;
import ca.tangerine.pce.webclient.mic.client.MarketInvestmentCatalogueWebClient;
import ca.tangerine.pce.webclient.mic.mapper.AssetAllocationMarketInvestmentCatalogueMapper;
import ca.tangerine.pce.webclient.mic.mapper.CountryExposureMapper;
import ca.tangerine.pce.webclient.mic.mapper.EquityCountryAllocationMapper;
import ca.tangerine.pce.webclient.mic.mapper.EquitySectorAllocationMapper;
import ca.tangerine.pce.webclient.mic.mapper.EquitySectorMapper;
import ca.tangerine.pce.webclient.mic.mapper.FeesMapper;
import ca.tangerine.pce.webclient.mic.mapper.FixedIncomeSectorAllocationMapper;
import ca.tangerine.pce.webclient.mic.mapper.GeographicAllocationMapper;
import ca.tangerine.pce.webclient.mic.mapper.LimitedHoldingsMapper;
import ca.tangerine.pce.webclient.mic.mapper.MonthlyReturnsMapper;
import ca.tangerine.pce.webclient.mic.mapper.SectorAllocationMapper;
import ca.tangerine.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.allocation.CountryAllocation;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorWithCurrency;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.financial.Fees;
import ca.tangerine.wm.commons.domain.financial.Geography;
import ca.tangerine.wm.commons.domain.holding.HoldingIdentifiers;
import ca.tangerine.wm.commons.domain.holding.Holdings;
import ca.tangerine.wm.commons.domain.performance.MonthlyReturns;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import tools.jackson.databind.ObjectMapper;

/**
 * Wiring for the generic Market Investment Catalogue attributes fetcher. The single {@code attributeBindings} bean is
 * the registry mapping every supported {@link CompositeSecurityAttribute} to the MIC response type it deserializes
 * into, the CE domain type the calculation services consume, and the mapper performing the conversion.
 */
@Configuration
@ConditionalOnProperty(name = "external-services.market-investment-catalogue.api-type", havingValue = "rest", matchIfMissing = true)
public class SecurityAttributeFetcherConfig {

  @Bean
  SecurityAttributesFetcher securityAttributesFetcher(
      MarketInvestmentCatalogueWebClient client, ObjectMapper objectMapper,
      List<CompositeAttributeBinding<?, ?>> attributeBindings,
      @Value("${external-services.market-investment-catalogue.rest.endpoints.attributes}") String endpointPath) {
    return new CompositeMarketInvestmentCatalogueFetcher(client, endpointPath, objectMapper, attributeBindings);
  }

  @Bean
  CompositeAttributeBinding<HoldingAssetAllocation, AssetAllocationWithCurrency> assetAllocationBinding(
      AssetAllocationMarketInvestmentCatalogueMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.ASSET_ALLOCATION,
        AssetAllocationWithCurrency.class, HoldingAssetAllocation.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<EquityCountryAllocation, CountryAllocation> equityCountryAllocationBinding(
      EquityCountryAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.EQUITY_COUNTRY_ALLOCATION,
        CountryAllocation.class, EquityCountryAllocation.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<HoldingGeographicAllocation, GeographicAllocationWithCurrency> equityGeographicAllocationBinding(
      GeographicAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.EQUITY_GEOGRAPHIC_ALLOCATION,
        GeographicAllocationWithCurrency.class, HoldingGeographicAllocation.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<HoldingGeographicAllocation, GeographicAllocationWithCurrency> fixedIncomeGeographicAllocationBinding(
      GeographicAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.FIXED_INCOME_GEOGRAPHIC_ALLOCATION,
        GeographicAllocationWithCurrency.class, HoldingGeographicAllocation.class, mapper);
  }

  /**
   * The whole-security counterpart of the two sleeve bindings above. Same wrapper, same mapper: Market Investment
   * Catalogue buckets all three into {@code GeographicRegionType} with one mapping, so the consolidated metric and the
   * per-sleeve ones classify a given country identically.
   */
  @Bean
  CompositeAttributeBinding<HoldingGeographicAllocation, GeographicAllocationWithCurrency> geographicAllocationBinding(
      GeographicAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.GEOGRAPHIC_ALLOCATION,
        GeographicAllocationWithCurrency.class, HoldingGeographicAllocation.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<EquitySector, EquitySectorAllocationWithCurrency> equitySectorAllocationBinding(
      EquitySectorAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION,
        EquitySectorAllocationWithCurrency.class, EquitySector.class, mapper);
  }

  /**
   * The scalar counterpart of the binding above, for a security whose sector is one value rather than a distribution.
   * Same CE domain type on purpose: the mapper widens the single sector into a one-bucket allocation, so a metric
   * reading either attribute reads the same shape and needs no branch of its own.
   */
  @Bean
  CompositeAttributeBinding<EquitySector, EquitySectorWithCurrency> equitySectorBinding(EquitySectorMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.EQUITY_SECTOR,
        EquitySectorWithCurrency.class, EquitySector.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<FixedIncomeBondSector, FixedIncomeSectorAllocationWithCurrency> fixedIncomeBondSectorBinding(
      FixedIncomeSectorAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.FIXED_INCOME_SECTOR_ALLOCATION,
        FixedIncomeSectorAllocationWithCurrency.class, FixedIncomeBondSector.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<HoldingSectorAllocation, SectorAllocationWithCurrency> sectorAllocationBinding(
      SectorAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.SECTOR_ALLOCATION,
        SectorAllocationWithCurrency.class, HoldingSectorAllocation.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<CountryExposure, CountryAllocation> countryExposureBinding(CountryExposureMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.COUNTRY_ALLOCATION,
        CountryAllocation.class, CountryExposure.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<Geography, Geography> geographyBinding() {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.GEOGRAPHY,
        Geography.class, Geography.class, (response, holding) -> response);
  }

  @Bean
  CompositeAttributeBinding<FeeData, Fees> feesBinding(FeesMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.FEES, Fees.class, FeeData.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<HoldingMonthlyReturns, MonthlyReturns> monthlyReturnsBinding(
      MonthlyReturnsMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.MONTHLY_RETURNS,
        MonthlyReturns.class, HoldingMonthlyReturns.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<CommonTopHoldings, Holdings> limitedHoldingsBinding(LimitedHoldingsMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.LIMITED_HOLDINGS,
        Holdings.class, CommonTopHoldings.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<HoldingIdentifiers, HoldingIdentifiers> holdingIdentifiersBinding() {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.HOLDING_IDENTIFIERS,
        HoldingIdentifiers.class, HoldingIdentifiers.class, (response, holding) -> response);
  }
}
