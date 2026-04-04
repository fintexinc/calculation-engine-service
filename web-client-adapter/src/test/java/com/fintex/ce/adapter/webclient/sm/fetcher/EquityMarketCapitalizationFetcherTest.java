package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.EquityMarketCapitalizationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.HoldingEquityMarketCap;
import com.fintex.sm.model.domain.datapoint.EquityMarketCapitalization;
import com.fintex.sm.model.domain.enumeration.EquityMarketCapitalizationType;
import com.fintex.sm.model.domain.value.EquityMarketCapitalizationTypeValue;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class EquityMarketCapitalizationFetcherTest
    extends
      AbstractSecurityMasterFetcherTest<HoldingEquityMarketCap, EquityMarketCapitalization> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/equity-market-cap";

  @Mock
  private EquityMarketCapitalizationMapper mapper;

  private AbstractSecurityMasterFetcher<HoldingEquityMarketCap, EquityMarketCapitalization> fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new AbstractSecurityMasterFetcher<>(securityMasterWebClient, ENDPOINT_PATH, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<EquityMarketCapitalization>>>() {}) {};
  }

  @Override
  protected AbstractSecurityMasterFetcher<HoldingEquityMarketCap, EquityMarketCapitalization> fetcher() {
    return fetcher;
  }

  @Override
  protected String expectedEndpointPath() {
    return ENDPOINT_PATH;
  }

  @Override
  protected EquityMarketCapitalization createSmsResponse() {
    var giant = new EquityMarketCapitalizationTypeValue();
    giant.setEquityMarketCapitalization(EquityMarketCapitalizationType.GIANT);
    giant.setValue(BigDecimal.valueOf(45.67));

    var large = new EquityMarketCapitalizationTypeValue();
    large.setEquityMarketCapitalization(EquityMarketCapitalizationType.LARGE);
    large.setValue(BigDecimal.valueOf(30.0));

    var smsResponse = new EquityMarketCapitalization();
    smsResponse.setValues(List.of(giant, large));
    return smsResponse;
  }

  @Override
  protected HoldingEquityMarketCap createExpectedDomainModel(String holdingId) {
    EnumMap<EquityMarketCapitalizationType, BigDecimal> ratings = new EnumMap<>(EquityMarketCapitalizationType.class);
    ratings.put(EquityMarketCapitalizationType.GIANT, BigDecimal.valueOf(45.67));
    ratings.put(EquityMarketCapitalizationType.LARGE, BigDecimal.valueOf(30.0));
    return new HoldingEquityMarketCap()
        .setRatings(ratings)
        .setHoldingId(holdingId);
  }

  @Override
  protected SecurityMasterResponseMapper<HoldingEquityMarketCap, EquityMarketCapitalization> mapper() {
    return mapper;
  }
}
