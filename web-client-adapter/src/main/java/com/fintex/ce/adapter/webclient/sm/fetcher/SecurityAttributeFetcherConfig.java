package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.AssetAllocationSecurityMasterMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.ClassificationAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.CountryExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.CreditQualityMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.EquityCountryAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.EquityMarketCapitalizationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.EquitySectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.EquityStyleboxExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.FeesMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeSectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeStyleboxExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.GeographicAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.MaturityAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.MonthlyReturnsMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SalesChargeMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.TopHoldingsMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.YieldMapper;
import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocation;
import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSector;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.calculation.allocation.HoldingEquityMarketCap;
import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocation;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.port.webclient.sm.SecurityAttributesFetcher;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.CountryAllocation;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalization;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.Maturities;
import com.fintex.wm.commons.domain.allocation.SecurityClassificationAllocation;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.financial.Fees;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.domain.financial.Income;
import com.fintex.wm.commons.domain.holding.HoldingIdentifiers;
import com.fintex.wm.commons.domain.holding.TopHoldings;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.rating.CreditQualityRatings;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxes;
import com.fintex.wm.commons.domain.rating.StyleBoxes;
import com.fintex.wm.commons.domain.sales.SalesChargeData;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Wiring for the generic Security Master attributes fetcher. The single {@code attributeBindings} bean is the registry
 * mapping every supported {@link CompositeSecurityAttribute} to the SMS response type it deserializes into, the CE
 * domain type the calculation services consume, and the mapper performing the conversion.
 */
@Configuration
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class SecurityAttributeFetcherConfig {

  @Bean
  SecurityAttributesFetcher securityAttributesFetcher(
      SecurityMasterWebClient client, ObjectMapper objectMapper,
      List<CompositeAttributeBinding<?, ?>> attributeBindings,
      @Value("${external-services.security-master.rest.endpoints.attributes}") String endpointPath) {
    return new CompositeSecurityMasterFetcher(client, endpointPath, objectMapper, attributeBindings);
  }

  @Bean
  CompositeAttributeBinding<HoldingAssetAllocation, AssetAllocationWithCurrency> assetAllocationBinding(
      AssetAllocationSecurityMasterMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.ASSET_ALLOCATION,
        AssetAllocationWithCurrency.class, HoldingAssetAllocation.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<CreditQuality, CreditQualityRatings> creditQualityBinding(CreditQualityMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.CREDIT_QUALITY_RATINGS,
        CreditQualityRatings.class, CreditQuality.class, mapper);
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
   * The whole-security counterpart of the two sleeve bindings above. Same wrapper, same mapper: Security Master buckets
   * all three into {@code GeographicRegionType} with one mapping, so the consolidated metric and the per-sleeve ones
   * classify a given country identically.
   */
  @Bean
  CompositeAttributeBinding<HoldingGeographicAllocation, GeographicAllocationWithCurrency> geographicAllocationBinding(
      GeographicAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.GEOGRAPHIC_ALLOCATION,
        GeographicAllocationWithCurrency.class, HoldingGeographicAllocation.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<HoldingEquityMarketCap, EquityMarketCapitalization> equityMarketCapitalizationBinding(
      EquityMarketCapitalizationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.EQUITY_MARKET_CAPITALIZATION,
        EquityMarketCapitalization.class, HoldingEquityMarketCap.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<EquitySector, EquitySectorAllocationWithCurrency> equitySectorAllocationBinding(
      EquitySectorAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.EQUITY_SECTOR_ALLOCATION,
        EquitySectorAllocationWithCurrency.class, EquitySector.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<EquityStyleboxExposure, StyleBoxes> equityStyleboxExposureBinding(
      EquityStyleboxExposureMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.EQUITY_STYLEBOX,
        StyleBoxes.class, EquityStyleboxExposure.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<FixedIncomeBondSector, FixedIncomeSectorAllocationWithCurrency> fixedIncomeBondSectorBinding(
      FixedIncomeSectorAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.FIXED_INCOME_SECTOR_ALLOCATION,
        FixedIncomeSectorAllocationWithCurrency.class, FixedIncomeBondSector.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> fixedIncomeStyleboxExposureBinding(
      FixedIncomeStyleboxExposureMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.FIXED_INCOME_STYLEBOX,
        FixedIncomeStyleBoxes.class, FixedIncomeStyleboxExposure.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<MaturityAllocation, Maturities> maturityAllocationBinding(
      MaturityAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.MATURITIES,
        Maturities.class, MaturityAllocation.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<CountryExposure, CountryAllocation> countryExposureBinding(CountryExposureMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.COUNTRY_ALLOCATION,
        CountryAllocation.class, CountryExposure.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<ClassificationAllocation, SecurityClassificationAllocation> classificationAllocationBinding(
      ClassificationAllocationMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.SECURITY_CLASSIFICATION_ALLOCATION,
        SecurityClassificationAllocation.class, ClassificationAllocation.class, mapper);
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
  CompositeAttributeBinding<SalesCharge, SalesChargeData> salesChargeBinding(SalesChargeMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.SALES_CHARGE,
        SalesChargeData.class, SalesCharge.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<Yield, Income> yieldBinding(YieldMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.INCOME, Income.class, Yield.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<HoldingMonthlyReturns, MonthlyReturns> monthlyReturnsBinding(
      MonthlyReturnsMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.MONTHLY_RETURNS,
        MonthlyReturns.class, HoldingMonthlyReturns.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<CommonTopHoldings, TopHoldings> topHoldingsBinding(TopHoldingsMapper mapper) {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.TOP_HOLDINGS,
        TopHoldings.class, CommonTopHoldings.class, mapper);
  }

  @Bean
  CompositeAttributeBinding<HoldingIdentifiers, HoldingIdentifiers> holdingIdentifiersBinding() {
    return new CompositeAttributeBinding<>(CompositeSecurityAttribute.HOLDING_IDENTIFIERS,
        HoldingIdentifiers.class, HoldingIdentifiers.class, (response, holding) -> response);
  }
}
