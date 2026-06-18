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
import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeSectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeStyleboxExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.GeographicAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.MaturityAllocationMapper;
import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocation;
import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.calculation.allocation.HoldingEquityMarketCap;
import com.fintex.ce.model.domain.calculation.allocation.HoldingGeographicAllocation;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocation;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.CountryAllocation;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalization;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocation;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import com.fintex.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import com.fintex.wm.commons.domain.allocation.Maturities;
import com.fintex.wm.commons.domain.allocation.SecurityClassificationAllocation;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeBundle;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.rating.CreditQualityRatings;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxes;
import com.fintex.wm.commons.domain.rating.StyleBoxes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/**
 * REST fetchers for Security Master allocation endpoints (/allocations/*).
 */
@Configuration
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class AllocationFetchers {

  @Bean
  AbstractSecurityMasterFetcher<HoldingAssetAllocation, AssetAllocationWithCurrency> assetAllocationFetcher(
      SecurityMasterWebClient client, AssetAllocationSecurityMasterMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.asset}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<AssetAllocationWithCurrency>>>() {},
        SecurityAttributeBundle.ASSET_ALLOCATION, AssetAllocationWithCurrency.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<CreditQuality, CreditQualityRatings> creditQualityFetcher(
      SecurityMasterWebClient client, CreditQualityMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.credit-quality}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<CreditQualityRatings>>>() {},
        SecurityAttributeBundle.CREDIT_QUALITY_RATINGS, CreditQualityRatings.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<EquityCountryAllocation, CountryAllocation> equityCountryAllocationFetcher(
      SecurityMasterWebClient client, EquityCountryAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-country}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<CountryAllocation>>>() {},
        SecurityAttributeBundle.EQUITY_COUNTRY_ALLOCATION, CountryAllocation.class) {};
  }

  @Bean("equityGeographicAllocationFetcher")
  AbstractSecurityMasterFetcher<HoldingGeographicAllocation, GeographicAllocationWithCurrency> equityGeographicAllocationFetcher(
      SecurityMasterWebClient client, GeographicAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-geographic}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<GeographicAllocationWithCurrency>>>() {},
        SecurityAttributeBundle.EQUITY_GEOGRAPHIC_ALLOCATION, GeographicAllocationWithCurrency.class) {};
  }

  @Bean("fixedIncomeGeographicAllocationFetcher")
  AbstractSecurityMasterFetcher<HoldingGeographicAllocation, GeographicAllocationWithCurrency> fixedIncomeGeographicAllocationFetcher(
      SecurityMasterWebClient client, GeographicAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.fixed-income-geographic}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<GeographicAllocationWithCurrency>>>() {},
        SecurityAttributeBundle.FIXED_INCOME_GEOGRAPHIC_ALLOCATION, GeographicAllocationWithCurrency.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<HoldingEquityMarketCap, EquityMarketCapitalization> equityMarketCapitalizationFetcher(
      SecurityMasterWebClient client, EquityMarketCapitalizationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-market-cap}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<EquityMarketCapitalization>>>() {},
        SecurityAttributeBundle.EQUITY_MARKET_CAPITALIZATION, EquityMarketCapitalization.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<EquitySector, EquitySectorAllocation> equitySectorAllocationFetcher(
      SecurityMasterWebClient client, EquitySectorAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-sector}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<EquitySectorAllocation>>>() {},
        SecurityAttributeBundle.EQUITY_SECTOR_ALLOCATION, EquitySectorAllocation.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<EquityStyleboxExposure, StyleBoxes> equityStyleboxExposureFetcher(
      SecurityMasterWebClient client, EquityStyleboxExposureMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-stylebox}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<StyleBoxes>>>() {},
        SecurityAttributeBundle.EQUITY_STYLE_BOXES, StyleBoxes.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> fixedIncomeBondSecuritiesFetcher(
      SecurityMasterWebClient client, FixedIncomeSectorAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.fixed-income-sector}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<FixedIncomeSectorAllocation>>>() {},
        SecurityAttributeBundle.FIXED_INCOME_SECTOR_ALLOCATION, FixedIncomeSectorAllocation.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> fixedIncomeStyleboxExposureFetcher(
      SecurityMasterWebClient client, FixedIncomeStyleboxExposureMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.fixed-income-stylebox}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<FixedIncomeStyleBoxes>>>() {},
        SecurityAttributeBundle.FIXED_INCOME_STYLE_BOXES, FixedIncomeStyleBoxes.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<MaturityAllocation, Maturities> maturityAllocationFetcher(
      SecurityMasterWebClient client, MaturityAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.maturities}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<Maturities>>>() {},
        SecurityAttributeBundle.MATURITIES, Maturities.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<CountryExposure, CountryAllocation> countryExposureFetcher(
      SecurityMasterWebClient client, CountryExposureMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.country}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<CountryAllocation>>>() {},
        SecurityAttributeBundle.COUNTRY_ALLOCATION, CountryAllocation.class) {};
  }

  @Bean
  AbstractSecurityMasterFetcher<ClassificationAllocation, SecurityClassificationAllocation> classificationAllocationFetcher(
      SecurityMasterWebClient client, ClassificationAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.classification}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<SecurityClassificationAllocation>>>() {},
        SecurityAttributeBundle.SECURITY_CLASSIFICATION_ALLOCATION, SecurityClassificationAllocation.class) {};
  }
}
