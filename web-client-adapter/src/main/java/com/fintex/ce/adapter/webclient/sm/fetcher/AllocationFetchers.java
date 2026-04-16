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
import com.fintex.ce.adapter.webclient.sm.mapper.MaturityAllocationMapper;
import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocation;
import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.ce.model.domain.calculation.allocation.EquityCountryAllocation;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.calculation.allocation.HoldingEquityMarketCap;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocation;
import com.fintex.ce.model.domain.calculation.exposure.CountryExposure;
import com.fintex.ce.model.domain.calculation.exposure.EquityStyleboxExposure;
import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.allocation.CountryAllocation;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalization;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocation;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import com.fintex.wm.commons.domain.allocation.Maturities;
import com.fintex.wm.commons.domain.allocation.SecurityClassificationAllocation;
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
  SecurityDataFetcher<HoldingAssetAllocation> assetAllocationFetcher(
      SecurityMasterWebClient client, AssetAllocationSecurityMasterMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.asset}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<AssetAllocation>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<CreditQuality> creditQualityFetcher(
      SecurityMasterWebClient client, CreditQualityMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.credit-quality}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<CreditQualityRatings>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<EquityCountryAllocation> equityCountryAllocationFetcher(
      SecurityMasterWebClient client, EquityCountryAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-country}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<CountryAllocation>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<HoldingEquityMarketCap> equityMarketCapitalizationFetcher(
      SecurityMasterWebClient client, EquityMarketCapitalizationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-market-cap}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<EquityMarketCapitalization>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<EquitySector> equitySectorAllocationFetcher(
      SecurityMasterWebClient client, EquitySectorAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-sector}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<EquitySectorAllocation>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<EquityStyleboxExposure> equityStyleboxExposureFetcher(
      SecurityMasterWebClient client, EquityStyleboxExposureMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.equity-stylebox}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<StyleBoxes>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeBondSecuritiesFetcher(
      SecurityMasterWebClient client, FixedIncomeSectorAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.fixed-income-sector}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<FixedIncomeSectorAllocation>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<FixedIncomeStyleboxExposure> fixedIncomeStyleboxExposureFetcher(
      SecurityMasterWebClient client, FixedIncomeStyleboxExposureMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.fixed-income-stylebox}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<FixedIncomeStyleBoxes>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<MaturityAllocation> maturityAllocationFetcher(
      SecurityMasterWebClient client, MaturityAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.maturities}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<Maturities>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<CountryExposure> countryExposureFetcher(
      SecurityMasterWebClient client, CountryExposureMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.country}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<CountryAllocation>>>() {}) {};
  }

  @Bean
  SecurityDataFetcher<ClassificationAllocation> classificationAllocationFetcher(
      SecurityMasterWebClient client, ClassificationAllocationMapper mapper,
      @Value("${external-services.security-master.rest.endpoints.allocations.classification}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<SecurityClassificationAllocation>>>() {}) {};
  }
}
