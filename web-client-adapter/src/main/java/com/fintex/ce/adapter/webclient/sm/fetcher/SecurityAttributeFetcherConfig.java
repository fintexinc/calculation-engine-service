package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.AssetAllocationSecurityMasterMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.CountryExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.EquityCountryAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.EquitySectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.EquitySectorMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.FeesMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeSectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.GeographicAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.LimitedHoldingsMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.MonthlyReturnsMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SectorAllocationMapper;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSector;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.calculation.allocation.HoldingSectorAllocation;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.port.webclient.sm.SecurityAttributesFetcher;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.CountryAllocation;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.EquitySectorWithCurrency;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.SectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.financial.Fees;
import com.fintex.wm.commons.domain.financial.Geography;
import com.fintex.wm.commons.domain.holding.HoldingIdentifiers;
import com.fintex.wm.commons.domain.holding.Holdings;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;

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
