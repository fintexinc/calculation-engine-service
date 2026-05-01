package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.CreditQualityMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.CreditQuality;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.rating.CreditQualityRatingType;
import com.fintex.wm.commons.domain.rating.CreditQualityRatingTypeValue;
import com.fintex.wm.commons.domain.rating.CreditQualityRatings;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CreditQualityRestFetcherTest
    extends
      AbstractSecurityMasterFetcherTest<CreditQuality, CreditQualityRatings> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/credit-quality";

  @Mock
  private CreditQualityMapper mapper;

  private AbstractSecurityMasterFetcher<CreditQuality, CreditQualityRatings> fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new AbstractSecurityMasterFetcher<>(securityMasterWebClient, ENDPOINT_PATH, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<CreditQualityRatings>>>() {}) {};
  }

  @Override
  protected AbstractSecurityMasterFetcher<CreditQuality, CreditQualityRatings> fetcher() {
    return fetcher;
  }

  @Override
  protected String expectedEndpointPath() {
    return ENDPOINT_PATH;
  }

  @Override
  protected CreditQualityRatings createSmsResponse() {
    var smsResponse = new CreditQualityRatings();
    smsResponse.setRatings(List.of(
        new CreditQualityRatingTypeValue(CreditQualityRatingType.AAA.name(), BigDecimal.valueOf(15.5), List.of()),
        new CreditQualityRatingTypeValue(CreditQualityRatingType.AA.name(), BigDecimal.valueOf(22.3), List.of())));
    smsResponse.setAverageCreditQualityRating("A");
    return smsResponse;
  }

  @Override
  protected CreditQuality createExpectedDomainModel(String holdingId) {
    final CreditQuality tmpCreditQuality = new CreditQuality();
    tmpCreditQuality.setRatings(Map.of(
        CreditQualityRatingType.AAA, BigDecimal.valueOf(15.5),
        CreditQualityRatingType.AA, BigDecimal.valueOf(22.3)));
    tmpCreditQuality.setHoldingId(holdingId);
    return tmpCreditQuality;
  }

  @Override
  protected SecurityMasterResponseMapper<CreditQuality, CreditQualityRatings> mapper() {
    return mapper;
  }
}