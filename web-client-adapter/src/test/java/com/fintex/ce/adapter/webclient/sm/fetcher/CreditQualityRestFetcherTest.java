package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.CreditQualityMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.calculation.CreditQualityRating;
import com.fintex.sm.model.domain.rating.CreditQualityRatings;
import com.fintex.sm.model.domain.value.CreditQualityRatingTypeValue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditQualityRestFetcherTest
    extends AbstractSecurityMasterFetcherTest<CreditQuality, CreditQualityRatings> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/credit-quality";

  @Mock
  private CreditQualityMapper mapper;

  private CreditQualityRestFetcher fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new CreditQualityRestFetcher(client, mapper, ENDPOINT_PATH);
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
        new CreditQualityRatingTypeValue(CreditQualityRating.AAA.getRating(), BigDecimal.valueOf(15.5), List.of()),
        new CreditQualityRatingTypeValue(CreditQualityRating.AA.getRating(), BigDecimal.valueOf(22.3), List.of())));
    smsResponse.setAverageCreditQualityRating("A");
    return smsResponse;
  }

  @Override
  protected CreditQuality createExpectedDomainModel(String holdingId) {
    return new CreditQuality()
        .setRatings(Map.of(
            CreditQualityRating.AAA, BigDecimal.valueOf(15.5),
            CreditQualityRating.AA, BigDecimal.valueOf(22.3)))
        .setHoldingId(holdingId);
  }

  @Override
  protected SecurityMasterResponseMapper<CreditQuality, CreditQualityRatings> mapper() {
    return mapper;
  }
}